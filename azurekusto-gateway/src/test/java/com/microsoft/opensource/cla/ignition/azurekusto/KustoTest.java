package com.microsoft.opensource.cla.ignition.azurekusto;
import com.microsoft.azure.kusto.data.*;
import com.microsoft.azure.kusto.data.exceptions.DataClientException;
import com.microsoft.azure.kusto.data.exceptions.DataServiceException;
import com.microsoft.azure.kusto.ingest.exceptions.IngestionClientException;
import com.microsoft.azure.kusto.ingest.exceptions.IngestionServiceException;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

public class KustoTest {

    public static void main(String[] args) throws IngestionClientException, IOException, IngestionServiceException, DataClientException, DataServiceException, URISyntaxException {

        // Ingest test without context
        String clusterName = System.getProperty("cluster","https://ignitionadxpoc.eastus.kusto.windows.net");
        String databaseName = System.getProperty("database","Contoso");
        String appId = System.getProperty("appId");
        String appKey = System.getProperty("appKey");
        String appTenant = System.getProperty("appTenant");
        AzureKustoHistoryProviderSettings settings = new AzureKustoHistoryProviderSettings();
        settings.setString(AzureKustoHistoryProviderSettings.ClusterURL, clusterName);
        settings.setString(AzureKustoHistoryProviderSettings.ApplicationId, appId);
        settings.setString(AzureKustoHistoryProviderSettings.ApplicationKey, appKey);
        settings.setString(AzureKustoHistoryProviderSettings.AADTenantId, appTenant);
        settings.setString(AzureKustoHistoryProviderSettings.DatabaseName, databaseName);

        AzureKustoHistorySink kusto = new AzureKustoHistorySink("kusto", null, settings);
        kusto.startup();
        ArrayList<AzureKustoTagValue> recs = new ArrayList<>();
        AzureKustoTagValue azureKustoTagValue = new AzureKustoTagValue();
        azureKustoTagValue.setSystemName("kustoIgnitoin");
        azureKustoTagValue.setTagProvider("ohad and uri");
        azureKustoTagValue.setTagPath("toKusto");
        azureKustoTagValue.setValue(new Object(){
            public String name = "travis";
            public int x = 1;
        });
        azureKustoTagValue.setTimestamp(new Date());
        azureKustoTagValue.setQuality(1000000);
        recs.add(azureKustoTagValue);

        kusto.ingestRecords(recs);

        ConnectionStringBuilder csb = ConnectionStringBuilder.createWithAadApplicationCredentials(
                clusterName,
                appId,
                appKey,
                appTenant
               );

        ClientImpl client = new ClientImpl(csb);

        Date dNow = DateTime.now().toDate();
        Date dEarlier = new Date(0);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSS");

        String queryText = "Events| where timestamp between(datetime(" +
                simpleDateFormat.format(dEarlier) + ")..datetime(" +
                simpleDateFormat.format(dNow) + "))";

        // in case we want to pass client request properties
        //ClientRequestProperties clientRequestProperties = new ClientRequestProperties();
        //clientRequestProperties.setTimeoutInMilliSec(TimeUnit.MINUTES.toMillis(1));

        KustoOperationResult results = client.execute(databaseName, queryText
        //        , clientRequestProperties
        );
        KustoResultSetTable mainTableResult = results.getPrimaryResults();
        System.out.println(String.format("Kusto sent back %s rows.", mainTableResult.count()));


        mainTableResult = results.getPrimaryResults();

        mainTableResult.first();
        do {
            String system = mainTableResult.getString("systemName");
            String tagProvider = mainTableResult.getString("tagProvider");
            String tagPath = mainTableResult.getString("tagPath");
            Object value = mainTableResult.getObject("value");
            Double value_double = mainTableResult.getDouble("value_double");
            Integer value_integer = mainTableResult.getInt("value_integer");
            LocalDateTime timestamp = mainTableResult.getKustoDateTime("timestamp");

            System.out.println(
                            "System:" + system +
                            " tagProvider:" +  tagProvider +
                            " tagPath:" +  tagPath +
                            " Value:" +  value +
                            " value_double:" +  value_double +
                            " value_integer:" +  value_integer +
                            " timestamp");
        }
        while (mainTableResult.next());


    }
}