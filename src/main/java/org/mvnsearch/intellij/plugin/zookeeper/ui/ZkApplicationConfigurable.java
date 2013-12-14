package org.mvnsearch.intellij.plugin.zookeeper.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.mvnsearch.intellij.plugin.zookeeper.ZkApplicationComponent;
import org.mvnsearch.intellij.plugin.zookeeper.ZkConfigPersistence;

import javax.swing.*;

/**
 * Zoo Keeper application configurable
 *
 * @author linux_china
 */
public class ZkApplicationConfigurable implements Configurable {
    private JPanel root;
    private JTextField hostTextField;
    private JTextField portTextField;
    private JCheckBox enableZooKeeperCheckBox;
    private ZkConfigPersistence config;

    public ZkApplicationConfigurable() {
        this.config = ZkConfigPersistence.getInstance();
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
                && config.enabled==enableZooKeeperCheckBox.isSelected());
    }

    public void apply() throws ConfigurationException {
        config.host = hostTextField.getText().trim();
        config.port = Integer.valueOf(portTextField.getText().trim());
        config.enabled = enableZooKeeperCheckBox.isEnabled();
        ZkApplicationComponent.getInstance().resetZkConfig();
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
