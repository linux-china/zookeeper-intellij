package org.mvnsearch.intellij.plugin.zookeeper;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.treeStructure.Tree;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.WrappingIconPanel;
import org.jetbrains.annotations.NotNull;
import org.mvnsearch.intellij.plugin.zookeeper.ui.ZkNode;
import org.mvnsearch.intellij.plugin.zookeeper.ui.ZkTreeModel;
import org.mvnsearch.intellij.plugin.zookeeper.vfs.ZkVirtualFileSystem;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Zoo Keeper project component
 *
 * @author linux_china
 */
public class ZkProjectComponent extends MouseAdapter implements ProjectComponent {
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
            this.fileSystem = new ZkVirtualFileSystem(curator);
            initToolWindow();
        }
    }

    public void initToolWindow() {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).registerToolWindow("ZooKeeper", false, ToolWindowAnchor.LEFT);
        toolWindow.setTitle("ZooKeeper");
        toolWindow.setIcon(rootIcon);
        ZkNode.ROOT_NAME = ZkConfigPersistence.getInstance(project).getUrl();
        zkTree = new Tree(new ZkTreeModel(curator));
        zkTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        zkTree.addMouseListener(this);
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
                        ((WrappingIconPanel) component).setIcon(fileType.getIcon());
                    }
                }

                return component;
            }
        });

        final ContentManager contentManager = toolWindow.getContentManager();
        JBScrollPane jbScrollPane = new JBScrollPane(zkTree);
        final Content content = contentManager.getFactory().createContent(jbScrollPane, null, false);
        contentManager.addContent(content);
    }

    public void projectClosed() {
        if (curator != null) {
            this.curator.close();
        }
    }


    public void mouseClicked(MouseEvent mouseEvent) {
        Tree source = (Tree) mouseEvent.getSource();
        if (mouseEvent.getButton() == 3) {
            ActionGroup operations = (ActionGroup) ActionManager.getInstance().getAction("ZK.OperationMenu");
            final DataContext context = DataManager.getInstance().getDataContext(source);
            ListPopup listPopup = JBPopupFactory.getInstance().createActionGroupPopup("ZooKeeper Operations", operations,
                    context, JBPopupFactory.ActionSelectionAid.MNEMONICS, true);
            listPopup.show(new RelativePoint(mouseEvent));
        } else if (mouseEvent.getClickCount() == 2) {
            TreePath treePath = source.getSelectionPath();
            ZkNode selectedNode = (ZkNode) treePath.getLastPathComponent();
            if (selectedNode.isLeaf()) {
                VirtualFile file = fileSystem.findFileByPath(selectedNode.getFilePath());
                if (file != null && project != null) {
                    new OpenFileDescriptor(project, file).navigate(true);
                }
            }
        }
    }

    public Tree getZkTree() {
        return zkTree;
    }

    private void initZk() {
        if (this.curator != null) {
            this.curator.close();
        }
        ZkConfigPersistence config = ZkConfigPersistence.getInstance(project);
        if (config.isAvailable()) {
            this.curator = CuratorFrameworkFactory.newClient(config.getUrl(), new ExponentialBackoffRetry(1000, 1));
            curator.start();
        }
    }
}
