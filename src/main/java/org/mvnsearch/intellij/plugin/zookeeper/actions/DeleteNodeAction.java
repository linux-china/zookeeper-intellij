package org.mvnsearch.intellij.plugin.zookeeper.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.treeStructure.Tree;
import org.mvnsearch.intellij.plugin.zookeeper.ZkProjectComponent;

import javax.swing.tree.TreePath;

/**
 * delete zk node action
 *
 * @author linux_china
 */
public class DeleteNodeAction extends AnAction {
    public void actionPerformed(AnActionEvent anActionEvent) {
        Tree zkTree = ZkProjectComponent.getInstance(anActionEvent.getProject()).getZkTree();
        TreePath treePath = zkTree.getSelectionPath();
    }
}
