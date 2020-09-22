package com.microsoft.opensource.cla.ignition;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface;
import com.microsoft.opensource.cla.ignition.azurekusto.binding.AzureKustoBindingDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DesignerHook extends AbstractDesignerModuleHook {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PerspectiveDesignerInterface perspectiveDesignerInterface;

    @Override
    public void startup(DesignerContext context, LicenseState activationState) throws Exception {
        perspectiveDesignerInterface = PerspectiveDesignerInterface.get(context);
        perspectiveDesignerInterface.getBindingRegistry().registerBinding(new AzureKustoBindingDelegate(context));

        BundleUtil.get().addBundle("AzureKustoBinding", AzureKustoBindingDelegate.class, "AzureKustoBinding");
    }

    @Override
    public void shutdown() {
        BundleUtil.get().removeBundle("AzureKustoBinding", "AzureKustoBinding");
    }
}
