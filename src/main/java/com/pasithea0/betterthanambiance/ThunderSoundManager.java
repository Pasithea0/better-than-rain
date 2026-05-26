package com.pasithea0.betterthanambiance;

import net.minecraft.core.world.weather.Weathers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.sound.SoundCategory;

import java.util.Random;

/**
 * Manages thunder sounds that play on random intervals.
 */
public class ThunderSoundManager {
	private static final Random RANDOM = new Random();
	// ticks until next thunder sound
	private static int nextThunderTick = getRandomDelay();
	private static int getRandomDelay() {
		// 30s -> 200s
		// 20 ticks = 1 second
		return (30 * 20) + RANDOM.nextInt((200 - 30) * 20);
	}

	public static void tick(Minecraft mc) {
		if (mc == null || mc.currentWorld == null || mc.thePlayer == null) {
			return;
		}

		// reset timer when not storming
		if (mc.currentWorld.getCurrentWeather() != Weathers.OVERWORLD_STORM) {
			nextThunderTick = getRandomDelay();
			return;
		}

		// optional: scale frequency by storm intensity if available
		float intensity = mc.currentWorld.getWeatherManager().getWeatherIntensity();

		// if storm is weak, reduce chance of thunder entirely
		if (intensity <= 0.4f) {
			return;
		}

		// make thunder more frequent in stronger storms
		nextThunderTick--;

		// intensity bonus: faster ticking in heavy storms
		if (intensity > 0.7f) {
			nextThunderTick--;
		}
		if (intensity > 0.9f) {
			nextThunderTick--;
		}

		if (nextThunderTick > 0) {
			return;
		}

		// extra randomness so it doesn't feel robotic
		if (RANDOM.nextFloat() > (0.3f + intensity * 0.7f)) {
			nextThunderTick = 20 + RANDOM.nextInt(120);
			return;
		}

		playRandomThunder(mc, intensity);

		// reschedule based on intensity (strong storms = faster thunder cycles)
		int baseMin = intensity > 0.8f ? 10 * 20 : 30 * 20;
		int baseMax = intensity > 0.8f ? 90 * 20 : 200 * 20;

		nextThunderTick = baseMin + RANDOM.nextInt(baseMax - baseMin);
	}


	private static void playRandomThunder(Minecraft mc, float intensity) {
		if (mc == null || mc.currentWorld == null || mc.thePlayer == null) {
			return;
		}

		// choose thunder variant (expand later if you add more sounds)
		String sound = "ambient.weather.thunder";

		// scale volume slightly with intensity
		float volume = 0.8f + (intensity * 0.2f);

		// slight pitch variation for realism
		float pitch = 0.9f + RANDOM.nextFloat() * 0.2f;

		mc.sndManager.playSound(
			sound,
			SoundCategory.WEATHER_SOUNDS, // todo make this configurable
			volume,
			pitch
		);
	}
}
