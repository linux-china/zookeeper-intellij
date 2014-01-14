package org.mvnsearch.intellij.plugin.zookeeper.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.treeStructure.Tree;
import org.mvnsearch.intellij.plugin.zookeeper.ZkProjectComponent;

import javax.swing.tree.TreePath;

/**
 * Refresh Zoo Keeper Tree
 *
 * @author linux_china
 */
public class ZkTreeRefreshAction extends AnAction {
    public void actionPerformed(AnActionEvent anActionEvent) {
        ZkProjectComponent zkProjectComponent = ZkProjectComponent.getInstance(anActionEvent.getProject());
        Tree zkTree = zkProjectComponent.getZkTree();
        TreePath treePath = zkTree.getSelectionPath();
        zkTree.updateUI();
        if (treePath != null) {
            zkTree.expandPath(treePath);
        }

    }
}
