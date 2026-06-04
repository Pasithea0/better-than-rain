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
        boolean isClose = RANDOM.nextFloat() < 0.6f;
        String sound = isClose ? BetterThanAmbianceSounds.THUNDER_CLOSE : BetterThanAmbianceSounds.THUNDER_DISTANT;

        float baseVolume = 0.8f + (intensity * 0.2f);
        baseVolume *= getFloatValue("betterthanambiance.thunderVolume", 1.0f);

        float pitch = 0.9f + RANDOM.nextFloat() * 0.2f;

        SoundCategory category = resolveCategory(SoundCategory.WEATHER_SOUNDS);

        float range = isClose ? 16.0f : 48.0f;
        float x = (float) player.x + (RANDOM.nextFloat() - 0.5f) * range * 2;
        float z = (float) player.z + (RANDOM.nextFloat() - 0.5f) * range * 2;
        float y = (float) player.y;

        mc.sndManager.playSoundAt(sound, category, x, y, z, baseVolume, pitch);
    }
}
