package com.lowdragmc.gtceu.api.data.chemical.material.properties;

import com.lowdragmc.gtceu.GTCEu;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class PlasmaProperty implements IMaterialProperty<PlasmaProperty> {
    private Supplier<? extends Fluid> plasmaSupplier;
    @Setter @Getter
    private ResourceLocation stillTexture, flowTexture;

    public PlasmaProperty() {
        stillTexture = GTCEu.id("block/fluids/fluid.plasma.autogenerated");
        flowTexture = stillTexture;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
    }

    @Nullable
    public Fluid getPlasma() {
        return plasmaSupplier == null ? null : plasmaSupplier.get();
    }

    public void setPlasma(Supplier<? extends Fluid> plasma) {
        this.plasmaSupplier = plasma;
    }

    public boolean hasPlasmaSupplier() {
        return plasmaSupplier != null;
    }

    @Nullable
    public FluidStack getPlasma(int amount) {
        var fluid = getPlasma();
        return fluid == null ? null : FluidStack.create(fluid, amount);
    }

}
