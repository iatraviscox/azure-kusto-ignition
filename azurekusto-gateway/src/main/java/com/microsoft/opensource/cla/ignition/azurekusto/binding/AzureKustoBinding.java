package com.microsoft.opensource.cla.ignition.azurekusto.binding;

import com.inductiveautomation.ignition.common.gson.JsonArray;
import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.sqltags.config.TagHistoryProviderRecord;
import com.inductiveautomation.metro.utils.StringUtils;
import com.inductiveautomation.perspective.common.ConfigurationException;
import com.inductiveautomation.perspective.gateway.api.BindingContext;
import com.inductiveautomation.perspective.gateway.binding.AbstractPollingBinding;
import com.inductiveautomation.perspective.gateway.binding.PerspectiveExpression;
import com.inductiveautomation.perspective.gateway.driven.ExpressionDrivenStringValue;
import com.microsoft.azure.kusto.data.KustoOperationResult;
import com.microsoft.azure.kusto.data.KustoResultColumn;
import com.microsoft.azure.kusto.data.KustoResultSetTable;
import com.microsoft.opensource.cla.ignition.Utils;
import com.microsoft.opensource.cla.ignition.azurekusto.AzureKustoConnection;
import com.microsoft.opensource.cla.ignition.azurekusto.AzureKustoHistoryProviderSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simpleorm.dataset.SQuery;

public class AzureKustoBinding extends AbstractPollingBinding {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private GatewayContext context;
    private BindingContext bindingContext;

    private AzureKustoBindingConfig config;
    private String database;
    private KustoQuery query;
    private String queryValue;
    private AzureKustoConnection connection;

    public AzureKustoBinding(GatewayContext context, BindingContext bindingContext, AzureKustoBindingConfig config) throws ConfigurationException {
        super(bindingContext, config.polling.getRateExpression());
        this.context = context;
        this.bindingContext = bindingContext;
        this.config = config;

        SQuery<TagHistoryProviderRecord> spQuery = new SQuery<>(TagHistoryProviderRecord.META);
        spQuery.eq(TagHistoryProviderRecord.Name, config.historyProvider);
        TagHistoryProviderRecord historyProviderRecord = context.getPersistenceInterface().queryOne(spQuery);

        SQuery<AzureKustoHistoryProviderSettings> sQuery = new SQuery<>(AzureKustoHistoryProviderSettings.META);
        sQuery.eq(AzureKustoHistoryProviderSettings.ProfileId, historyProviderRecord.getId());
        AzureKustoHistoryProviderSettings settings = context.getPersistenceInterface().queryOne(sQuery);
        connection = new AzureKustoConnection(settings);

        database = config.database;
        if (database == null || StringUtils.isBlank(database)) {
            database = connection.getDatabase();
        }

        query = new KustoQuery(this, PerspectiveExpression.create(bindingContext, config.query), this::scheduleNow);
    }

    @Override
    protected boolean isReady() {
        return query.isReady();
    }

    @Override
    protected boolean isDirty() {
        return query.isDirty();
    }

    @Override
    protected void preExecutionPrep() {
        queryValue = query.getAndClear().orElse("");
    }

    @Override
    public synchronized void startup() {
        query.startup();
    }

    @Override
    public synchronized void shutdown() {
        query.shutdown();
    }

    @Override
    protected QualifiedValue execute() throws Exception {
        BasicQualifiedValue qv = null;

        try {
            KustoOperationResult results = connection.runQuery(database, queryValue);
            KustoResultSetTable tableResult = results.getPrimaryResults();
            JsonArray retArray = new JsonArray();

            while (tableResult.next()) {
                JsonObject rowObj = new JsonObject();
                for (KustoResultColumn col : tableResult.getColumns()) {
                    rowObj.add(col.getColumnName(), Utils.toJsonDeep(tableResult.getObject(col.getColumnName())));
                }
                retArray.add(rowObj);
            }

            qv = new BasicQualifiedValue(retArray);
        } catch (Throwable ex) {
            logger.error("Error running Kusto query", ex);
            qv = new BasicQualifiedValue(bindingContext.getLastValue().getValue(), QualityCode.Error);
        }

        return qv;
    }

    private class KustoQuery extends ExpressionDrivenStringValue {
        KustoQuery(final Object lock,
            final PerspectiveExpression expression,
            final Runnable onValueChangedCallback) {
            super(lock, expression, onValueChangedCallback);
        }
    }
}