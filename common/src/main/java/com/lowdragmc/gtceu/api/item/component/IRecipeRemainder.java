package com.lowdragmc.gtceu.api.item.component;

import net.minecraft.world.item.ItemStack;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote IRecipeRemainder
 */
public interface IRecipeRemainder extends IItemComponent {
    ItemStack getRecipeRemained(ItemStack itemStack);
}
