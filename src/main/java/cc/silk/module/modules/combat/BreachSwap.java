package cc.silk.module.modules.combat;

import cc.silk.event.impl.player.AttackEvent;
import cc.silk.event.impl.player.TickEvent;
import cc.silk.mixin.MinecraftClientAccessor;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.BooleanSetting;
import cc.silk.module.setting.NumberSetting;
import cc.silk.utils.mc.EnchantmentUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public final class BreachSwap extends Module {

    private final NumberSetting switchDelay = new NumberSetting("Switch Delay", 10, 100, 30, 1);
    private final BooleanSetting onlyOnGround = new BooleanSetting("Only on ground", true);
    private final BooleanSetting silentSwap = new BooleanSetting("Silent Swap", true);

    private int originalSlot = -1;
    private boolean shouldSwitchBack = false;
    private long switchTime = 0;
    private boolean isSwappingAttack = false;

    public BreachSwap() {
        super("Breach Swap", "Switches to a Breach enchanted mace when attacking", Category.COMBAT);
        addSettings(switchDelay, onlyOnGround, silentSwap);
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (isNull() || isSwappingAttack)
            return;
        if (onlyOnGround.getValue() && !mc.player.isOnGround())
            return;
        if (ShieldBreaker.breakingShield)
            return;
        if (!(event.getTarget() instanceof LivingEntity))
            return;

        int maceSlot = findBreachMaceSlot();
        if (maceSlot == -1)
            return;

        if (originalSlot == -1) {
            originalSlot = mc.player.getInventory().selectedSlot;
        }

        if (silentSwap.getValue()) {
            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = maceSlot;

            isSwappingAttack = true;
            ((MinecraftClientAccessor) mc).invokeDoAttack();
            isSwappingAttack = false;

            mc.player.getInventory().selectedSlot = prevSlot;
        } else {
            mc.player.getInventory().selectedSlot = maceSlot;

            isSwappingAttack = true;
            ((MinecraftClientAccessor) mc).invokeDoAttack();
            isSwappingAttack = false;

            shouldSwitchBack = true;
            switchTime = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (isNull())
            return;
        if (ShieldBreaker.breakingShield)
            return;

        if (shouldSwitchBack && System.currentTimeMillis() - switchTime >= switchDelay.getValue()) {
            if (originalSlot != -1) {
                mc.player.getInventory().selectedSlot = originalSlot;
                originalSlot = -1;
            }
            shouldSwitchBack = false;
        }

        if (mc.options.attackKey.isPressed()) {
            HitResult hitResult = mc.crosshairTarget;
            if (hitResult instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity) {
                int maceSlot = findBreachMaceSlot();
                if (maceSlot != -1) {
                    if (originalSlot == -1) {
                        originalSlot = mc.player.getInventory().selectedSlot;
                    }

                    if (silentSwap.getValue()) {
                        int prevSlot = mc.player.getInventory().selectedSlot;
                        mc.player.getInventory().selectedSlot = maceSlot;

                        ((MinecraftClientAccessor) mc).invokeDoAttack();

                        mc.player.getInventory().selectedSlot = prevSlot;
                    } else {
                        mc.player.getInventory().selectedSlot = maceSlot;

                        ((MinecraftClientAccessor) mc).invokeDoAttack();

                        switchTime = System.currentTimeMillis();
                        shouldSwitchBack = true;
                    }
                }
            }
        }
    }

    private int findBreachMaceSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            Item item = stack.getItem();
            if (item instanceof MaceItem && hasBreach(stack)) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasBreach(ItemStack stack) {
        RegistryKey<net.minecraft.enchantment.Enchantment> breachKey = RegistryKey.of(RegistryKeys.ENCHANTMENT,
                Identifier.of("minecraft", "breach"));
        return EnchantmentUtil.hasEnchantment(stack, mc.world, breachKey);
    }

    @Override
    public void onDisable() {
        if (originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
            originalSlot = -1;
        }
        shouldSwitchBack = false;
        isSwappingAttack = false;
    }
}
