package org.mvnsearch.intellij.plugin.zookeeper.ui;

import org.apache.curator.framework.CuratorFramework;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * zoo keeper tree model
 *
 * @author linux_china
 */
public class ZkTreeModel implements TreeModel {
    private ZkNode root = new ZkNode("/", null);
    private CuratorFramework curator;

    public ZkTreeModel(CuratorFramework curator) {
        this.curator = curator;
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
            for (String temp : nodes) {
                children.add(new ZkNode(node.getFilePath(), temp));
            }
            if (nodes.isEmpty()) {
                node.setLeaf(true);
            }
        } catch (Exception ignore) {
        }
        return children;
    }
}