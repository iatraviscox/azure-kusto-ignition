package com.microsoft.opensource.cla.ignition;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.perspective.common.ConfigurationException;
import com.inductiveautomation.perspective.common.config.BindingConfig;
import com.inductiveautomation.perspective.gateway.api.Binding;
import com.inductiveautomation.perspective.gateway.api.BindingContext;
import com.inductiveautomation.perspective.gateway.api.BindingFactory;
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext;
import com.microsoft.opensource.cla.ignition.azurekusto.AzureKustoBindingFactory;
import com.microsoft.opensource.cla.ignition.azurekusto.AzureKustoHistoryProvider;
import com.microsoft.opensource.cla.ignition.azurekusto.AzureKustoHistoryProviderType;
import com.microsoft.opensource.cla.ignition.azurekusto.binding.AzureKustoBinding;
import com.microsoft.opensource.cla.ignition.azurekusto.binding.AzureKustoBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

/**
 * The GatewayHook is the entry for the module. The hook is responsible for
 * using extension points and adding profiles to Ignition. This hook adds a
 * new tag history provider type that a developer can use to store/retrieve
 * data from Azure Data Explorer.
 */
public class GatewayHook extends AbstractGatewayModuleHook {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private GatewayContext context;
    private AzureKustoHistoryProviderType azureKustoHistoryProviderType;
    private PerspectiveContext perspectiveContext;

    @Override
    public void setup(GatewayContext gatewayContext) {
        this.context = gatewayContext;

        // Add Perspective binding
        try {
            this.perspectiveContext = PerspectiveContext.get(gatewayContext);
            this.perspectiveContext.getBindingRegistry().register(AzureKustoBindingConstants.TYPE_ID, new AzureKustoBindingFactory(context));
        } catch(Exception ex){
            logger.error("Error registering Perspective binding", ex);
        }

        azureKustoHistoryProviderType = new AzureKustoHistoryProviderType();

        // Add bundle resource for localization
        BundleUtil.get().addBundle(AzureKustoHistoryProvider.class);

        // Add Azure Kusto history provider type
        try {
            context.getTagHistoryManager().addTagHistoryProviderType(azureKustoHistoryProviderType);
        } catch (Exception ex) {
            logger.error("Error adding Azure Kusto history provider type", ex);
        }
    }

    @Override
    public void startup(LicenseState licenseState) {

    }

    @Override
    public void shutdown() {
        // Remove bundle resource
        BundleUtil.get().removeBundle(AzureKustoHistoryProviderType.class);

        // Remove Azure Kusto history provider type
        try {
            context.getTagHistoryManager().removeTagHistoryProviderType(azureKustoHistoryProviderType);
        } catch (Exception ex) {
            logger.error("Error shutting down Azure Kusto history provider type", ex);
        }
    }

    @Override
    public boolean isFreeModule() {
        return true;
    }
}
