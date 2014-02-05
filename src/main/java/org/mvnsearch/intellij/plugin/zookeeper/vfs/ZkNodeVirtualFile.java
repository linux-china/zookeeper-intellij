package org.mvnsearch.intellij.plugin.zookeeper.vfs;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.util.LocalTimeCounter;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * ZooKeeper node virtual file
 *
 * @author linux_china
 */
public class ZkNodeVirtualFile extends VirtualFile {
    private ZkVirtualFileSystem fileSystem;
    private String filePath;
    private String fileName;
    private boolean isLeaf;
    private Stat stat;
    private byte[] content;
    private VirtualFileListener fileListener = null;
    private final long myTimeStamp = System.currentTimeMillis();
    private long myModStamp = LocalTimeCounter.currentTime();

    public ZkNodeVirtualFile(ZkVirtualFileSystem fileSystem, String filePath) {
        this.fileSystem = fileSystem;
        this.filePath = filePath;
        if (!filePath.equals("/") && filePath.endsWith("/")) {
            this.filePath = filePath.substring(0, filePath.length() - 1);
        }
        fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        try {
            this.stat = getCurator().checkExists().forPath(filePath);
            this.isLeaf = stat.getNumChildren() == 0;
        } catch (Exception ignore) {

        }
    }

    public void setLeaf() {
        this.isLeaf = true;
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
        return this.fileSystem;
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
        if ("/".equals(filePath)) {
            return null;
        }
        int slashIndex = filePath.lastIndexOf("/");
        if (slashIndex == 0) {
            return new ZkNodeVirtualFile(this.fileSystem, "/");
        } else {
            String parentPath = filePath.substring(0, slashIndex);
            return new ZkNodeVirtualFile(this.fileSystem, parentPath);
        }
    }

    public VirtualFile[] getChildren() {
        try {
            List<String> children = getCurator().getChildren().forPath(filePath);
            if (children != null && !children.isEmpty()) {
                VirtualFile[] files = new VirtualFile[children.size()];
                for (int i = 0; i < children.size(); i++) {
                    String childName = children.get(i);
                    files[i] = new ZkNodeVirtualFile(fileSystem, filePath.endsWith("/") ? filePath + childName : filePath + "/" + childName);
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
                // disable save to update node operation
                //setContent(requestor, toByteArray(), newModificationStamp);
            }
        };
    }

    @NotNull
    public byte[] contentsToByteArray() throws IOException {
        checkContent();
        return this.content;
    }

    public void checkContent() {
        if (content == null) {
            try {
                this.content = getCurator().getData().storingStatIn(stat).forPath(filePath);
                if (isSingleFileZip()) {
                    this.content = unzip(content);
                }
            } catch (Exception ignore) {

            }
            if (this.content == null) {
                content = "".getBytes();
            }
        }
    }

    public long getTimeStamp() {
        return myModStamp;
    }

    @Override
    public long getModificationStamp() {
        return myTimeStamp;
    }

    public long getLength() {
        checkContent();
        return this.content.length;
    }

    public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {

    }

    public InputStream getInputStream() throws IOException {
        checkContent();
        return new ByteArrayInputStream(content);
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

    @Override
    public Charset getCharset() {
        return fileSystem.getCharset();
    }

    @NotNull
    public FileType getFileType() {
        String newFileName = this.fileName;
        if (isSingleFileZip()) {
            newFileName = newFileName.replace(".zip", "");
        }
        FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(newFileName);
        if (fileType.getName().equalsIgnoreCase(FileTypes.UNKNOWN.getName())) {
            return FileTypes.PLAIN_TEXT;
        }
        return fileType;
    }

    public CuratorFramework getCurator() {
        return fileSystem.getCurator();
    }

    public boolean equals(Object obj) {
        return obj instanceof ZkNodeVirtualFile && ((ZkNodeVirtualFile) obj).getFilePath().equals(filePath);
    }

    @Override
    public String toString() {
        return this.filePath;
    }

    public boolean isSingleFileZip() {
        return isLeaf && fileName.endsWith(".zip") && fileName.replace(".zip", "").contains(".");
    }

    public static byte[] unzip(byte[] zipContent) throws Exception {
        File tempFile = File.createTempFile("zk_node_11", "zip");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(zipContent);
        fos.close();
        ZipFile zipFile = new ZipFile(tempFile);
        ZipEntry entry = zipFile.entries().nextElement();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(zipFile.getInputStream(entry), bos);
        zipFile.close();
        tempFile.delete();
        return bos.toByteArray();
    }

    public static byte[] zip(String name, byte[] content) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zipOutput = new ZipOutputStream(bos);
        ZipEntry entry = new ZipEntry(name);
        entry.setSize(content.length);
        zipOutput.putNextEntry(entry);
        zipOutput.write(content);
        zipOutput.closeEntry();
        zipOutput.close();
        return bos.toByteArray();
    }
}
