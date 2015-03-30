package org.mvnsearch.intellij.plugin.zookeeper.vfs;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.ex.dummy.DummyFileSystem;
import org.apache.curator.framework.CuratorFramework;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * zookeeper virtual file system
 *
 * @author linux_china
 */
public class ZkVirtualFileSystem extends DummyFileSystem {
    public static final String PROTOCOL = "zk";
    private CuratorFramework curator;
    private Charset charset = Charset.forName("utf-8");

    public ZkVirtualFileSystem(CuratorFramework curator, String charsetName) {
        this.curator = curator;
        if (charsetName == null || charsetName.isEmpty()) {
            charsetName = "utf-8";
        }
        this.charset = Charset.forName(charsetName);
    }

    @NotNull
    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    public Charset getCharset() {
        return this.charset;
    }

    @Nullable
    @Override
    public VirtualFile findFileByPath(@NotNull @NonNls String path) {
        return new ZkNodeVirtualFile(this, path);
    }

    @Override
    public void refresh(boolean b) {

    }

    @Nullable
    @Override
    public VirtualFile refreshAndFindFileByPath(@NotNull String path) {
        return findFileByPath(path);
    }

    @Override
    public void addVirtualFileListener(@NotNull VirtualFileListener virtualFileListener) {

    }

    @Override
    public void removeVirtualFileListener(@NotNull VirtualFileListener virtualFileListener) {

    }

    @Override
    public void deleteFile(Object o, @NotNull VirtualFile virtualFile) throws IOException {
        try {
            getCurator().delete().forPath(virtualFile.getPath());
        } catch (Exception ignore) {

        }
    }

    @Override
    public void moveFile(Object o, @NotNull VirtualFile virtualFile, @NotNull VirtualFile virtualFile2) throws IOException {
        try {
            byte[] content = getCurator().getData().forPath(virtualFile.getPath());
            getCurator().create().forPath(virtualFile2.getPath(), content);
            getCurator().delete().forPath(virtualFile.getPath());
        } catch (Exception ignore) {

        }
    }

    @Override
    public void renameFile(Object o, @NotNull VirtualFile virtualFile, @NotNull String name) throws IOException {
        String newFilePath = virtualFile.getPath().substring(0, virtualFile.getPath().indexOf("/")) + "/" + name;
        moveFile(o, virtualFile, new ZkNodeVirtualFile(this, newFilePath));
    }

    @Override
    public VirtualFile createChildFile(Object o, @NotNull VirtualFile virtualFile, @NotNull String fileName) throws IOException {
        String filePath = virtualFile.getPath() + "/" + fileName;
        try {
            getCurator().create().forPath(filePath);
        } catch (Exception ignore) {

        }
        return new ZkNodeVirtualFile(this, filePath);
    }

    @NotNull
    @Override
    public VirtualFile createChildDirectory(Object o, @NotNull VirtualFile virtualFile, @NotNull String directory) throws IOException {
        String filePath = virtualFile.getPath() + "/" + directory;
        try {
            getCurator().create().forPath(filePath);
        } catch (Exception ignore) {

        }
        return new ZkNodeVirtualFile(this, filePath);
    }

    @Override
    public VirtualFile copyFile(Object o, @NotNull VirtualFile virtualFile, @NotNull VirtualFile virtualFile2, @NotNull String s) throws IOException {
        try {
            //todo
        } catch (Exception ignore) {

        }
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    public CuratorFramework getCurator() {
        return curator;
    }
}
