package org.mvnsearch.intellij.plugin.zookeeper.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.impl.status.StatusBarUtil;
import com.intellij.ui.treeStructure.Tree;
import org.apache.curator.framework.CuratorFramework;
import org.mvnsearch.intellij.plugin.zookeeper.ZkProjectComponent;
import org.mvnsearch.intellij.plugin.zookeeper.ui.ZkNode;

import javax.swing.*;
import javax.swing.tree.TreePath;

/**
 * delete zk node action
 *
 * @author linux_china
 */
public class DeleteNodeAction extends AnAction {
    public void actionPerformed(final AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getProject();
        final ZkProjectComponent zkProjectComponent = ZkProjectComponent.getInstance(project);
        final Tree zkTree = zkProjectComponent.getZkTree();
        TreePath treePath = zkTree.getSelectionPath();
        final ZkNode currentNode = (ZkNode) treePath.getLastPathComponent();
        final DialogBuilder builder = new DialogBuilder(project);
        builder.setTitle("Delete Node");
        final JLabel jTextField = new JLabel("Path: " + currentNode.getFilePath());
        builder.setCenterPanel(jTextField);
        builder.setOkOperation(new Runnable() {
            public void run() {
                CuratorFramework curator = zkProjectComponent.getCurator();
                try {
                    curator.delete().deletingChildrenIfNeeded().forPath(currentNode.getFilePath());
                    zkTree.updateUI();
                } catch (Exception ignore) {

                }
                builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
                StatusBarUtil.setStatusBarInfo(project, "'" + currentNode.getFilePath() + "' has been deleted!");
            }
        });
        builder.showModal(true);
    }
}
