package com.pasithea0.betterthanrain;

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

public class BetterThanRainMod implements ModInitializer, RecipeEntrypoint, GameStartEntrypoint, ClientStartEntrypoint, OptionsInitEntrypoint {
    public static final String MOD_ID = "betterthanrain";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Better than Rain! mod initialized.");
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
        LOGGER.info("Better than Rain! client initialized.");
    }

    @Override
    public void afterClientStart() {
    }

    @Override
    public void initOptions(GameSettings settings) {
        new OptionFloat(settings, "betterthanrain.masterRainVolume", 1.0f);
        new OptionFloat(settings, "betterthanrain.muffledVolume", 1.0f);
        new OptionFloat(settings, "betterthanrain.metalRainVolume", 1.0f);
        new OptionFloat(settings, "betterthanrain.glassRainVolume", 1.0f);
        new OptionFloat(settings, "betterthanrain.fabricRainVolume", 1.0f);
        new OptionFloat(settings, "betterthanrain.lavaRainVolume", 0.8f);
        new OptionFloat(settings, "betterthanrain.foliageRainVolume", 0.8f);
        new OptionFloat(settings, "betterthanrain.waterRainVolume", 0.7f);
        new OptionFloat(settings, "betterthanrain.noteblockRainVolume", 0.5f);
        new OptionFloat(settings, "betterthanrain.stoneRainVolume", 0.5f);
        new OptionFloat(settings, "betterthanrain.woodRainVolume", 0.5f);
        new OptionFloat(settings, "betterthanrain.plasticRainVolume", 0.8f);
        new OptionBoolean(settings, "betterthanrain.useWeatherSounds", false);
    }
}
