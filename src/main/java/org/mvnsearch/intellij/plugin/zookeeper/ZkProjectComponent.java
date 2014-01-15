package org.mvnsearch.intellij.plugin.zookeeper;

import com.intellij.ide.IconUtilEx;
import com.intellij.ide.ui.customization.CustomizationUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.IconUtil;
import com.intellij.util.io.IOUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.common.IOUtils;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.WrappingIconPanel;
import org.jetbrains.annotations.NotNull;
import org.mvnsearch.intellij.plugin.zookeeper.ui.ZkNode;
import org.mvnsearch.intellij.plugin.zookeeper.ui.ZkTreeModel;
import org.mvnsearch.intellij.plugin.zookeeper.vfs.ZkVirtualFileSystem;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;

/**
 * Zoo Keeper project component
 *
 * @author linux_china
 */
public class ZkProjectComponent extends DoubleClickListener implements ProjectComponent {
    private Project project;
    private CuratorFramework curator;
    private Tree zkTree;
    private ZkVirtualFileSystem fileSystem;
    private final Icon rootIcon = IconLoader.findIcon("/icons/zookeeper_small.png");

    public ZkProjectComponent(Project project) {
        this.project = project;
    }

    public static ZkProjectComponent getInstance(Project project) {
        return project.getComponent(ZkProjectComponent.class);
    }

    public void initComponent() {

    }

    public void disposeComponent() {

    }

    @NotNull
    public String getComponentName() {
        return "ZkProjectComponent";
    }

    public void projectOpened() {
        initZk();
        if (this.curator != null) {
            initToolWindow();
        }
    }

    public void initToolWindow() {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).registerToolWindow("ZooKeeper", false, ToolWindowAnchor.LEFT);
        toolWindow.setTitle("ZooKeeper");
        toolWindow.setIcon(rootIcon);
        ZkNode.ROOT_NAME = ZkConfigPersistence.getInstance(project).getTitle();
        zkTree = new Tree(new ZkTreeModel(curator, ZkConfigPersistence.getInstance(project).whitePaths));
        zkTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.installOn(zkTree);
        CustomizationUtil.installPopupHandler(zkTree, "ZK.OperationMenu", ActionPlaces.UNKNOWN);
        zkTree.setCellRenderer(new DefaultTreeRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                if (value instanceof ZkNode && component instanceof WrappingIconPanel) {
                    ZkNode node = (ZkNode) value;
                    if (node.isRoot()) {
                        ((WrappingIconPanel) component).setIcon(rootIcon);
                    } else if (node.isLeaf()) {
                        FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(node.getName());
                        Icon icon = fileType.getIcon();
                        if (fileType.getName().equalsIgnoreCase(FileTypes.UNKNOWN.getName())) {
                            icon = FileTypes.PLAIN_TEXT.getIcon();
                        }
                        if (node.isEphemeral() && icon != null) {
                            icon = IconLoader.getTransparentIcon(icon);
                        }
                        ((WrappingIconPanel) component).setIcon(icon);
                    }
                }

                return component;
            }
        });
        final ContentManager contentManager = toolWindow.getContentManager();
        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(true);
        JBScrollPane jbScrollPane = new JBScrollPane(zkTree);
        panel.add(jbScrollPane);
        panel.setToolbar(createToolBar());
        final Content content = contentManager.getFactory().createContent(panel, null, false);
        contentManager.addContent(content);
    }

    private JComponent createToolBar() {
        ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction("ZK.Toolbar");
        String place = ActionPlaces.EDITOR_TOOLBAR;
        JPanel toolBarPanel = new JPanel(new GridLayout());
        toolBarPanel.add(ActionManager.getInstance().createActionToolbar(place, actionGroup, true).getComponent());
        return toolBarPanel;
    }

    public void projectClosed() {
        if (curator != null) {
            this.curator.close();
        }
    }

    protected boolean onDoubleClick(MouseEvent mouseEvent) {
        Tree source = (Tree) mouseEvent.getSource();
        TreePath treePath = source.getSelectionPath();
        ZkNode selectedNode = (ZkNode) treePath.getLastPathComponent();
        if (selectedNode.isLeaf()) {
            VirtualFile file = fileSystem.findFileByPath(selectedNode.getFilePath());
            if (file != null && project != null) {
                new OpenFileDescriptor(project, file).navigate(true);
            }
        }
        return true;
    }

    public ZkVirtualFileSystem getFileSystem() {
        return fileSystem;
    }

    public Tree getZkTree() {
        return zkTree;
    }

    public void reloadZkTree() {
        ZkNode.ROOT_NAME = ZkConfigPersistence.getInstance(project).getTitle();
        zkTree.setModel(new ZkTreeModel(curator, ZkConfigPersistence.getInstance(project).whitePaths));
        zkTree.updateUI();
    }

    public CuratorFramework getCurator() {
        return curator;
    }

    public void initZk() {
        if (this.curator != null) {
            this.curator.close();
        }
        ZkConfigPersistence config = ZkConfigPersistence.getInstance(project);
        if (config.isAvailable() && ruok(config.getFirstServer())) {
            this.curator = CuratorFrameworkFactory.newClient(config.getZkUrl(), new ExponentialBackoffRetry(1000, 0, 1000));
            curator.start();
            this.fileSystem = new ZkVirtualFileSystem(curator, ZkConfigPersistence.getInstance(project).charset);
        }
    }

    public boolean ruok(String server) {
        try {
            String[] parts = server.split(":");
            Socket sock = new Socket(parts[0], Integer.valueOf(parts[1]));
            sock.getOutputStream().write("ruok".getBytes());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyBytes(sock.getInputStream(), bos, 1000);
            if (!sock.isClosed()) {
                sock.close();
            }
            return "imok".equals(new String(bos.toByteArray()));
        } catch (Exception e) {
            return false;
        }
    }
}
