package org.mvnsearch.intellij.plugin.zookeeper.ui;

import com.intellij.openapi.fileTypes.FileTypes;
import org.apache.zookeeper.data.Stat;

import java.util.Arrays;
import java.util.Date;

/**
 * ZooKeeper Node
 *
 * @author linux_china
 */
public class ZkNode {
    private static java.util.List<String> binaryExtNames = Arrays.asList("pb", "bin", "msgpack");

    public static String ROOT_NAME = "/";
    private String path;
    private String name;
    private Stat stat;

    public ZkNode(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public boolean isLeaf() {
        return stat != null && stat.getNumChildren() == 0;
    }

    public boolean isRoot() {
        return path.equals("/") && name == null;
    }

    public boolean isEphemeral() {
        return stat != null && stat.getEphemeralOwner() > 0;
    }

    public int getChildrenCount() {
        return stat != null ? stat.getNumChildren() : 0;
    }

    public boolean isFilled() {
        return stat != null;
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

    public Stat getStat() {
        return stat;
    }

    public void setStat(Stat stat) {
        this.stat = stat;
    }

    @Override
    public String toString() {
        return name == null ? ROOT_NAME : name;
    }

    public boolean isBinary() {
        String extName = null;
        if (name.contains(".")) {
            extName = name.substring(name.lastIndexOf(".") + 1);
        }
        return extName != null && binaryExtNames.contains(extName.toLowerCase());
    }

    public String getTooltip() {
        return "cZxid = " + stat.getCzxid() + "\n" +
                "ctime = " + new Date(stat.getCtime()) + "\n" +
                "mZxid = " + stat.getMzxid() + "\n" +
                "mtime = " + new Date(stat.getMtime()) + "\n" +
                "pZxid = " + stat.getPzxid() + "\n" +
                "cversion = " + stat.getCversion() + "\n" +
                "dataVersion = " + stat.getVersion() + "\n" +
                "aclVersion = " + stat.getAversion() + "\n" +
                "ephemeralOwner = " + stat.getEphemeralOwner() + "\n" +
                "dataLength =" + stat.getDataLength() + " \n" +
                "numChildren = " + stat.getNumChildren();
    }
}
