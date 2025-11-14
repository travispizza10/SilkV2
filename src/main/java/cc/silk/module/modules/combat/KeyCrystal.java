package cc.silk.module.modules.combat;

import cc.silk.event.impl.player.ItemUseEvent;
import cc.silk.event.impl.player.TickEvent;
import cc.silk.mixin.MinecraftClientAccessor;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.BooleanSetting;
import cc.silk.module.setting.KeybindSetting;
import cc.silk.module.setting.NumberSetting;
import cc.silk.utils.keybinding.KeyUtils;
import cc.silk.utils.math.TimerUtil;
import cc.silk.utils.mc.InventoryUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Random;

public final class KeyCrystal extends Module {

    private final KeybindSetting crystalKeybind = new KeybindSetting("Crystal Key", GLFW.GLFW_MOUSE_BUTTON_4, false);
    private final BooleanSetting antiSuicide = new BooleanSetting("Anti Suicide", true);
    private final BooleanSetting antiWeakness = new BooleanSetting("Anti Weakness", true);
    private final BooleanSetting stopOnKill = new BooleanSetting("Stop On Kill", false);

    private final NumberSetting placeChance = new NumberSetting("Place Chance (%)", 0, 100, 100, 1);
    private final NumberSetting breakChance = new NumberSetting("Break Chance (%)", 0, 100, 100, 1);

    private final NumberSetting minBreakDelay = new NumberSetting("Min Break Delay (MS)", 10, 500, 50, 1);
    private final NumberSetting maxBreakDelay = new NumberSetting("Max Break Delay (MS)", 10, 500, 100, 1);
    private final NumberSetting minPlaceDelay = new NumberSetting("Min Place Delay (MS)", 10, 500, 30, 1);
    private final NumberSetting maxPlaceDelay = new NumberSetting("Max Place Delay (MS)", 10, 500, 80, 1);

    private final TimerUtil breakTimer = new TimerUtil();
    private final TimerUtil placeTimer = new TimerUtil();
    private final Random random = new Random();

    private boolean keyPressed = false;
    private boolean isActive = false;
    private int originalSlot = -1;
    private boolean hasPlacedObsidian = false;

    private long currentBreakDelay;
    private long currentPlaceDelay;

    public KeyCrystal() {
        super("Key Crystal", "Automatically places and explodes crystals and obsidian for PvP", -1, Category.COMBAT);
        this.addSettings(
                crystalKeybind,
                placeChance, breakChance, stopOnKill,
                minBreakDelay, maxBreakDelay, minPlaceDelay, maxPlaceDelay,
                antiSuicide, antiWeakness
        );

        this.getSettings().removeIf(setting -> setting instanceof KeybindSetting && !setting.equals(crystalKeybind));
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull() || mc.currentScreen != null) return;

        if (minBreakDelay.getValueFloat() >= maxBreakDelay.getValueFloat())
            minBreakDelay.setValue(maxBreakDelay.getValueFloat() - 1);
        if (minPlaceDelay.getValueFloat() >= maxPlaceDelay.getValueFloat())
            minPlaceDelay.setValue(maxPlaceDelay.getValueFloat() - 1);

        boolean currentKeyState = KeyUtils.isKeyPressed(crystalKeybind.getKeyCode());

        if (currentKeyState && !keyPressed) startCrystalPvP();
        else if (!currentKeyState && keyPressed) stopCrystalPvP();

        keyPressed = currentKeyState;

        if (isActive) processCrystalPvP();
    }

    private void startCrystalPvP() {
        if (isActive) return;
        isActive = true;
        originalSlot = mc.player.getInventory().selectedSlot;
        hasPlacedObsidian = false;
        resetDelays();
    }

    private void stopCrystalPvP() {
        if (!isActive) return;
        if (originalSlot != -1) mc.player.getInventory().selectedSlot = originalSlot;
        isActive = false;
        originalSlot = -1;
        hasPlacedObsidian = false;
        resetDelays();
    }

    private void resetDelays() {
        breakTimer.reset();
        placeTimer.reset();

        int minBreak = minBreakDelay.getValueInt();
        int maxBreak = maxBreakDelay.getValueInt();
        int minPlace = minPlaceDelay.getValueInt();
        int maxPlace = maxPlaceDelay.getValueInt();

        if (minBreak >= maxBreak) {
            maxBreak = minBreak + 1;
            maxBreakDelay.setValue(maxBreak);
        }
        if (minPlace >= maxPlace) {
            maxPlace = minPlace + 1;
            maxPlaceDelay.setValue(maxPlace);
        }

        currentBreakDelay = minBreak + random.nextInt(maxBreak - minBreak);
        currentPlaceDelay = minPlace + random.nextInt(maxPlace - minPlace);
    }

    private void processCrystalPvP() {
        if (antiSuicide.getValue() && !mc.player.isOnGround()) return;
        if (stopOnKill.getValue() && isDeadPlayerNearby()) return;

        int randomInt = random.nextInt(100) + 1;

        if (mc.crosshairTarget instanceof EntityHitResult entityHit && breakTimer.hasElapsedTime(currentBreakDelay)) {
            if (entityHit.getEntity() instanceof EndCrystalEntity crystal && randomInt <= breakChance.getValueInt()) {
                if (mc.player.getPos().distanceTo(crystal.getPos()) <= 6.0) {
                    if (antiWeakness.getValue() &&
                            mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
                        InventoryUtil.swapToWeapon(SwordItem.class);
                    }

                    ((MinecraftClientAccessor) mc).invokeDoAttack();


                    breakTimer.reset();
                    currentBreakDelay = random.nextLong(minBreakDelay.getValueInt(), maxBreakDelay.getValueInt());
                }
                return;
            }
        }


        if (mc.crosshairTarget instanceof BlockHitResult blockHit && placeTimer.hasElapsedTime(currentPlaceDelay)) {
            BlockPos targetBlock = blockHit.getBlockPos();
            BlockPos placementPos = targetBlock.offset(blockHit.getSide());

            if (isObsidianOrBedrock(targetBlock) && isValidCrystalPosition(placementPos)
                    && randomInt <= placeChance.getValueInt()) {
                if (hasItemInHotbar(Items.END_CRYSTAL)) {
                    InventoryUtil.swapToSlot(Items.END_CRYSTAL);
                    ((MinecraftClientAccessor) mc).invokeDoItemUse();
                    placeTimer.reset();
                    currentPlaceDelay = random.nextLong(minPlaceDelay.getValueInt(), maxPlaceDelay.getValueInt());
                }
            } else if (isValidPosition(placementPos) && !hasPlacedObsidian) {
                BlockPos below = placementPos.down();
                if (!mc.world.getBlockState(below).isAir()) {
                    if (hasItemInHotbar(Items.OBSIDIAN)) {
                        InventoryUtil.swapToSlot(Items.OBSIDIAN);
                        ((MinecraftClientAccessor) mc).invokeDoItemUse();
                        hasPlacedObsidian = true;
                        placeTimer.reset();
                        currentPlaceDelay = random.nextLong(minPlaceDelay.getValueInt(), maxPlaceDelay.getValueInt());
                    }
                }
            }
        }
    }

    private boolean hasItemInHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) return true;
        }
        return false;
    }

    private boolean isValidPosition(BlockPos pos) {
        if (mc.world == null) return false;
        if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > 4.5) return false;
        if (!mc.world.getBlockState(pos).isAir()) return false;

        BlockPos playerPos = mc.player.getBlockPos();
        return !pos.equals(playerPos) && !pos.equals(playerPos.up());
    }

    private boolean isObsidianOrBedrock(BlockPos pos) {
        if (mc.world == null) return false;
        var block = mc.world.getBlockState(pos).getBlock();
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK;
    }

    private boolean isValidCrystalPosition(BlockPos pos) {
        if (mc.world == null) return false;
        if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > 4.5) return false;
        if (!mc.world.getBlockState(pos).isAir()) return false;
        if (!mc.world.getBlockState(pos.up()).isAir()) return false;

        BlockPos playerPos = mc.player.getBlockPos();
        return !pos.equals(playerPos) && !pos.equals(playerPos.up()) &&
                !pos.up().equals(playerPos) && !pos.up().equals(playerPos.up());
    }

    private boolean isDeadPlayerNearby() {
        if (mc.world == null) return false;
        List<? extends Entity> players = mc.world.getPlayers();
        for (Entity e : players) {
            if (e == mc.player) continue;
            if (e.isRemoved() || ((LivingEntity) e).getHealth() <= 0.0f)
                if (e.squaredDistanceTo(mc.player) < 36)
                    return true;
        }
        return false;
    }


    @Override
    public void onDisable() {
        super.onDisable();
        stopCrystalPvP();
    }

    @Override
    public int getKey() {
        return -1;
    }
}