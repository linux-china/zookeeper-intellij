package org.mvnsearch.intellij.plugin.zookeeper;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * zookeeper configuration persistence
 *
 * @author linux_china
 */
@State(name = "ZooKeeperConfig", storages = {@Storage(id = "main", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/zookeeper_config.xml")})
public class ZkConfigPersistence implements PersistentStateComponent<ZkConfigPersistence> {
    public String host;
    public Integer port;
    public String charset;
    public String whitePaths;
    public boolean enabled;
    public boolean tooltip;

    public static ZkConfigPersistence getInstance(Project project) {
        return ServiceManager.getService(project, ZkConfigPersistence.class);
    }

    @Nullable
    @Override
    public ZkConfigPersistence getState() {
        return this;
    }

    @Override
    public void loadState(ZkConfigPersistence state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public boolean isAvailable() {
        return enabled && StringUtil.isNotEmpty(host);
    }

    public String getZkUrl() {
        if (host.contains(":")) {
            return host;
        } else if (host.contains(",")) {
            return host.replaceAll("[\\s,]+", ":" + port + ",");
        } else {
            return host + ":" + port;
        }
    }

    public String getFirstServer() {
        String zkUrl = getZkUrl();
        if (zkUrl.contains(",")) {
            return zkUrl.substring(0, zkUrl.indexOf(","));
        }
        return zkUrl;
    }

    public String getTitle() {
        String zkUrl = getZkUrl();
        if (zkUrl.contains(",")) {
            return zkUrl.substring(0, zkUrl.indexOf(","));
        }
        return zkUrl;
    }

}
