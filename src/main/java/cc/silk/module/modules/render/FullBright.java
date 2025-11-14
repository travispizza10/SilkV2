package cc.silk.module.modules.render;

import cc.silk.module.Category;
import cc.silk.module.Module;
import net.minecraft.client.option.SimpleOption;

public class FullBright extends Module {
    private Double previousGamma = null;

    public FullBright() {
        super("Full Bright", "Removes darkness", Category.RENDER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc != null && mc.options != null) {
            try {
                SimpleOption<Double> gamma = mc.options.getGamma();
                previousGamma = gamma.getValue();
                gamma.setValue(1.0);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc != null && mc.options != null && previousGamma != null) {
            try {
                mc.options.getGamma().setValue(previousGamma);
            } catch (Throwable ignored) {
            }
        }
    }
}
