package com.gregtechceu.gtceu.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.api.gui.factory.CoverUIFactory;
import com.gregtechceu.gtceu.api.gui.factory.GTUIEditorFactory;
import com.gregtechceu.gtceu.api.gui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.common.item.tool.forge.ToolLootModifier;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.GregTechDatagen;
import com.gregtechceu.gtceu.integration.top.forge.TheOneProbePluginImpl;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CommonProxy {
    private static final Object LOCK = new Object();
    private static boolean isKubJSSetup = false;

    public CommonProxy() {
        // used for forge events (ClientProxy + CommonProxy)
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(this);
        GTRegistries.init(eventBus);
        GTFeatures.init(eventBus);
        // init common features
        if (GTCEu.isKubeJSLoaded()) {
            synchronized (LOCK) {
                if (!isKubJSSetup) {
                    try { LOCK.wait(); } catch (InterruptedException ignored) {}
                }
            }
        }
        CommonProxy.init();
        GTRegistries.GLOBAL_LOOT_MODIFIES.register("tool", () -> ToolLootModifier.CODEC);
    }

    /**
     * If kjs is loaded, make sure our mod is loaded after it.
     */
    public static void onKubeJSSetup() {
        synchronized (LOCK) {
            isKubJSSetup = true;
            LOCK.notify();
        }
    }

    public static void init() {
        GTCEu.LOGGER.info("GTCEu common proxy init!");
        ConfigHolder.init();
        GTCEu.initializeHighTier();

        UIFactory.register(MachineUIFactory.INSTANCE);
        UIFactory.register(CoverUIFactory.INSTANCE);
        UIFactory.register(GTUIEditorFactory.INSTANCE);
        GTPlacerTypes.init();
        GTRecipeCapabilities.init();
        GTRecipeConditions.init();
        GTElements.init();
        MaterialIconSet.init();
        MaterialIconType.init();
        GTMaterials.init();
        TagPrefix.init();
        GTSoundEntries.init();
        GTDamageTypes.init();
        GTCompassSections.init();
        GTCompassNodes.init();
        GTCovers.init();
        GTFluids.init();
        GTCreativeModeTabs.init();
        GTBlocks.init();
        GTBlockEntities.init();
        GTRecipeTypes.init();
        GTMachines.init();
        GTFoods.init();
        GTItems.init();
        GregTechDatagen.init();
        AddonFinder.getAddons().forEach(IGTAddon::initializeAddon);

        // fabric exclusive, squeeze this in here to register before stuff is used
        GTRegistries.REGISTRATE.registerRegistrate();
        WorldGenLayers.registerAll();
        GTFeatures.init();
        GTFeatures.register();
    }


    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {

        });
        CraftingHelper.register(SizedIngredient.TYPE, SizedIngredient.SERIALIZER);
    }

    @SubscribeEvent
    public void loadComplete(FMLLoadCompleteEvent e) {
        e.enqueueWork(() -> {
            if (LDLib.isModLoaded(GTValues.MODID_TOP)) {
                GTCEu.LOGGER.info("TheOneProbe found. Enabling integration...");
                TheOneProbePluginImpl.init();
            }
        });
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        GTCapability.register(event);
    }
}
