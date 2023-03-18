package com.lowdragmc.gtceu.api.machine.steam;

import com.lowdragmc.gtceu.api.machine.IMetaMachineBlockEntity;
import com.lowdragmc.gtceu.api.machine.MetaMachine;
import com.lowdragmc.gtceu.api.machine.feature.ITieredMachine;
import com.lowdragmc.gtceu.api.machine.trait.NotifiableFluidTank;
import com.lowdragmc.gtceu.common.libs.GTFluids;
import com.lowdragmc.gtceu.common.libs.GTMaterials;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/14
 * @implNote SteamMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class SteamMachine extends MetaMachine implements ITieredMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(SteamMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);

    @Getter
    public final boolean isHighPressure;
    @Persisted
    public final NotifiableFluidTank steamTank;

    public SteamMachine(IMetaMachineBlockEntity holder, boolean isHighPressure, Object... args) {
        super(holder);
        this.isHighPressure = isHighPressure;
        this.steamTank = createSteamTank(args);
        this.steamTank.setFilter(fluid -> GTMaterials.Steam.getFluid() == fluid.getFluid());
    }

    //////////////////////////////////////
    //*****     Initialization     *****//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public int getTier() {
        return isHighPressure ? 1 : 0;
    }

    protected abstract NotifiableFluidTank createSteamTank(Object... args);
}
