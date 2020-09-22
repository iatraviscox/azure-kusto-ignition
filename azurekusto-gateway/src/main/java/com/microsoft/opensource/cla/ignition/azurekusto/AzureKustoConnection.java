package com.microsoft.opensource.cla.ignition.azurekusto;

import com.microsoft.azure.kusto.data.ClientImpl;
import com.microsoft.azure.kusto.data.ConnectionStringBuilder;
import com.microsoft.azure.kusto.data.KustoOperationResult;
import com.microsoft.azure.kusto.data.exceptions.DataClientException;
import com.microsoft.azure.kusto.data.exceptions.DataServiceException;
import com.microsoft.azure.kusto.ingest.IngestClient;
import com.microsoft.azure.kusto.ingest.IngestClientFactory;
import com.microsoft.azure.kusto.ingest.IngestionProperties;
import com.microsoft.azure.kusto.ingest.StreamingIngestClient;
import com.microsoft.azure.kusto.ingest.exceptions.IngestionClientException;
import com.microsoft.azure.kusto.ingest.exceptions.IngestionServiceException;
import com.microsoft.azure.kusto.ingest.source.StreamSourceInfo;
import com.microsoft.opensource.cla.ignition.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

public class AzureKustoConnection {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AzureKustoHistoryProviderSettings settings;
    private ClientImpl clientImpl;
    private IngestClient ingestClient;
    private StreamingIngestClient streamingIngestClient;
    private IngestionProperties ingestionProperties;

    public AzureKustoConnection(AzureKustoHistoryProviderSettings settings) {
        this.settings = settings;

        ingestionProperties = new IngestionProperties(getDatabase(), getTable());
        ingestionProperties.setDataFormat(IngestionProperties.DATA_FORMAT.csv);
    }

    private ConnectionStringBuilder getConnectionString() {
        String clusterURL = settings.getClusterURL();
        String engineURL = Utils.getEngineUriFromSetting(clusterURL);
        String applicationId = settings.getApplicationId();
        String applicationKey = settings.getApplicationKey();
        String aadTenantId = settings.getAADTenantId();

        ConnectionStringBuilder connectionString = ConnectionStringBuilder.createWithAadApplicationCredentials(
                engineURL,
                applicationId,
                applicationKey,
                aadTenantId);

        return connectionString;
    }

    private ConnectionStringBuilder getDMConnectionString() {
        String clusterURL = settings.getClusterURL();
        String dmUrl = Utils.getDMUriFromSetting(clusterURL);
        String applicationId = settings.getApplicationId();
        String applicationKey = settings.getApplicationKey();
        String aadTenantId = settings.getAADTenantId();

        ConnectionStringBuilder connectionString = ConnectionStringBuilder.createWithAadApplicationCredentials(
                dmUrl,
                applicationId,
                applicationKey,
                aadTenantId);

        return connectionString;
    }

    private ClientImpl getQueryClient() {
        if (clientImpl == null) {
            try {
                clientImpl = new ClientImpl(getConnectionString());
            } catch (URISyntaxException ex) {
                logger.error("Error creating query client", ex);
            }
        }
        return clientImpl;
    }

    private IngestClient getIngestClient() {
        if (ingestClient == null) {
            try {
                ingestClient = IngestClientFactory.createClient(getDMConnectionString());
            } catch (URISyntaxException ex) {
                logger.error("Error creating ingest client", ex);
            }
        }
        return ingestClient;
    }

    private StreamingIngestClient getStreamingIngestClient() {
        if (streamingIngestClient == null) {
            try {
                streamingIngestClient = IngestClientFactory.createStreamingIngestClient(getConnectionString());
            } catch (URISyntaxException ex) {
                logger.error("Error creating streaming ingest client", ex);
            }
        }
        return streamingIngestClient;
    }

    public void ingestFromStream(StreamSourceInfo streamSourceInfo) throws IngestionClientException, IngestionServiceException {
        getIngestClient().ingestFromStream(streamSourceInfo, ingestionProperties);
    }

    public String getDatabase() {
        return settings.getDatabaseName();
    }

    public String getTable() {
        return settings.getTableName();
    }

    public void verifyTable() {
        try {
            KustoOperationResult result = getQueryClient().execute(getDatabase(), ".show table " + getTable());
        } catch (Throwable ex) {
            try {
                getQueryClient().execute(getDatabase(), ".create table " + getTable() + " ( systemName:string, tagProvider:string, tagPath:string, value:dynamic, value_double:real, value_integer:int, timestamp:datetime, quality:int)");
            } catch (Throwable ex2) {
                logger.error("Error creating table '" + getTable() + "'", ex2);
            }
        }
    }

    public KustoOperationResult runQuery(String query) throws DataServiceException, DataClientException {
        return runQuery(getDatabase(), query);
    }

    public KustoOperationResult runQuery(String database, String query) throws DataServiceException, DataClientException {
        return getQueryClient().execute(database, query);
    }

}
