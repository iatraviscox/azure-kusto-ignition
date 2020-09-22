package com.microsoft.opensource.cla.ignition.azurekusto;

import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.perspective.common.ConfigurationException;
import com.inductiveautomation.perspective.common.config.BindingConfig;
import com.inductiveautomation.perspective.gateway.api.Binding;
import com.inductiveautomation.perspective.gateway.api.BindingContext;
import com.inductiveautomation.perspective.gateway.api.BindingFactory;
import com.microsoft.opensource.cla.ignition.azurekusto.binding.AzureKustoBinding;
import com.microsoft.opensource.cla.ignition.azurekusto.binding.AzureKustoBindingConfig;

public class AzureKustoBindingFactory implements BindingFactory {
    private GatewayContext context;

    public AzureKustoBindingFactory(GatewayContext context) {
        this.context = context;
    }

    @Override
    public Binding create(BindingContext bindingContext, BindingConfig bindingConfig) throws ConfigurationException {
        return new AzureKustoBinding(context, bindingContext, AzureKustoBindingConfig.fromJson(bindingConfig.config));
    }
}
