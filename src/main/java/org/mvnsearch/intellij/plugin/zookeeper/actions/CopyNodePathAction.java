package org.mvnsearch.intellij.plugin.zookeeper.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.treeStructure.Tree;
import org.mvnsearch.intellij.plugin.zookeeper.ZkProjectComponent;
import org.mvnsearch.intellij.plugin.zookeeper.ui.ZkNode;

import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

/**
 * copy zk node path action
 *
 * @author linux_china
 */
public class CopyNodePathAction extends AnAction implements ClipboardOwner {
    public void actionPerformed(final AnActionEvent anActionEvent) {
        ZkProjectComponent zkProjectComponent = ZkProjectComponent.getInstance(anActionEvent.getProject());
        Tree zkTree = zkProjectComponent.getZkTree();
        TreePath treePath = zkTree.getSelectionPath();
        ZkNode currentNode = (ZkNode) treePath.getLastPathComponent();
        if (currentNode != null) {
            StringSelection stringSelection = new StringSelection(currentNode.getFilePath());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, this);
        }
    }

    public void lostOwnership(Clipboard clipboard, Transferable transferable) {

    }
}
