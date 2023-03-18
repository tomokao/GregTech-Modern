package com.lowdragmc.gtceu.api.block;

import com.lowdragmc.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.lowdragmc.gtceu.api.data.RotationState;
import com.lowdragmc.gtceu.api.item.MetaMachineItem;
import com.lowdragmc.gtceu.api.machine.IMetaMachineBlockEntity;
import com.lowdragmc.gtceu.api.machine.MachineDefinition;
import com.lowdragmc.gtceu.api.machine.MetaMachine;
import com.lowdragmc.gtceu.api.machine.feature.*;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/17
 * @implNote GTBlock
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MetaMachineBlock extends Block implements EntityBlock, IBlockRendererProvider {

    public final MachineDefinition definition;
    public final RotationState rotationState;

    public MetaMachineBlock(Properties properties, MachineDefinition definition) {
        super(properties);
        this.definition = definition;
        this.rotationState = RotationState.get();
        if (rotationState != RotationState.NONE) {
            registerDefaultState(defaultBlockState().setValue(rotationState.property, rotationState.defaultDirection));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(BlockProperties.SERVER_TICK);
        RotationState rotationState = RotationState.get();
        if (rotationState != RotationState.NONE) {
            pBuilder.add(rotationState.property);
        }
    }

    @Nullable
    public MetaMachine getMachine(BlockGetter level, BlockPos pos) {
        return MetaMachine.getMachine(level, pos);
    }

    @Nullable
    @Override
    public IRenderer getRenderer(BlockState state) {
        return definition.getRenderer();
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return getRotationState() == RotationState.NONE ? definition.getShape(Direction.NORTH) : definition.getShape(pState.getValue(getRotationState().property));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        var machine = getMachine(level, pos);
        if (machine != null) {
            machine.animateTick(random);
        }
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity player, ItemStack pStack) {
        if (!pLevel.isClientSide) {
            var machine = getMachine(pLevel, pPos);
            if (machine instanceof IDropSaveMachine dropSaveMachine) {
                CompoundTag tag = pStack.getTag();
                if (tag != null) {
                    dropSaveMachine.loadFromItem(tag);
                }
            }
            if (machine instanceof IMachineLife machineLife) {
                machineLife.onMachinePlaced(player, pStack);
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        RotationState rotationState = getRotationState();
        var player = context.getPlayer();
        var blockPos = context.getClickedPos();
        var state = defaultBlockState();
        if (player != null && rotationState != RotationState.NONE) {
            Vec3 pos = player.position();
            if (Math.abs(pos.x - (double) ((float) blockPos.getX() + 0.5F)) < 2.0D && Math.abs(pos.z - (double) ((float) blockPos.getZ() + 0.5F)) < 2.0D) {
                double d0 = pos.y + (double) player.getEyeHeight();
                if (d0 - (double) blockPos.getY() > 2.0D && rotationState.test(Direction.UP)) {
                    return state.setValue(rotationState.property, Direction.UP);
                }
                if ((double) blockPos.getY() - d0 > 0.0D && rotationState.test(Direction.DOWN)) {
                    return state.setValue(rotationState.property, Direction.DOWN);
                }
            }
            if (rotationState == RotationState.Y_AXIS) {
                return state.setValue(rotationState.property, Direction.UP);
            } else {
                return state.setValue(rotationState.property, player.getDirection().getOpposite());
            }
        }
        return state;
    }

    public RotationState getRotationState() {
        return this.rotationState;
    }

    public Direction getFrontFacing(BlockState state) {
        return getRotationState() == RotationState.NONE ? Direction.NORTH : state.getValue(getRotationState().property);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack itemStack = super.getCloneItemStack(level, pos, state);
        if (getMachine(level, pos) instanceof IDropSaveMachine dropSaveMachine) {
            dropSaveMachine.saveToItem(itemStack.getOrCreateTag());
        }
        return itemStack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.addAll(definition.getTooltips());
        String mainKey = String.format("%s.machine.%s.tooltip", definition.getId().getNamespace(), definition.getId().getPath());
        if (LocalizationUtils.exist(mainKey)) {
            tooltip.add(1, Component.translatable(mainKey));
        }
    }

    @Override
    public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
        BlockEntity tile = pLevel.getBlockEntity(pPos);
        if (tile != null) {
            return tile.triggerEvent(pId, pParam);
        }
        return false;
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return super.rotate(pState, pRotation);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        LootContext context = pBuilder.withParameter(LootContextParams.BLOCK_STATE, pState).create(LootContextParamSets.BLOCK);
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        BlockEntity tileEntity = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        var drops = super.getDrops(pState, pBuilder);
        if (tileEntity instanceof IMetaMachineBlockEntity holder) {
            var machine = holder.getMetaMachine();
            if (machine instanceof IMachineModifyDrops machineModifyDrops && entity instanceof Player) {
                machineModifyDrops.onDrops(drops, (Player) entity);
            }
            if (machine instanceof IDropSaveMachine dropSaveMachine) {
                for (ItemStack drop : drops) {
                    if (drop.getItem() instanceof MetaMachineItem item && item.getBlock() == this) {
                        dropSaveMachine.saveToItem(drop.getOrCreateTag());
                    }
                }
            }
        }
        return drops;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.hasBlockEntity()) {
            if (!pState.is(pNewState.getBlock())) { // new block
                if(getMachine(pLevel, pPos) instanceof IMachineLife machineLife) {
                    machineLife.onMachineRemoved();
                }

                pLevel.updateNeighbourForOutputSignal(pPos, this);
                pLevel.removeBlockEntity(pPos);
            } else if (rotationState != RotationState.NONE){ // old block different facing
                var oldFacing = pState.getValue(rotationState.property);
                var newFacing = pNewState.getValue(rotationState.property);
                if (newFacing != oldFacing) {
                    var machine = getMachine(pLevel, pPos);
                    if(machine != null) {
                        machine.onRotated(oldFacing, newFacing);
                    }
                }
            }
        }
    }


    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (getMachine(world, pos) instanceof IInteractedMachine interactedMachine) {
            return interactedMachine.onUse(state, world, pos, player, hand, hit);
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return definition.getBlockEntityType().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (blockEntityType == definition.getBlockEntityType()) {
            if (state.getValue(BlockProperties.SERVER_TICK) && !level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof MetaMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().serverTick();
                    }
                };
            }
            if (level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof MetaMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().clientTick();
                    }
                };
            }
        }
        return null;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (getMachine(level, pos) instanceof IRedstoneSignalMachine redstoneSignalMachine) {
            return redstoneSignalMachine.getOutputSignal(direction);
        }
        return super.getSignal(state, level, pos, direction);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (getMachine(level, pos) instanceof IRedstoneSignalMachine redstoneSignalMachine) {
            return redstoneSignalMachine.getOutputDirectSignal(direction);
        }
        return super.getDirectSignal(state, level, pos, direction);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (getMachine(level, pos) instanceof IRedstoneSignalMachine redstoneSignalMachine) {
            return redstoneSignalMachine.getAnalogOutputSignal();
        }
        return super.getAnalogOutputSignal(state, level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        var machine = getMachine(level, pos);
        if (machine != null) {
            machine.onNeighborChanged(block, fromPos, isMoving);
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    public static int colorTinted(BlockState blockState, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int index) {
        if (level != null && pos != null) {
            var machine = MetaMachine.getMachine(level, pos);
            if (machine != null) {
                return machine.tintColor(index);
            }
        }
        return -1;
    }
}
