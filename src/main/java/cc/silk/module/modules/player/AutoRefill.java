package cc.silk.module.modules.player;

import cc.silk.event.impl.player.TickEvent;
import cc.silk.mixin.HandledScreenAccessor;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.BooleanSetting;
import cc.silk.module.setting.KeybindSetting;
import cc.silk.module.setting.NumberSetting;
import cc.silk.utils.keybinding.KeyUtils;
import cc.silk.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public final class AutoRefill extends Module {
    private static final NumberSetting delay = new NumberSetting("Delay", 10, 200, 50, 10);
    private static final NumberSetting hoverDelay = new NumberSetting("Hover Delay", 10, 100, 25, 5);
    private static final NumberSetting minStack = new NumberSetting("Min Stack", 1, 64, 16, 1);
    private static final BooleanSetting hoverMode = new BooleanSetting("Hover Mode", false);
    private static final BooleanSetting health = new BooleanSetting("Health", true);
    private static final BooleanSetting regen = new BooleanSetting("Regen", true);
    private static final BooleanSetting strength = new BooleanSetting("Strength", false);
    private static final BooleanSetting speed = new BooleanSetting("Speed", false);
    private static final BooleanSetting tntCarts = new BooleanSetting("TNT Carts", false);
    private static final BooleanSetting randomPick = new BooleanSetting("Random Pick", true);
    
    private final KeybindSetting keybind = new KeybindSetting("Key", GLFW.GLFW_KEY_R, false);
    private final TimerUtil timer = new TimerUtil();
    private final Random random = new Random();
    private boolean keyPressed = false;
    private boolean wasInventoryOpen = false;
    private boolean active = false;

    public AutoRefill() {
        super("Auto Refill", "Refills hotbar with potions and TNT carts", -1, Category.PLAYER);
        this.addSettings(keybind, delay, hoverDelay, minStack, hoverMode, health, regen, strength, speed, tntCarts, randomPick);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        
        if (hoverMode.getValue()) {
            handleHover();
            return;
        }
        
        if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) return;
        
        boolean keyPressed = KeyUtils.isKeyPressed(keybind.getKeyCode());
        if (keyPressed && !this.keyPressed) {
            startRefill();
        }
        this.keyPressed = keyPressed;
        
        if (!active) return;
        
        if (timer.hasElapsedTime(delay.getValueInt())) {
            performRefill();
        }
    }

    private void handleHover() {
        if (!(mc.currentScreen instanceof InventoryScreen inv)) return;
        
        try {
            Slot slot = ((HandledScreenAccessor) inv).getFocusedSlot();
            if (slot == null || slot.getIndex() < 9 || !isPotion(slot.getStack()) || !needsRefill()) return;
            
            if (timer.hasElapsedTime(hoverDelay.getValueInt() + random.nextInt(10))) {
                clickSlot(slot.getIndex());
            }
        } catch (Exception ignored) {}
    }

    private void startRefill() {
        if (!hasPotions()) return;
        
        wasInventoryOpen = mc.currentScreen instanceof InventoryScreen;
        if (!wasInventoryOpen) {
            assert mc.player != null;
            mc.setScreen(new InventoryScreen(mc.player));
        }
        active = true;
        timer.reset();
    }
    
    private void performRefill() {
        if (!(mc.currentScreen instanceof InventoryScreen) || !needsRefill()) {
            finishRefill();
            return;
        }
        
        int slot = findPotion();
        if (slot == -1) {
            finishRefill();
            return;
        }
        
        clickSlot(slot);
    }

    private void finishRefill() {
        if (!wasInventoryOpen) {
            mc.setScreen(null);
        }
        active = false;
    }

    private boolean needsRefill() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || (isPotion(stack) && stack.getCount() < minStack.getValueInt())) {
                return true;
            }
        }
        return false;
    }
    
    private int findPotion() {
        java.util.List<Integer> potions = new java.util.ArrayList<>();
        for (int i = 9; i < 36; i++) {
            if (isPotion(mc.player.getInventory().getStack(i))) potions.add(i);
        }
        return potions.isEmpty() ? -1 : potions.get(random.nextInt(potions.size()));
    }
    
    private boolean hasPotions() {
        return findPotion() != -1;
    }
    
    private boolean isPotion(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        if (tntCarts.getValue() && stack.getItem() == Items.TNT_MINECART) {
            return true;
        }
        
        if (!isPotionItem(stack.getItem())) return false;
        
        PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (contents == null || contents.potion().isEmpty()) return false;
        
        return contents.potion().get().value().getEffects().stream().anyMatch(effect -> 
            (health.getValue() && effect.getEffectType().equals(StatusEffects.INSTANT_HEALTH)) ||
            (regen.getValue() && effect.getEffectType().equals(StatusEffects.REGENERATION)) ||
            (strength.getValue() && effect.getEffectType().equals(StatusEffects.STRENGTH)) ||
            (speed.getValue() && effect.getEffectType().equals(StatusEffects.SPEED)));
    }
    
    private boolean isPotionItem(Item item) {
        return item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION;
    }

    private void clickSlot(int slot) {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 
            slot, 0, SlotActionType.QUICK_MOVE, mc.player);
        timer.reset();
    }

    @Override
    public void onEnable() {
        timer.reset();
        keyPressed = KeyUtils.isKeyPressed(keybind.getKeyCode());
        active = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.currentScreen instanceof InventoryScreen && !wasInventoryOpen) {
            mc.setScreen(null);
        }
        active = false;
        super.onDisable();
    }
}