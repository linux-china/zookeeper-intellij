package org.mvnsearch.intellij.plugin.zookeeper.ui;

/**
 * ZooKeeper Node
 *
 * @author linux_china
 */
public class ZkNode {
    public static String ROOT_NAME = "/";
    private String path;
    private String name;
    private boolean ephemeral;
    private int childrenCount;
    private boolean isLeaf;
    private boolean filled;

    public ZkNode(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public boolean isRoot() {
        return path.equals("/") && name == null;
    }

    public void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public boolean isFilled() {
        return filled;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        if (name == null) {
            return path;
        } else {
            if (path.endsWith("/")) {
                return path + name;
            } else {
                return path + "/" + name;
            }
        }
    }

    public ZkNode getSubNode(String subNodeName) {
        return new ZkNode(getFilePath(), subNodeName);
    }

    @Override
    public String toString() {
        return name == null ? ROOT_NAME : name;
    }
}
