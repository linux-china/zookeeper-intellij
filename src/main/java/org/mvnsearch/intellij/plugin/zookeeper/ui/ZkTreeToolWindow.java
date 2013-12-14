package org.mvnsearch.intellij.plugin.zookeeper.ui;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.treeStructure.Tree;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.WrappingIconPanel;
import org.mvnsearch.intellij.plugin.zookeeper.ZkApplicationComponent;
import org.mvnsearch.intellij.plugin.zookeeper.ZkConfigPersistence;
import org.mvnsearch.intellij.plugin.zookeeper.vfs.ZkVirtualFileSystem;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * ZooKeeper Tree tool window
 *
 * @author linux_china
 */
public class ZkTreeToolWindow implements ToolWindowFactory, MouseListener {
    private Project project;
    private final Icon rootIcon = IconLoader.findIcon("/icons/zookeeper_small.png");

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        this.project = project;
        toolWindow.setTitle("ZooKeeper");
        Tree zkTree;
        if (ZkConfigPersistence.getInstance().isAvailable()) {
            ZkNode.ROOT_NAME = ZkConfigPersistence.getInstance().getUrl();
            zkTree = new Tree(new ZkTreeModel());
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
        } else {
            zkTree = new Tree(new DefaultMutableTreeNode("No ZooKeeper"));
        }
        final ContentManager contentManager = toolWindow.getContentManager();
        JBScrollPane jbScrollPane = new JBScrollPane(zkTree);
        final Content content = contentManager.getFactory().createContent(jbScrollPane, null, false);
        contentManager.addContent(content);
    }

    public void mouseClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            Tree source = (Tree) mouseEvent.getSource();
            TreePath treePath = source.getSelectionPath();
            ZkNode selectedNode = (ZkNode) treePath.getLastPathComponent();
            if (selectedNode.isLeaf()) {
                ZkVirtualFileSystem fileSystem = ZkApplicationComponent.getInstance().getFileSystem();
                VirtualFile file = fileSystem.findFileByPath(selectedNode.getFilePath());
                if (file != null && project != null) {
                    new OpenFileDescriptor(project, file).navigate(true);
                }
            }
        }
    }

    public void mousePressed(MouseEvent mouseEvent) {

    }

    public void mouseReleased(MouseEvent mouseEvent) {

    }

    public void mouseEntered(MouseEvent mouseEvent) {

    }

    public void mouseExited(MouseEvent mouseEvent) {

    }
}
