package cc.silk.utils.mc;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

public final class EnchantmentUtil {

    public static boolean hasEnchantment(ItemStack stack, World world, RegistryEntry<Enchantment> entry) {
        if (stack == null || world == null || entry == null) return false;
        ItemEnchantmentsComponent ench = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (ench == null) return false;
        return ench.getLevel(entry) > 0;
    }

    public static boolean hasEnchantment(ItemStack stack, World world, Enchantment enchantment) {
        if (stack == null || world == null || enchantment == null) return false;
        ItemEnchantmentsComponent ench = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (ench == null) return false;
        try {
            Registry<Enchantment> reg = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            net.minecraft.registry.entry.RegistryEntry<Enchantment> entryRef = reg.getEntry(enchantment);
            if (entryRef != null) {
                return ench.getLevel(entryRef) > 0;
            }
        } catch (Throwable ignored) {
        }
        try {
            Registry<?> reg = (Registry<?>) world.getRegistryManager().getClass().getMethod("get", RegistryKey.class)
                    .invoke(world.getRegistryManager(), RegistryKeys.ENCHANTMENT);
            Object entry = reg.getClass().getMethod("getEntry", Object.class).invoke(reg, enchantment);
            Integer level = (Integer) ench.getClass().getMethod("getLevel", entry.getClass()).invoke(ench, entry);
            return level != null && level > 0;
        } catch (Throwable ignored) {
        }
        try {
            Integer level = (Integer) ench.getClass().getMethod("getLevel", Enchantment.class).invoke(ench, enchantment);
            return level != null && level > 0;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static boolean hasEnchantment(ItemStack stack, World world, RegistryKey<Enchantment> enchantmentKey) {
        if (stack == null || world == null || enchantmentKey == null) return false;
        ItemEnchantmentsComponent ench = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (ench == null) return false;
        try {
            Registry<Enchantment> reg = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            Enchantment enchInstance = reg.get(enchantmentKey);
            if (enchInstance != null) {
                RegistryEntry<Enchantment> entryRef = reg.getEntry(enchInstance);
                if (entryRef != null) {
                    return ench.getLevel(entryRef) > 0;
                }
            }
        } catch (Throwable ignored) {
        }

        try {
            Object reg = world.getRegistryManager().getClass().getMethod("get", RegistryKey.class)
                    .invoke(world.getRegistryManager(), RegistryKeys.ENCHANTMENT);
            Object opt = reg.getClass().getMethod("getOrEmpty", RegistryKey.class).invoke(reg, enchantmentKey);
            java.util.Optional<?> optional = (java.util.Optional<?>) opt;
            if (optional.isPresent()) {
                Object enchantment = optional.get();
                Object entry = reg.getClass().getMethod("getEntry", Object.class).invoke(reg, enchantment);
                return hasEnchantment(stack, world, (RegistryEntry<Enchantment>) entry);
            }
        } catch (Throwable ignored) {
        }
        return false;
    }
}