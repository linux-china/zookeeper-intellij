package org.mvnsearch.intellij.plugin.zookeeper.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.mvnsearch.intellij.plugin.zookeeper.ZkConfigPersistence;
import org.mvnsearch.intellij.plugin.zookeeper.ZkProjectComponent;

import javax.swing.*;

/**
 * Zoo Keeper application configurable
 *
 * @author linux_china
 */
public class ZkProjectConfigurable implements Configurable {
    private Project project;
    private JPanel root;
    private JTextField hostTextField;
    private JTextField portTextField;
    private JCheckBox enableZooKeeperCheckBox;
    private ZkConfigPersistence config;

    public ZkProjectConfigurable(Project project) {
        this.project = project;
        this.config = ZkConfigPersistence.getInstance(project);
        reset();
    }

    @Nls
    public String getDisplayName() {
        return "ZooKeeper";
    }

    @Nullable
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    public JComponent createComponent() {
        return root;
    }

    public boolean isModified() {
        String newHost = hostTextField.getText().trim();
        String newPort = portTextField.getText().trim();
        return !(newHost.equals(config.host) && Integer.valueOf(newPort).equals(config.port)
                && config.enabled == enableZooKeeperCheckBox.isSelected());
    }

    public void apply() throws ConfigurationException {
        config.host = hostTextField.getText().trim();
        config.port = Integer.valueOf(portTextField.getText().trim());
        boolean oldEnabled = config.enabled;
        config.enabled = enableZooKeeperCheckBox.isSelected();
        if (!oldEnabled && config.enabled) {
            ZkProjectComponent zkProjectComponent = ZkProjectComponent.getInstance(project);
            zkProjectComponent.initZk();
            if (ToolWindowManager.getInstance(project).getToolWindow("ZooKeeper") == null) {
                zkProjectComponent.initToolWindow();
            }
        }
    }

    public void reset() {
        hostTextField.setText(config.host);
        if (config.port != null) {
            portTextField.setText(String.valueOf(config.port));
        } else {
            portTextField.setText("2181");
        }
        enableZooKeeperCheckBox.setSelected(config.enabled);
    }

    public void disposeUIResources() {

    }
}
