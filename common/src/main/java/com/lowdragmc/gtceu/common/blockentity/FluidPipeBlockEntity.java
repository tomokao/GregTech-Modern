package com.lowdragmc.gtceu.common.blockentity;

import com.lowdragmc.gtceu.api.blockentity.PipeBlockEntity;
import com.lowdragmc.gtceu.common.block.FluidPipeBlock;
import com.lowdragmc.gtceu.common.cover.FluidFilterCover;
import com.lowdragmc.gtceu.common.pipelike.fluidpipe.FluidPipeData;
import com.lowdragmc.gtceu.common.pipelike.fluidpipe.FluidPipeNet;
import com.lowdragmc.gtceu.common.pipelike.fluidpipe.FluidPipeType;
import com.lowdragmc.gtceu.common.pipelike.fluidpipe.FluidTransferHandler;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
public class FluidPipeBlockEntity extends PipeBlockEntity<FluidPipeType, FluidPipeData> {
    protected WeakReference<FluidPipeNet> currentFluidPipeNet = new WeakReference<>(null);

    public FluidPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @ExpectPlatform
    public static FluidPipeBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onBlockEntityRegister(BlockEntityType<FluidPipeBlockEntity> cableBlockEntityBlockEntityType) {
        throw new AssertionError();
    }

    @Override
    public boolean canAttachTo(Direction side) {
        if (level != null) {
            if (level.getBlockEntity(getBlockPos().relative(side)) instanceof FluidPipeBlockEntity) {
                return false;
            }
            return FluidTransferHelper.getFluidTransfer(level, getBlockPos().relative(side), side.getOpposite()) != null;
        }
        return false;
    }

    @Nullable
    private FluidPipeNet getFluidPipeNet() {
        if (level instanceof ServerLevel serverLevel && getBlockState().getBlock() instanceof FluidPipeBlock fluidPipeBlock) {
            FluidPipeNet currentFluidPipeNet = this.currentFluidPipeNet.get();
            if (currentFluidPipeNet != null && currentFluidPipeNet.isValid() && currentFluidPipeNet.containsNode(getBlockPos()))
                return currentFluidPipeNet; //return current net if it is still valid
            currentFluidPipeNet = fluidPipeBlock.getWorldPipeNet(serverLevel).getNetFromPos(getBlockPos());
            if (currentFluidPipeNet != null) {
                this.currentFluidPipeNet = new WeakReference<>(currentFluidPipeNet);
            }
        }
        return this.currentFluidPipeNet.get();
    }

    @Nullable
    public IFluidTransfer getFluidHandler(@Nullable Direction side) {
        var net = getFluidPipeNet();
        if (net != null) {
            var transfer = new FluidTransferHandler(net, this, side);
            if (side != null) {
                if (getCoverContainer().getCoverAtSide(side) instanceof FluidFilterCover cover) {
                    transfer.setFilter(cover.getFluidFilter());
                }
            }
            return transfer;
        }
        return null;
    }
}
