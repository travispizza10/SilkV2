package cc.silk.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ItemUtil {
    
    public static boolean isFood(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        return stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE || 
               stack.getItem() == Items.BREAD || stack.getItem() == Items.COOKED_BEEF || 
               stack.getItem() == Items.COOKED_PORKCHOP || stack.getItem() == Items.COOKED_CHICKEN ||
               stack.getItem() == Items.COOKED_MUTTON || stack.getItem() == Items.COOKED_RABBIT ||
               stack.getItem() == Items.COOKED_SALMON || stack.getItem() == Items.COOKED_COD ||
               stack.getItem() == Items.APPLE || stack.getItem() == Items.CARROT ||
               stack.getItem() == Items.POTATO || stack.getItem() == Items.BAKED_POTATO ||
               stack.getItem() == Items.MELON_SLICE || stack.getItem() == Items.SWEET_BERRIES ||
               stack.getItem() == Items.GLOW_BERRIES || stack.getItem() == Items.CHORUS_FRUIT ||
               stack.getItem() == Items.DRIED_KELP || stack.getItem() == Items.HONEY_BOTTLE ||
               stack.getItem() == Items.MUSHROOM_STEW || stack.getItem() == Items.RABBIT_STEW ||
               stack.getItem() == Items.BEETROOT_SOUP || stack.getItem() == Items.SUSPICIOUS_STEW ||
               stack.getItem() == Items.PUMPKIN_PIE || stack.getItem() == Items.CAKE;
    }
}
