package org.mvnsearch.intellij.plugin.zookeeper.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.mvnsearch.intellij.plugin.zookeeper.ZkProjectComponent;

/**
 * Refresh Zoo Keeper Tree
 *
 * @author linux_china
 */
public class ZkTreeRefreshAction extends AnAction {
    public void actionPerformed(AnActionEvent anActionEvent) {
        ZkProjectComponent zkProjectComponent = ZkProjectComponent.getInstance(anActionEvent.getProject());
        zkProjectComponent.getZkTree().updateUI();
    }
}
