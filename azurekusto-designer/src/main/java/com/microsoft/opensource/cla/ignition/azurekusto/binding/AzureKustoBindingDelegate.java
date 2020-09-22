package com.microsoft.opensource.cla.ignition.azurekusto.binding;

import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.perspective.designer.api.BindingConfigPanel;
import com.inductiveautomation.perspective.designer.api.BindingDesignDelegate;
import com.inductiveautomation.perspective.designer.api.BindingEditContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class AzureKustoBindingDelegate extends BindingDesignDelegate {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DesignerContext context;

    public AzureKustoBindingDelegate(DesignerContext context) {
        super(AzureKustoBindingConstants.TYPE_ID, "AzureKustoBinding.Binding.Name", "AzureKustoBinding.Binding.Desc");
        this.context = context;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(AzureKustoBindingDelegate.class.getResource("adx.png"));
    }

    @Override
    public BindingConfigPanel createEditingPane(BindingEditContext context) {
        return new AzureKustoBindingConfigPanel(context);
    }
}
