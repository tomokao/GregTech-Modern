package com.lowdragmc.gtceu.api.item;

import com.lowdragmc.gtceu.api.block.MetaMachineBlock;
import com.lowdragmc.gtceu.api.machine.MachineDefinition;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote MetaMachineItem
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MetaMachineItem extends BlockItem implements IItemRendererProvider {

    public MetaMachineItem(MetaMachineBlock block, Properties properties) {
        super(block, properties);
    }

    public MachineDefinition getDefinition() {
        return ((MetaMachineBlock)getBlock()).definition;
    }

    @Nullable
    @Override
    public IRenderer getRenderer(ItemStack stack) {
        return ((MetaMachineBlock)getBlock()).definition.getRenderer();
    }

}
