package com.pasithea0.betterthanambiance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.client.option.OptionFloat;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class SoundManager {
    protected static final Random RANDOM = new Random();

    private static final Map<String, OptionFloat> floatOptionCache = new HashMap<>();
    private static final Map<String, OptionBoolean> booleanOptionCache = new HashMap<>();

    private static Object soundSystem;
    private static Method playingMethod;
    private static Method stopMethod;
    private static Method setVolumeMethod;
    private static boolean ssInitialized;

    protected Minecraft mc;
    protected World world;
    protected Player player;

    public final void tick(Minecraft mc) {
        this.mc = mc;
        if (mc != null) {
            this.world = mc.currentWorld;
            this.player = mc.thePlayer;
        } else {
            this.world = null;
            this.player = null;
        }

        if (mc == null || world == null || player == null || mc.isGamePaused) {
            onEnterInvalidState();
            return;
        }

        tickInternal();
    }

    protected void onEnterInvalidState() {
    }

    protected abstract void tickInternal();

    public static OptionFloat getFloatOption(String name) {
        return floatOptionCache.computeIfAbsent(name, key -> {
            for (net.minecraft.client.option.Option<?> option : GameSettings.getAllOptions()) {
                if (option instanceof OptionFloat && option.id.equals(key)) {
                    return (OptionFloat) option;
                }
            }
            return null;
        });
    }

    public static OptionBoolean getBooleanOption(String name) {
        return booleanOptionCache.computeIfAbsent(name, key -> {
            for (net.minecraft.client.option.Option<?> option : GameSettings.getAllOptions()) {
                if (option instanceof OptionBoolean && option.id.equals(key)) {
                    return (OptionBoolean) option;
                }
            }
            return null;
        });
    }

    protected static boolean isWorldSoundCategory() {
        OptionBoolean opt = getBooleanOption("betterthanambiance.useWeatherSounds");
        return opt != null && opt.value;
    }

    protected static int randomBetweenInclusive(int min, int max) {
        if (max <= min) return min;
        return min + RANDOM.nextInt(max - min + 1);
    }

    protected SoundCategory resolveCategory(SoundCategory defaultCategory) {
        return isWorldSoundCategory() ? SoundCategory.WORLD_SOUNDS : defaultCategory;
    }

    protected static float getFloatValue(String name, float fallback) {
        OptionFloat opt = getFloatOption(name);
        return opt != null ? opt.value : fallback;
    }

    protected static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void clearOptionCache() {
        floatOptionCache.clear();
        booleanOptionCache.clear();
    }

    // === Shared SoundSystem access (via public SoundEngine.getSoundSystem()) ===

    protected static void initSoundSystem() {
        if (ssInitialized) return;
        ssInitialized = true;
        try {
            Method getter = SoundEngine.class.getMethod("getSoundSystem");
            soundSystem = getter.invoke(null);
            if (soundSystem == null) return;
            Class<?> ssClass = soundSystem.getClass();
            playingMethod = ssClass.getMethod("playing", String.class);
            stopMethod = ssClass.getMethod("stop", String.class);
            setVolumeMethod = ssClass.getMethod("setVolume", String.class, float.class);
        } catch (Exception e) {
            BetterThanAmbianceMod.LOGGER.warn("Failed to get SoundSystem: {}", e.getMessage());
        }
    }

    protected static boolean isSourcePlaying(String sourceId) {
        if (soundSystem == null || playingMethod == null) return false;
        try {
            return (Boolean) playingMethod.invoke(soundSystem, sourceId);
        } catch (Exception e) {
            return false;
        }
    }

    protected static void stopSource(String sourceId) {
        if (soundSystem == null || stopMethod == null) return;
        try {
            stopMethod.invoke(soundSystem, sourceId);
        } catch (Exception e) {
        }
    }

    protected static void setSourceVolume(String sourceId, float volume) {
        if (soundSystem == null || setVolumeMethod == null) return;
        try {
            setVolumeMethod.invoke(soundSystem, sourceId, volume);
        } catch (Exception e) {
        }
    }
}
