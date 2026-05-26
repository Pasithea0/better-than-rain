package com.pasithea0.betterthanambiance;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.sound.SoundTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.HalpLibe;
import turniplabs.halplibe.util.ClientStartEntrypoint;
import turniplabs.halplibe.util.GameStartEntrypoint;
import turniplabs.halplibe.util.RecipeEntrypoint;
import turniplabs.halplibe.util.OptionsInitEntrypoint;
import net.minecraft.client.option.OptionFloat;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.client.option.GameSettings;

public class BetterThanAmbianceMod implements ModInitializer, RecipeEntrypoint, GameStartEntrypoint, ClientStartEntrypoint, OptionsInitEntrypoint {
    public static final String MOD_ID = HalpLibe.registerMod("betterthanambiance", true);
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Better Than Ambiance mod initialized.");
    }

    @Override
    public void onRecipesReady() {
    }

    @Override
    public void initNamespaces() {
        net.minecraft.core.lang.I18n.initialize(MOD_ID);
    }

    @Override
    public void beforeGameStart() {
        SoundTypes.loadSoundsJson(MOD_ID);
    }

    @Override
    public void afterGameStart() {
    }

    @Override
    public void beforeClientStart() {
        net.minecraft.client.sound.SoundRepository.namespaceAdded(MOD_ID);
        LOGGER.info("Better Than Ambiance client initialized.");
    }

    @Override
    public void afterClientStart() {
    }

    public void initOptions() {
        registerOptions();
    }


    private static void registerOptions() {
        // Rain
        GameSettings.register(new OptionFloat("betterthanambiance.metalRainVolume", 1.0f));
        GameSettings.register(new OptionFloat("betterthanambiance.metalMuffledVolume", 1.0f));
        GameSettings.register(new OptionFloat("betterthanambiance.glassRainVolume", 1.0f));
        GameSettings.register(new OptionFloat("betterthanambiance.glassMuffledVolume", 1.0f));
        GameSettings.register(new OptionFloat("betterthanambiance.fabricRainVolume", 1.0f));
        GameSettings.register(new OptionFloat("betterthanambiance.fabricMuffledVolume", 1.0f));
        GameSettings.register(new OptionFloat("betterthanambiance.lavaRainVolume", 0.8f));
        GameSettings.register(new OptionFloat("betterthanambiance.lavaMuffledVolume", 1.0f));
        GameSettings.register(new OptionFloat("betterthanambiance.foliageRainVolume", 0.8f));
        GameSettings.register(new OptionFloat("betterthanambiance.foliageMuffledVolume", 1.0f));
        GameSettings.register(new OptionFloat("betterthanambiance.waterRainVolume", 0.7f));
        GameSettings.register(new OptionFloat("betterthanambiance.waterMuffledVolume", 1.0f));
        GameSettings.register(new OptionFloat("betterthanambiance.noteblockRainVolume", 0.5f));
        GameSettings.register(new OptionFloat("betterthanambiance.noteblockMuffledVolume", 1.0f));
        GameSettings.register(new OptionFloat("betterthanambiance.stoneRainVolume", 0.5f));
        GameSettings.register(new OptionFloat("betterthanambiance.stoneMuffledVolume", 1.0f));
        GameSettings.register(new OptionFloat("betterthanambiance.woodRainVolume", 0.5f));
        GameSettings.register(new OptionFloat("betterthanambiance.woodMuffledVolume", 1.0f));
        GameSettings.register(new OptionFloat("betterthanambiance.plasticRainVolume", 0.8f));
        GameSettings.register(new OptionFloat("betterthanambiance.plasticMuffledVolume", 1.0f));
        GameSettings.register(new OptionBoolean("betterthanambiance.useWeatherSounds", false));

        // Thunder
        GameSettings.register(new OptionFloat("betterthanambiance.thunderVolume", 0.5f));

        // Crickets / night ambience
        GameSettings.register(new OptionFloat("betterthanambiance.cricketsVolume", 1.0f));
    }
}
