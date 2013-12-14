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
 * create zk node action
 *
 * @author linux_china
 */
public class CreateNodeAction extends AnAction {
    public void actionPerformed(final AnActionEvent anActionEvent) {
        final DialogBuilder builder = new DialogBuilder(anActionEvent.getProject());
        builder.setTitle("Create Node");
        final JTextField jTextField = new JTextField();
        builder.setCenterPanel(jTextField);
        builder.setOkOperation(new Runnable() {
            public void run() {
                String nodeName = jTextField.getText();
                ZkProjectComponent zkProjectComponent = ZkProjectComponent.getInstance(anActionEvent.getProject());
                if (StringUtil.isNotEmpty(jTextField.getText())) {
                    Tree zkTree = zkProjectComponent.getZkTree();
                    TreePath treePath = zkTree.getSelectionPath();
                    CuratorFramework curator = zkProjectComponent.getCurator();
                    ZkNode currentNode = (ZkNode) treePath.getLastPathComponent();
                    try {
                        curator.create().forPath(currentNode.getSubNode(nodeName).getFilePath(), new byte[0]);
                    } catch (Exception ignore) {
                        ignore.printStackTrace();
                    }
                    builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
                }
            }
        });
        builder.showModal(true);
    }
}
