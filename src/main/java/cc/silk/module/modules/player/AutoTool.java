package cc.silk.module.modules.player;

import cc.silk.event.impl.player.TickEvent;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.BooleanSetting;
import cc.silk.module.setting.NumberSetting;
import cc.silk.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public final class AutoTool extends Module {
    private static final NumberSetting delay = new NumberSetting("Delay", 0, 100, 5, 1);
    private static final BooleanSetting returnToPrevious = new BooleanSetting("Return To Previous", true);
    private static final BooleanSetting onlyWhenSneaking = new BooleanSetting("Only When Sneaking", false);
    private static final BooleanSetting preventLowDurability = new BooleanSetting("Prevent Low Durability", true);
    private static final NumberSetting durabilityThreshold = new NumberSetting("Durability Threshold", 1, 100, 10, 1);
    
    private final TimerUtil timer = new TimerUtil();
    private int previousSlot = -1;

    public AutoTool() {
        super("Auto Tool", "Automatically switches to the best tool", -1, Category.PLAYER);
        this.addSettings(delay, returnToPrevious, onlyWhenSneaking, preventLowDurability, durabilityThreshold);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;

        if (onlyWhenSneaking.getValue() && !mc.player.isSneaking()) return;

        if (!mc.options.attackKey.isPressed()) {
            if (returnToPrevious.getValue() && previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
                previousSlot = -1;
            }
            return;
        }

        HitResult hit = mc.crosshairTarget;
        if (hit == null) return;

        if (hit.getType() == HitResult.Type.BLOCK) {
            handleMining((BlockHitResult) hit);
        }
    }

    private void handleMining(BlockHitResult hit) {
        BlockState state = mc.world.getBlockState(hit.getBlockPos());
        if (state.getHardness(mc.world, hit.getBlockPos()) < 0) return;

        int bestSlot = findBestTool(state);
        if (bestSlot != -1 && bestSlot != mc.player.getInventory().selectedSlot) {
            switchTool(bestSlot);
        }
    }

    private void switchTool(int slot) {
        if (!timer.hasElapsedTime(delay.getValueInt())) return;

        if (previousSlot == -1) {
            previousSlot = mc.player.getInventory().selectedSlot;
        }
        mc.player.getInventory().selectedSlot = slot;
        timer.reset();
    }

    private int findBestTool(BlockState state) {
        int bestSlot = -1;
        float bestScore = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !isEffectiveTool(stack, state)) continue;
            if (preventLowDurability.getValue() && hasLowDurability(stack)) continue;

            float score = calculateToolScore(stack, state);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    private boolean isEffectiveTool(ItemStack tool, BlockState state) {
        return tool.isSuitableFor(state) || tool.getMiningSpeedMultiplier(state) > 1.0f;
    }

    private float calculateToolScore(ItemStack tool, BlockState state) {
        float baseScore = tool.getMiningSpeedMultiplier(state);
        float materialBonus = getMaterialBonus(tool);
        return baseScore * materialBonus;
    }

    private float getMaterialBonus(ItemStack tool) {
        String name = tool.getItem().toString().toLowerCase();
        if (name.contains("netherite")) return 6.0f;
        if (name.contains("diamond")) return 5.0f;
        if (name.contains("iron")) return 4.0f;
        if (name.contains("golden")) return 3.5f;
        if (name.contains("stone")) return 2.0f;
        if (name.contains("wooden")) return 1.5f;
        return 1.0f;
    }

    private boolean hasLowDurability(ItemStack stack) {
        if (stack.getMaxDamage() <= 0) return false;
        int remaining = stack.getMaxDamage() - stack.getDamage();
        return remaining <= durabilityThreshold.getValueInt();
    }

    @Override
    public void onEnable() {
        timer.reset();
        previousSlot = -1;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (returnToPrevious.getValue() && previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
        }
        previousSlot = -1;
        super.onDisable();
    }
}