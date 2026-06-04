package com.pasithea0.betterthanambiance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.client.option.OptionFloat;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class SoundManager {
    protected static final Random RANDOM = new Random();

    private static final Map<String, OptionFloat> floatOptionCache = new HashMap<>();
    private static final Map<String, OptionBoolean> booleanOptionCache = new HashMap<>();

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
}
