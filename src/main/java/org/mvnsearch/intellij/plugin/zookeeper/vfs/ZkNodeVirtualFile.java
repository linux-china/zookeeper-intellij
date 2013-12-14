package org.mvnsearch.intellij.plugin.zookeeper.vfs;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.util.LocalTimeCounter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mvnsearch.intellij.plugin.zookeeper.ZkApplicationComponent;

import java.io.*;
import java.util.List;

/**
 * ZooKeeper node virtual file
 *
 * @author linux_china
 */
public class ZkNodeVirtualFile extends VirtualFile {
    private String filePath;
    private String fileName;
    private boolean isLeaf;
    private Stat stat;
    private byte[] content;
    private VirtualFileListener fileListener = null;
    private final long myTimeStamp = System.currentTimeMillis();
    private long myModStamp = LocalTimeCounter.currentTime();

    public ZkNodeVirtualFile(String filePath) {
        this.filePath = filePath;
        fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        this.stat = new Stat();
        try {
            this.content = getCurator().getData().storingStatIn(stat).forPath(filePath);
            this.isLeaf = getCurator().getChildren().forPath(filePath).isEmpty();
        } catch (Exception ignore) {

        }
    }

    public void setFileListener(VirtualFileListener fileListener) {
        this.fileListener = fileListener;
    }

    @NotNull
    public String getName() {
        return this.fileName;
    }

    @NotNull
    public VirtualFileSystem getFileSystem() {
        return ZkVirtualFileSystem.getInstance();
    }

    public String getPath() {
        String path = "/";
        if (filePath.lastIndexOf("/") > 0) {
            path = filePath.substring(0, filePath.lastIndexOf("/"));
        }
        return path;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isWritable() {
        return true;
    }

    public boolean isDirectory() {
        return !isLeaf;
    }

    public boolean isValid() {
        return true;
    }

    public VirtualFile getParent() {
        return null;
    }

    public VirtualFile[] getChildren() {
        try {
            List<String> children = getCurator().getChildren().forPath(this.getPath());
            if (children != null && !children.isEmpty()) {
                VirtualFile[] files = new VirtualFile[children.size()];
                for (int i = 0; i < children.size(); i++) {
                    files[i] = new ZkNodeVirtualFile(children.get(i));
                }
                return files;
            }
        } catch (Exception ignore) {

        }
        return new VirtualFile[0];
    }

    @NotNull
    public OutputStream getOutputStream(final Object requestor, final long newModificationStamp, long newTimeStamp) throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() {
                setContent(requestor, toByteArray(), newModificationStamp);
            }
        };
    }

    @NotNull
    public byte[] contentsToByteArray() throws IOException {
        return this.content;
    }

    public long getTimeStamp() {
        return myModStamp;
    }

    @Override
    public long getModificationStamp() {
        return myTimeStamp;
    }

    public long getLength() {
        return this.content.length;
    }

    public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {

    }

    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException("Method getInputStream is not yet implemented in " + getClass().getName());
    }

    public void setContent(@Nullable Object requestor, byte[] content, long newModificationStamp) {
        long oldModstamp = myModStamp;
        myModStamp = newModificationStamp;
        this.content = content;
        try {
            getCurator().setData().forPath(this.filePath, content);
        } catch (Exception ignore) {

        }
        if (fileListener != null) {
            fileListener.contentsChanged(new VirtualFileEvent(requestor, this, null, oldModstamp, myModStamp));
        }
    }

    public CuratorFramework getCurator() {
        return ZkApplicationComponent.getInstance().getCurator();
    }

    public boolean equals(Object obj) {
        return obj instanceof ZkNodeVirtualFile && ((ZkNodeVirtualFile) obj).getFilePath().equals(filePath);
    }
}
