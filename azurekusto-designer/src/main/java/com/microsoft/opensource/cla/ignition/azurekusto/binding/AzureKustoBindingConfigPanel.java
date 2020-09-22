package com.microsoft.opensource.cla.ignition.azurekusto.binding;

import com.inductiveautomation.ignition.common.JsonUtilities;
import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.inductiveautomation.perspective.designer.api.BindingConfigPanel;
import com.inductiveautomation.perspective.designer.api.BindingEditContext;
import com.inductiveautomation.perspective.designer.binding.ExpressionField;
import com.inductiveautomation.perspective.designer.binding.PollingOptionPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class AzureKustoBindingConfigPanel extends BindingConfigPanel {
    private final BindingEditContext context;
    private JTextField historyProviderTextField, databaseTextField;
    private ExpressionField queryExpressionField;
    private PollingOptionPanel polling;

    public AzureKustoBindingConfigPanel(BindingEditContext context) {
        super(new MigLayout("ins 0"), context);

        this.context = context;

        final JPanel historyProviderPanel = new JPanel(new MigLayout("ins 0"));
        historyProviderPanel.add(new JLabel("History Provider"));
        historyProviderTextField = new JTextField("");
        historyProviderPanel.add(historyProviderTextField, "push,grow");
        add(historyProviderPanel, "grow,wrap");

        final JPanel databasePanel = new JPanel(new MigLayout("ins 0"));
        databasePanel.add(new JLabel("Database"));
        databaseTextField = new JTextField("");
        databasePanel.add(databaseTextField, "push,grow");
        add(databasePanel, "grow,wrap");

        final JPanel queryPanel = new JPanel(new MigLayout("ins 0"));
        queryPanel.add(new JLabel("Query"));
        queryExpressionField = new ExpressionField(context);
        queryPanel.add(queryExpressionField, "push,grow");
        add(queryPanel, "grow,push,wrap");

        polling = new PollingOptionPanel(context);
        add(polling);
    }

    @Override
    public void initialize(JsonObject config) {
        String historyProvider = JsonUtilities.readString(config, AzureKustoBindingConstants.CONFIG_HISTORY_PROVIDER, "");
        String database = JsonUtilities.readString(config, AzureKustoBindingConstants.CONFIG_DATABASE, "");
        String query = JsonUtilities.readString(config, AzureKustoBindingConstants.CONFIG_QUERY, "");

        historyProviderTextField.setText(historyProvider);
        databaseTextField.setText(database);
        queryExpressionField.setText(query);

        polling.initialize(config);

        listen(historyProviderTextField);
        listen(databaseTextField);
        listen(queryExpressionField);
    }

    @Override
    public JsonObject getConfig() {
        JsonObject config = new JsonObject();
        config.addProperty(AzureKustoBindingConstants.CONFIG_HISTORY_PROVIDER, historyProviderTextField.getText());
        config.addProperty(AzureKustoBindingConstants.CONFIG_DATABASE, databaseTextField.getText());
        config.addProperty(AzureKustoBindingConstants.CONFIG_QUERY, queryExpressionField.getText());
        if (polling.isPollingEnabled()) {
            config.add("polling", polling.toJson());
        }
        return config;
    }
}
