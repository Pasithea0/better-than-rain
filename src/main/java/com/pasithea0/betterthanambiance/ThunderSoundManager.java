package com.pasithea0.betterthanambiance;

import net.minecraft.core.world.weather.Weathers;
import net.minecraft.core.sound.SoundCategory;

public class ThunderSoundManager extends SoundManager {
    public static final ThunderSoundManager INSTANCE = new ThunderSoundManager();

    private int nextThunderTick = getRandomDelay();

    private static int getRandomDelay() {
        return (30 * 20) + RANDOM.nextInt((200 - 30) * 20);
    }

    @Override
    protected void tickInternal() {
        if (world.getCurrentWeather() != Weathers.OVERWORLD_STORM) {
            nextThunderTick = getRandomDelay();
            return;
        }

        float intensity = world.getWeatherManager().getWeatherIntensity();

        if (intensity <= 0.4f) {
            return;
        }

        nextThunderTick--;

        if (intensity > 0.7f) {
            nextThunderTick--;
        }
        if (intensity > 0.9f) {
            nextThunderTick--;
        }

        if (nextThunderTick > 0) {
            return;
        }

        if (RANDOM.nextFloat() > (0.3f + intensity * 0.7f)) {
            nextThunderTick = 20 + RANDOM.nextInt(120);
            return;
        }

        playRandomThunder(intensity);

        int baseMin = intensity > 0.8f ? 10 * 20 : 30 * 20;
        int baseMax = intensity > 0.8f ? 90 * 20 : 200 * 20;

        nextThunderTick = baseMin + RANDOM.nextInt(baseMax - baseMin);
    }

    private void playRandomThunder(float intensity) {
        String sound = "ambient.weather.thunder";

        float volume = 0.8f + (intensity * 0.2f);

        float pitch = 0.9f + RANDOM.nextFloat() * 0.2f;

        mc.sndManager.playSound(
            sound,
            SoundCategory.WEATHER_SOUNDS,
            volume,
            pitch
        );
    }
}
