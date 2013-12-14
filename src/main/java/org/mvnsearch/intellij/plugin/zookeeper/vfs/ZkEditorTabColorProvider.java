package org.mvnsearch.intellij.plugin.zookeeper.vfs;

import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * zoo keeper editor tab color provider
 *
 * @author linux_china
 */
public class ZkEditorTabColorProvider implements EditorTabColorProvider {
    @Nullable
    public Color getEditorTabColor(Project project, VirtualFile virtualFile) {
        if (virtualFile instanceof ZkNodeVirtualFile) {
            return JBColor.ORANGE;
        } else {
            return JBColor.RED;
        }
    }
}
