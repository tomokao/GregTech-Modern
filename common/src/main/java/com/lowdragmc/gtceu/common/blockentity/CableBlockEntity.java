package com.lowdragmc.gtceu.common.blockentity;

import com.lowdragmc.gtceu.api.blockentity.PipeBlockEntity;
import com.lowdragmc.gtceu.api.capability.GTCapabilityHelper;
import com.lowdragmc.gtceu.api.capability.ICoverable;
import com.lowdragmc.gtceu.api.capability.IEnergyContainer;
import com.lowdragmc.gtceu.api.item.tool.GTToolType;
import com.lowdragmc.gtceu.common.block.CableBlock;
import com.lowdragmc.gtceu.common.pipelike.cable.CableData;
import com.lowdragmc.gtceu.common.pipelike.cable.EnergyNet;
import com.lowdragmc.gtceu.common.pipelike.cable.EnergyNetHandler;
import com.lowdragmc.gtceu.common.pipelike.cable.Insulation;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;

/**
 * @author KilaBash
 * @date 2023/3/1
 * @implNote CableBlockEntity
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CableBlockEntity extends PipeBlockEntity<Insulation, CableData> {
    protected WeakReference<EnergyNet> currentEnergyNet = new WeakReference<>(null);

    public CableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @ExpectPlatform
    public static CableBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        throw new AssertionError();
    }

    @Override
    public boolean canAttachTo(Direction side) {
        if (level != null) {
            if (level.getBlockEntity(getBlockPos().relative(side)) instanceof CableBlockEntity) {
                return false;
            }
            return GTCapabilityHelper.getEnergyContainer(level, getBlockPos().relative(side), side.getOpposite()) != null;
        }
        return false;
    }

    @Nullable
    private EnergyNet getEnergyNet() {
        if (level instanceof ServerLevel serverLevel && getBlockState().getBlock() instanceof CableBlock cableBlock) {
            EnergyNet currentEnergyNet = this.currentEnergyNet.get();
            if (currentEnergyNet != null && currentEnergyNet.isValid() && currentEnergyNet.containsNode(getBlockPos()))
                return currentEnergyNet; //return current net if it is still valid
            currentEnergyNet = cableBlock.getWorldPipeNet(serverLevel).getNetFromPos(getBlockPos());
            if (currentEnergyNet != null) {
                this.currentEnergyNet = new WeakReference<>(currentEnergyNet);
            }
        }
        return this.currentEnergyNet.get();
    }

    public IEnergyContainer getEnergyContainer() {
        var ENet = getEnergyNet();
        if (ENet != null) {
            return new EnergyNetHandler(ENet, this);
        }
        return IEnergyContainer.DEFAULT;
    }

    @ExpectPlatform
    public static void onBlockEntityRegister(BlockEntityType<CableBlockEntity> cableBlockEntityBlockEntityType) {
        throw new AssertionError();
    }

    //////////////////////////////////////
    //*******     Interaction    *******//
    //////////////////////////////////////
    @Override
    public boolean shouldRenderGrid(Player player, ItemStack held, GTToolType toolType) {
        return super.shouldRenderGrid(player, held, toolType) || toolType == GTToolType.WIRE_CUTTER;
    }

    @Override
    public boolean isSideUsed(Player player, GTToolType toolType, Direction side) {
        if (toolType == GTToolType.WIRE_CUTTER) {
            return isBlocked(side);
        } else {
            return super.isSideUsed(player, toolType, side);
        }
    }

    @Override
    public InteractionResult onToolClick(@NotNull GTToolType toolType, ItemStack itemStack, UseOnContext context) {
        // the side hit from the machine grid
        var playerIn = context.getPlayer();
        if (playerIn == null) return InteractionResult.PASS;

        var hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), false);
        Direction gridSide = ICoverable.determineGridSideHit(hitResult);
        if (gridSide == null) gridSide = hitResult.getDirection();

        if (toolType == GTToolType.WIRE_CUTTER) {
            setBlocked(gridSide, !isBlocked(gridSide));
            return InteractionResult.CONSUME;
        }

        return super.onToolClick(toolType, itemStack, context);
    }
}
