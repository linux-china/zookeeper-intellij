package org.mvnsearch.intellij.plugin.zookeeper;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.jetbrains.annotations.NotNull;

/**
 * ZooKeeper application component
 *
 * @author linux_china
 */
public class ZkApplicationComponent implements ApplicationComponent {
    private CuratorFramework curator;

    public ZkApplicationComponent() {
    }

    public static ZkApplicationComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(ZkApplicationComponent.class);
    }

    public CuratorFramework getCurator() {
        return this.curator;
    }

    public void resetZkConfig() {
        initZk();
    }

    public void initComponent() {
        initZk();
    }

    public void disposeComponent() {
        this.curator.close();
    }

    private void initZk() {
        if (this.curator != null) {
            this.curator.close();
        }
        ZkConfigPersistence config = ZkConfigPersistence.getInstance();
        if (config.isAvailable()) {
            this.curator = CuratorFrameworkFactory.newClient(config.getUrl(), new ExponentialBackoffRetry(1000, 1));
            curator.start();
        }
    }

    @NotNull
    public String getComponentName() {
        return "ZkApplicationComponent";
    }
}
