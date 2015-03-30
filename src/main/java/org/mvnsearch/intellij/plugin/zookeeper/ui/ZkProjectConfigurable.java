package org.mvnsearch.intellij.plugin.zookeeper.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.mvnsearch.intellij.plugin.zookeeper.ZkConfigPersistence;
import org.mvnsearch.intellij.plugin.zookeeper.ZkProjectComponent;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
    private JTextField pathsTextField;
    private JCheckBox enableZooKeeperCheckBox;
    private JTextField charsetTextField;
    private JCheckBox statTooltipCheckBox;
    private ZkConfigPersistence config;

    public ZkProjectConfigurable(Project project) {
        this.project = project;
        this.config = ZkConfigPersistence.getInstance(project);
        reset();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "ZooKeeper";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return root;
    }

    @Override
    public boolean isModified() {
        String newHost = hostTextField.getText().trim();
        String newPort = portTextField.getText().trim();
        String newPath = pathsTextField.getText();
        String newCharset = charsetTextField.getText();
        if (newPath == null) {
            newPath = "";
        } else {
            newPath = newPath.trim();
        }
        return !(newHost.equals(config.host)
                && Integer.valueOf(newPort).equals(config.port)
                && newCharset.equals(config.charset)
                && config.enabled == enableZooKeeperCheckBox.isSelected()
                && config.tooltip == statTooltipCheckBox.isSelected()
                && (newPath.equals(config.whitePaths)));
    }

    @Override
    public void apply() throws ConfigurationException {
        String oldHost = config.host;
        config.host = hostTextField.getText().trim();
        config.port = Integer.valueOf(portTextField.getText().trim());
        config.charset = charsetTextField.getText();
        boolean oldEnabled = config.enabled;
        config.enabled = enableZooKeeperCheckBox.isSelected();
        config.tooltip = statTooltipCheckBox.isSelected();
        ZkProjectComponent zkProjectComponent = ZkProjectComponent.getInstance(project);
        if (!oldEnabled && config.enabled) {
            zkProjectComponent.initZk();
            if (ToolWindowManager.getInstance(project).getToolWindow("ZooKeeper") == null) {
                zkProjectComponent.initToolWindow();
            }
        }
        // host changed to init zk again
        if (oldHost != null && !oldHost.equals(config.host)) {
            zkProjectComponent.initZk();
        }
        config.whitePaths = pathsTextField.getText();
        if (config.isAvailable()) {
            zkProjectComponent.reloadZkTree();
        }
    }

    @Override
    public void reset() {
        hostTextField.setText(config.host);
        portTextField.setText(config.port == null ? "2181" : String.valueOf(config.port));
        enableZooKeeperCheckBox.setSelected(config.enabled);
        pathsTextField.setText(config.whitePaths);
        charsetTextField.setText(config.charset == null ? "UTF-8" : config.charset);
        statTooltipCheckBox.setSelected(config.tooltip);
    }

    @Override
    public void disposeUIResources() {

    }
}
