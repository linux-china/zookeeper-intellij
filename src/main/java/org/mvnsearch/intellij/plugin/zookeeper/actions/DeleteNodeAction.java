package org.mvnsearch.intellij.plugin.zookeeper.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.treeStructure.Tree;
import org.apache.curator.framework.CuratorFramework;
import org.jetbrains.generate.tostring.util.StringUtil;
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
        final ZkProjectComponent zkProjectComponent = ZkProjectComponent.getInstance(anActionEvent.getProject());
        Tree zkTree = zkProjectComponent.getZkTree();
        TreePath treePath = zkTree.getSelectionPath();
        final ZkNode currentNode = (ZkNode) treePath.getLastPathComponent();
        final DialogBuilder builder = new DialogBuilder(anActionEvent.getProject());
        builder.setTitle("Delete Node");
        final JLabel jTextField = new JLabel("Path: " + currentNode.getFilePath());
        builder.setCenterPanel(jTextField);
        builder.setOkOperation(new Runnable() {
            public void run() {
                CuratorFramework curator = zkProjectComponent.getCurator();
                try {
                    curator.delete().forPath(currentNode.getFilePath());
                    zkProjectComponent.reloadzkTree();
                } catch (Exception ignore) {

                }
                builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
            }
        });
        builder.showModal(true);
    }
}
