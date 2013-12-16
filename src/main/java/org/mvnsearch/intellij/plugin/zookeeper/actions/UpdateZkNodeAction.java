package org.mvnsearch.intellij.plugin.zookeeper.actions;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.mvnsearch.intellij.plugin.zookeeper.ZkProjectComponent;
import org.mvnsearch.intellij.plugin.zookeeper.vfs.ZkNodeVirtualFile;

/**
 * update zoo keeper node action
 *
 * @author linux_china
 */
public class UpdateZkNodeAction extends EditorAction {

    public UpdateZkNodeAction() {
        super(new EditorActionHandler() {
            @Override
            public void execute(Editor editor, DataContext dataContext) {
                VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
                if (virtualFile != null && virtualFile instanceof ZkNodeVirtualFile) {
                    ZkNodeVirtualFile nodeFile = (ZkNodeVirtualFile) virtualFile;
                    String nodeContent = editor.getDocument().getText();
                    Project project = CommonDataKeys.PROJECT.getData(dataContext);
                    ZkProjectComponent zkProjectComponent = ZkProjectComponent.getInstance(project);
                    try {
                        zkProjectComponent.getCurator().setData().forPath(nodeFile.getFilePath(), nodeContent.getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public boolean isEnabled(Editor editor, DataContext dataContext) {
                VirtualFile virtualFile = DataKeys.VIRTUAL_FILE.getData(dataContext);
                return virtualFile instanceof ZkNodeVirtualFile;
            }
        });
    }
}
