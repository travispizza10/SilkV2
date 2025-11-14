package cc.silk.module.modules.player;


import cc.silk.event.impl.player.TickEvent;
import cc.silk.mixin.MinecraftClientAccessor;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.BooleanSetting;
import cc.silk.module.setting.NumberSetting;
import cc.silk.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public final class FastPlace extends Module {

    private static final BooleanSetting blocksOnly = new BooleanSetting("Blocks Only", true);
    private static final NumberSetting delay = new NumberSetting("Delay", 0, 4, 0, 1);
    private final TimerUtil timer = new TimerUtil();

    public FastPlace() {
        super("Fast Place", "Bypasses item use cooldown for faster block placement", -1, Category.PLAYER);
        this.addSettings(blocksOnly, delay);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;

        if (blocksOnly.getValue()) {
            ItemStack heldItem = mc.player.getMainHandStack();
            if (heldItem.isEmpty() || !(heldItem.getItem() instanceof BlockItem)) {
                return;
            }
        }

        long delayMs = delay.getValueInt() * 50L;
        if (timer.hasElapsedTime(delayMs, true)) {
            ((MinecraftClientAccessor) mc).setItemUseCooldown(0);
        }
    }

    @Override
    public void onEnable() {
        timer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        timer.reset();

        if (mc.player != null) {
            ((MinecraftClientAccessor) mc).setItemUseCooldown(4);
        }
        super.onDisable();
    }
}