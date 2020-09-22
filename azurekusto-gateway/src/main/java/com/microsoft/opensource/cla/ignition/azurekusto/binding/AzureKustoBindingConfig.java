package com.microsoft.opensource.cla.ignition.azurekusto.binding;

import com.inductiveautomation.ignition.common.JsonUtilities;
import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.inductiveautomation.perspective.common.ConfigCondition;
import com.inductiveautomation.perspective.common.ConfigurationException;
import com.inductiveautomation.perspective.common.config.binding.PollingConfig;

public class AzureKustoBindingConfig {
    public final String historyProvider, database, query;
    public final PollingConfig polling;

    public static AzureKustoBindingConfig fromJson(JsonObject object) throws ConfigurationException {
        ConfigCondition.ofObject(object).requireNonBlankString(AzureKustoBindingConstants.CONFIG_HISTORY_PROVIDER).optionalString(AzureKustoBindingConstants.CONFIG_DATABASE).requireNonBlankString(AzureKustoBindingConstants.CONFIG_QUERY);

        String historyProvider = object.get(AzureKustoBindingConstants.CONFIG_HISTORY_PROVIDER).getAsString();
        String database = object.get(AzureKustoBindingConstants.CONFIG_DATABASE).getAsString();
        String query = object.get(AzureKustoBindingConstants.CONFIG_QUERY).getAsString();

        PollingConfig polling = PollingConfig.fromJson(JsonUtilities.readObject(object, "polling").orElse(null));

        return new AzureKustoBindingConfig(historyProvider, database, query, polling);
    }

    public AzureKustoBindingConfig(String historyProvider, String database, String query, PollingConfig polling) {
        this.historyProvider = historyProvider;
        this.database = database;
        this.query = query;
        this.polling = polling;
    }
}
