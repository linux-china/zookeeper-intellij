package org.mvnsearch.intellij.plugin.zookeeper.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;
import org.apache.curator.framework.CuratorFramework;
import org.mvnsearch.intellij.plugin.zookeeper.ZkProjectComponent;
import org.mvnsearch.intellij.plugin.zookeeper.ui.ZkNode;
import org.mvnsearch.intellij.plugin.zookeeper.vfs.ZkNodeVirtualFile;

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
        builder.setPreferredFocusComponent(jTextField);
        builder.setCenterPanel(jTextField);
        builder.setOkOperation(new Runnable() {
            public void run() {
                String nodeName = jTextField.getText();
                if (StringUtil.isNotEmpty(nodeName)) {
                    if (nodeName.startsWith("/")) {
                        nodeName = nodeName.substring(1);
                    }
                    if (nodeName.endsWith("/")) {
                        nodeName = nodeName.substring(0, nodeName.length() - 1);
                    }
                    ZkProjectComponent zkProjectComponent = ZkProjectComponent.getInstance(anActionEvent.getProject());
                    Tree zkTree = zkProjectComponent.getZkTree();
                    TreePath treePath = zkTree.getSelectionPath();
                    CuratorFramework curator = zkProjectComponent.getCurator();
                    ZkNode currentNode = (ZkNode) treePath.getLastPathComponent();
                    try {
                        //create recursively support
                        String[] parts = nodeName.split("/");
                        ZkNode newNode = null;
                        for (String part : parts) {
                            newNode = currentNode.getSubNode(part);
                            // check exists
                            if (curator.checkExists().forPath(newNode.getFilePath()) == null) {
                                if (nodeName.endsWith(".zip")) {
                                    String entryName = nodeName.replace(".zip", "");
                                    curator.create().forPath(newNode.getFilePath(), ZkNodeVirtualFile.zip(entryName, "".getBytes()));
                                } else {
                                    curator.create().forPath(newNode.getFilePath(), "".getBytes());
                                }
                            }
                            currentNode = newNode;
                        }
                        zkTree.updateUI();
                        zkTree.expandPath(treePath);
                        if (newNode != null) {
                            FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(nodeName);
                            if (!fileType.getName().equals(FileTypes.UNKNOWN.getName())) {
                                VirtualFile virtualFile = zkProjectComponent.getFileSystem().findFileByPath(newNode.getFilePath());
                                if (anActionEvent.getProject() != null && virtualFile != null) {
                                    new OpenFileDescriptor(anActionEvent.getProject(), virtualFile).navigate(true);
                                }
                            }
                        }
                    } catch (Exception ignore) {
                        ignore.printStackTrace();
                    }
                }
                builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
            }
        });
        builder.showModal(true);
    }
}
