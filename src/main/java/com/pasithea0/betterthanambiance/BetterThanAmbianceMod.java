package com.pasithea0.betterthanambiance;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.sound.SoundRepository;
import net.minecraft.core.sound.SoundTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.ClientStartEntrypoint;
import turniplabs.halplibe.util.GameStartEntrypoint;
import turniplabs.halplibe.util.RecipeEntrypoint;
import turniplabs.halplibe.util.OptionsInitEntrypoint;
import net.minecraft.client.option.OptionFloat;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.client.option.GameSettings;

public class BetterThanAmbianceMod implements ModInitializer, RecipeEntrypoint, GameStartEntrypoint, ClientStartEntrypoint, OptionsInitEntrypoint {
    public static final String MOD_ID = "betterthanambiance";
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
        SoundRepository.registerNamespace(MOD_ID);
        LOGGER.info("Better Than Ambiance client initialized.");
    }

    @Override
    public void afterClientStart() {
    }

    @Override
    public void initOptions(GameSettings settings) {
        // Rain
        new OptionFloat(settings, "betterthanambiance.metalRainVolume", 1.0f);
        new OptionFloat(settings, "betterthanambiance.metalMuffledVolume", 1.0f);
        new OptionFloat(settings, "betterthanambiance.glassRainVolume", 1.0f);
        new OptionFloat(settings, "betterthanambiance.glassMuffledVolume", 1.0f);
        new OptionFloat(settings, "betterthanambiance.fabricRainVolume", 1.0f);
        new OptionFloat(settings, "betterthanambiance.fabricMuffledVolume", 1.0f);
        new OptionFloat(settings, "betterthanambiance.lavaRainVolume", 0.8f);
        new OptionFloat(settings, "betterthanambiance.lavaMuffledVolume", 1.0f);
        new OptionFloat(settings, "betterthanambiance.foliageRainVolume", 0.8f);
        new OptionFloat(settings, "betterthanambiance.foliageMuffledVolume", 1.0f);
        new OptionFloat(settings, "betterthanambiance.waterRainVolume", 0.7f);
        new OptionFloat(settings, "betterthanambiance.waterMuffledVolume", 1.0f);
        new OptionFloat(settings, "betterthanambiance.noteblockRainVolume", 0.5f);
        new OptionFloat(settings, "betterthanambiance.noteblockMuffledVolume", 1.0f);
        new OptionFloat(settings, "betterthanambiance.stoneRainVolume", 0.5f);
        new OptionFloat(settings, "betterthanambiance.stoneMuffledVolume", 1.0f);
        new OptionFloat(settings, "betterthanambiance.woodRainVolume", 0.5f);
        new OptionFloat(settings, "betterthanambiance.woodMuffledVolume", 1.0f);
        new OptionFloat(settings, "betterthanambiance.plasticRainVolume", 0.8f);
        new OptionFloat(settings, "betterthanambiance.plasticMuffledVolume", 1.0f);
        new OptionBoolean(settings, "betterthanambiance.useWeatherSounds", false);

        // Thunder
        new OptionFloat(settings, "betterthanambiance.thunderVolume", 0.5f);

        // Crickets / night ambience
        new OptionFloat(settings, "betterthanambiance.cricketsVolume", 1.0f);
    }
}
