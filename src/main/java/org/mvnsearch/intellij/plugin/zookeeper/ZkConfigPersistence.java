package org.mvnsearch.intellij.plugin.zookeeper;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    public static ZkConfigPersistence getInstance(Project project) {
        return ServiceManager.getService(project, ZkConfigPersistence.class);
    }

    @Nullable
    public ZkConfigPersistence getState() {
        return this;
    }

    public void loadState(ZkConfigPersistence state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public boolean isAvailable() {
        return enabled && StringUtil.isNotEmpty(host);
    }

    public String getZkUrl() {
        if (host.contains(":")) {
            return host;
        } else if (host.contains(";") || host.contains(",")) {
            return host.replace("[\\s;]+", ":" + port + ",");
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
