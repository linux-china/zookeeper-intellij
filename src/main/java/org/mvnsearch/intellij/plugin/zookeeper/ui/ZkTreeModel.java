package org.mvnsearch.intellij.plugin.zookeeper.ui;

import com.intellij.openapi.util.text.StringUtil;
import org.apache.curator.framework.CuratorFramework;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.*;

/**
 * zoo keeper tree model
 *
 * @author linux_china
 */
public class ZkTreeModel implements TreeModel {
    private ZkNode root = new ZkNode("/", null);
    private CuratorFramework curator;
    private List<String> whitePaths;

    public ZkTreeModel(CuratorFramework curator, String whitePaths) {
        this.curator = curator;
        if (StringUtil.isNotEmpty(whitePaths)) {
            this.whitePaths = Arrays.asList(whitePaths.trim().split("[\\s;]+"));
        }
    }

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int i) {
        List<ZkNode> children = getChildren((ZkNode) parent);
        return children.get(i);
    }

    public int getChildCount(Object parent) {
        return getChildren((ZkNode) parent).size();
    }

    public boolean isLeaf(Object node) {
        return getChildren((ZkNode) node).size() == 0;
    }

    public void valueForPathChanged(TreePath treePath, Object o) {

    }

    public int getIndexOfChild(Object parent, Object node) {
        List<ZkNode> children = getChildren((ZkNode) parent);
        for (int i = 0; i < children.size(); i++) {
            if (((ZkNode) node).getFilePath().equals(children.get(i).getFilePath())) {
                return i;
            }
        }
        return -1;
    }

    public void addTreeModelListener(TreeModelListener treeModelListener) {

    }

    public void removeTreeModelListener(TreeModelListener treeModelListener) {

    }

    public List<ZkNode> getChildren(ZkNode node) {
        List<ZkNode> children = new ArrayList<ZkNode>();
        try {
            List<String> nodes = curator.getChildren().forPath(node.getFilePath());
            if (nodes.isEmpty()) {
                node.setLeaf(true);
            } else {
                Collections.sort(nodes, new Comparator<String>() {
                    public int compare(String s, String s2) {
                        return s.compareTo(s2);
                    }
                });
                for (int i = 0; i < nodes.size() && i < 100; i++) {
                    ZkNode zkNode = new ZkNode(node.getFilePath(), nodes.get(i));
                    if (isWhitePath(zkNode.getFilePath())) {
                        children.add(zkNode);
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return children;
    }

    private boolean isWhitePath(String filePath) {
        if (this.whitePaths != null) {
            boolean legal = false;
            for (String whitePath : whitePaths) {
                if (filePath.startsWith(whitePath)) {
                    legal = true;
                    break;
                } else if (whitePath.lastIndexOf("/") > 1 && whitePath.startsWith(filePath)) {
                    legal = true;
                    break;
                }
            }
            return legal;
        }
        return true;
    }
}