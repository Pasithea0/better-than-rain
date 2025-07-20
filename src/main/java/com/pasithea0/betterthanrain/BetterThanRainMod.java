package com.pasithea0.betterthanrain;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.sound.SoundRepository;
import net.minecraft.core.sound.SoundTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.ClientStartEntrypoint;
import turniplabs.halplibe.util.GameStartEntrypoint;
import turniplabs.halplibe.util.RecipeEntrypoint;

public class BetterThanRainMod implements ModInitializer, RecipeEntrypoint, GameStartEntrypoint, ClientStartEntrypoint {
    public static final String MOD_ID = "betterthanrain";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("✅ Better than Rain! mod initialized.");
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
        LOGGER.info("✅ Better than Rain! client initialized.");
    }

    @Override
    public void afterClientStart() {
    }
}
