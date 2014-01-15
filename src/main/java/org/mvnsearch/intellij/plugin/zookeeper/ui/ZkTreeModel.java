package org.mvnsearch.intellij.plugin.zookeeper.ui;

import com.intellij.openapi.util.text.StringUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;

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
        ZkNode zkNode = (ZkNode) parent;
        if (!zkNode.isFilled()) {
            fillZkNode(zkNode);
        }
        return zkNode.getChildrenCount();
    }

    public boolean isLeaf(Object node) {
        ZkNode zkNode = (ZkNode) node;
        if (!zkNode.isFilled()) {
            fillZkNode(zkNode);
        }
        return zkNode.isLeaf();
    }

    private void fillZkNode(ZkNode zkNode) {
        try {
            Stat stat = curator.checkExists().forPath(zkNode.getFilePath());
            if (stat != null) {
                zkNode.setStat(stat);
            }
        } catch (Exception ignore) {

        }
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
        if (!node.isFilled()) {
            fillZkNode(node);
        }
        if (node.isLeaf()) {
            return children;
        }
        try {
            List<String> nodes = curator.getChildren().forPath(node.getFilePath());
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