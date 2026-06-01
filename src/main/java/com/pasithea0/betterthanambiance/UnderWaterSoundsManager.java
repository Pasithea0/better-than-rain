package com.pasithea0.betterthanambiance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.client.option.OptionFloat;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UnderWaterSoundsManager {
	private static final Random RANDOM = new Random();
	private static final Map<String, OptionFloat> cachedFloatOptions = new HashMap<>();
	private static final Map<String, OptionBoolean> cachedBooleanOptions = new HashMap<>();
	private static final Map<String, Boolean> cachedVorbisAssets = new HashMap<>();
	private static final Map<String, Boolean> cachedVorbisWarnings = new HashMap<>();

	// Reflection access to underlying SoundSystem
	private static Object soundSystemInstance = null;
	private static java.lang.reflect.Method playingMethod = null;
	private static java.lang.reflect.Method stopMethod = null;
	private static java.lang.reflect.Method setVolumeMethod = null;
	private static boolean reflectionInitialized = false;

	private static final float ADDITIONS_CHANCE = 0.70f;
	private static final float RARE_CHANCE = 0.09f;
	private static final float ULTRA_RARE_CHANCE = 0.01f;

	private static final int LOOP_INTERVAL_MIN_TICKS = 360;
	private static final int LOOP_INTERVAL_MAX_TICKS = 420;

	private static final int ADDITIONS_INTERVAL_MIN_TICKS = 80;
	private static final int ADDITIONS_INTERVAL_MAX_TICKS = 140;

	private static final int RARE_INTERVAL_MIN_TICKS = 160;
	private static final int RARE_INTERVAL_MAX_TICKS = 260;

	private static final int ULTRA_RARE_INTERVAL_MIN_TICKS = 320;
	private static final int ULTRA_RARE_INTERVAL_MAX_TICKS = 520;

	// Depth at which ambience starts to become audible
	private static final int MIN_DEPTH_FOR_AMBIENCE = 5;
	// Depth at which ambience reaches full volume
	private static final int FULL_DEPTH_FOR_AMBIENCE = 20;

	private static final String[] LOOP_AMBIENCE_SOUNDS = {
		BetterThanAmbianceSounds.UNDERWATER_AMBIENCE
	};

	private static final String[] ADDITION_SOUNDS = {
		BetterThanAmbianceSounds.BUBBLES_1,
		BetterThanAmbianceSounds.BUBBLES_2,
		BetterThanAmbianceSounds.BUBBLES_3,
		BetterThanAmbianceSounds.BUBBLES_4,
		BetterThanAmbianceSounds.BUBBLES_5,
		BetterThanAmbianceSounds.BUBBLES_6,
		BetterThanAmbianceSounds.UNDERWATER_ANIMAL_1
	};

	private static final String[] RARE_SOUNDS = {
		BetterThanAmbianceSounds.UNDERWATER_BASS_WHALE_1,
		BetterThanAmbianceSounds.UNDERWATER_BASS_WHALE_2,
		BetterThanAmbianceSounds.UNDERWATER_CRACKLES_1,
		BetterThanAmbianceSounds.UNDERWATER_CRACKLES_2,
		BetterThanAmbianceSounds.UNDERWATER_DRIPLETS_1,
		BetterThanAmbianceSounds.UNDERWATER_DRIPLETS_2,
		BetterThanAmbianceSounds.UNDERWATER_EARTH_CRACK,
		BetterThanAmbianceSounds.UNDERWATER_ANIMAL_2
	};

	private static final String[] ULTRA_RARE_SOUNDS = {
		BetterThanAmbianceSounds.UNDERWATER_DARK_1,
		BetterThanAmbianceSounds.UNDERWATER_DARK_2,
		BetterThanAmbianceSounds.UNDERWATER_DARK_3,
		BetterThanAmbianceSounds.UNDERWATER_DARK_4
	};

	// Sound ID for the ambience loop we manage
	private static final String AMBIENCE_LOOP_ID = "betterthanambiance_underwater_ambience";

	private static boolean wasUnderwater = false;
	private static int loopAmbienceCooldownTicks = 0;
	private static int additionsCooldownTicks = 0;
	private static int rareCooldownTicks = 0;
	private static int ultraRareCooldownTicks = 0;

	// Current fade volume for ambience loop (0.0 to 1.0)
	private static float ambienceFadeVolume = 0.0f;
	// Target fade volume based on depth
	private static float ambienceTargetVolume = 0.0f;
	// Fade speed per tick - slower for smoother fade
	private static final float FADE_IN_SPEED = 0.005f;
	private static final float FADE_OUT_SPEED = 0.02f;

	private static void initReflection(SoundEngine snd) {
		if (reflectionInitialized) return;
		reflectionInitialized = true;
		try {
			Field[] fields = SoundEngine.class.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				Object value = field.get(snd);
				if (value != null && value.getClass().getName().contains("SoundSystem")) {
					soundSystemInstance = value;
					Class<?> soundSystemClass = value.getClass();

					playingMethod = soundSystemClass.getMethod("playing", String.class);
					stopMethod = soundSystemClass.getMethod("stop", String.class);
					setVolumeMethod = soundSystemClass.getMethod("setVolume", String.class, float.class);

					BetterThanAmbianceMod.LOGGER.info("Successfully hooked into SoundSystem via reflection");
					return;
				}
			}
			BetterThanAmbianceMod.LOGGER.warn("Could not find SoundSystem field in SoundEngine");
		} catch (Exception e) {
			BetterThanAmbianceMod.LOGGER.warn("Failed to initialize SoundSystem reflection: {}", e.getMessage());
		}
	}

	private static boolean isSoundPlaying() {
		if (soundSystemInstance == null || playingMethod == null) return false;
		try {
			return (Boolean) playingMethod.invoke(soundSystemInstance, AMBIENCE_LOOP_ID);
		} catch (Exception e) {
			return false;
		}
	}

	private static void stopSound() {
		if (soundSystemInstance == null || stopMethod == null) return;
		try {
			stopMethod.invoke(soundSystemInstance, AMBIENCE_LOOP_ID);
		} catch (Exception e) {
			// Ignore
		}
	}

	private static void setSoundVolume(float volume) {
		if (soundSystemInstance == null || setVolumeMethod == null) return;
		try {
			setVolumeMethod.invoke(soundSystemInstance, AMBIENCE_LOOP_ID, volume);
		} catch (Exception e) {
			// Ignore
		}
	}

	public static void tick(Minecraft mc) {
		if (mc == null || mc.isGamePaused || mc.currentWorld == null || mc.thePlayer == null) {
			reset();
			return;
		}

		if (!isUnderwaterEnabled()) {
			reset();
			return;
		}

		initReflection(mc.sndManager);

		World world = mc.currentWorld;
		Player player = mc.thePlayer;

		boolean underwater = isPlayerHeadUnderwater(world, player);
		if (!underwater) {
			if (wasUnderwater) {
				reset();
			}
			wasUnderwater = false;
			return;
		}

		if (!wasUnderwater) {
			loopAmbienceCooldownTicks = 0;
			additionsCooldownTicks = randomBetweenInclusive(ADDITIONS_INTERVAL_MIN_TICKS, ADDITIONS_INTERVAL_MAX_TICKS);
			rareCooldownTicks = randomBetweenInclusive(RARE_INTERVAL_MIN_TICKS, RARE_INTERVAL_MAX_TICKS);
			ultraRareCooldownTicks = randomBetweenInclusive(ULTRA_RARE_INTERVAL_MIN_TICKS, ULTRA_RARE_INTERVAL_MAX_TICKS);
			ambienceFadeVolume = 0.0f;
		}

		wasUnderwater = true;

		// Calculate depth-based volume
		int surfaceY = getSurfaceY(world, player);
		int playerHeadY = (int) Math.floor(player.y + 1.62);
		int depth = surfaceY - playerHeadY;
		ambienceTargetVolume = calculateDepthVolume(depth);

		// Update ambience loop with fading
		updateAmbienceLoop(mc, player);

		// Random one-shot additions (bubbles and other sounds)
		if (additionsCooldownTicks-- <= 0) {
			if (RANDOM.nextFloat() < ADDITIONS_CHANCE) {
				playRandomFromGroup(mc, player, ADDITION_SOUNDS, ambienceFadeVolume);
			}
			additionsCooldownTicks = randomBetweenInclusive(ADDITIONS_INTERVAL_MIN_TICKS, ADDITIONS_INTERVAL_MAX_TICKS);
		}

		if (rareCooldownTicks-- <= 0) {
			if (RANDOM.nextFloat() < RARE_CHANCE) {
				playRandomFromGroup(mc, player, RARE_SOUNDS, ambienceFadeVolume);
			}
			rareCooldownTicks = randomBetweenInclusive(RARE_INTERVAL_MIN_TICKS, RARE_INTERVAL_MAX_TICKS);
		}

		if (ultraRareCooldownTicks-- <= 0) {
			if (RANDOM.nextFloat() < ULTRA_RARE_CHANCE) {
				playRandomFromGroup(mc, player, ULTRA_RARE_SOUNDS, ambienceFadeVolume);
			}
			ultraRareCooldownTicks = randomBetweenInclusive(ULTRA_RARE_INTERVAL_MIN_TICKS, ULTRA_RARE_INTERVAL_MAX_TICKS);
		}
	}

	private static void updateAmbienceLoop(Minecraft mc, Player player) {
		boolean isPlaying = isSoundPlaying();

		if (ambienceTargetVolume <= 0.01f) {
			// Not deep enough - fade out and stop
			if (isPlaying) {
				ambienceFadeVolume -= FADE_OUT_SPEED;
				if (ambienceFadeVolume <= 0.0f) {
					ambienceFadeVolume = 0.0f;
					stopSound();
				} else {
					setSoundVolume(calculateFinalVolume());
				}
			}
			loopAmbienceCooldownTicks = 0;
			return;
		}

		// Fade toward target
		if (ambienceFadeVolume < ambienceTargetVolume) {
			ambienceFadeVolume = Math.min(ambienceFadeVolume + FADE_IN_SPEED, ambienceTargetVolume);
		} else if (ambienceFadeVolume > ambienceTargetVolume) {
			ambienceFadeVolume = Math.max(ambienceFadeVolume - FADE_OUT_SPEED, ambienceTargetVolume);
		}

		if (!isPlaying) {
			if (loopAmbienceCooldownTicks-- <= 0) {
				String sound = pickVorbisSoundOrNull(LOOP_AMBIENCE_SOUNDS);
				if (sound != null) {
					// Only start playing if we're somewhat faded in already
					// Start at volume 0 so we fade in smoothly
					if (ambienceFadeVolume > 0.05f) {
						// Play at very low volume initially, then fade up
						mc.sndManager.playSoundWithIdAtPos(sound, SoundCategory.WORLD_SOUNDS,
							(float) player.x, (float) player.y, (float) player.z,
							0.001f, 1.0f, AMBIENCE_LOOP_ID);
						// Immediately set to actual faded volume
						setSoundVolume(calculateFinalVolume());
					}
				}
				loopAmbienceCooldownTicks = randomBetweenInclusive(LOOP_INTERVAL_MIN_TICKS, LOOP_INTERVAL_MAX_TICKS);
			}
		} else {
			// Update volume of currently playing loop
			setSoundVolume(calculateFinalVolume());
			// Reset cooldown since we're actively playing
			loopAmbienceCooldownTicks = randomBetweenInclusive(LOOP_INTERVAL_MIN_TICKS, LOOP_INTERVAL_MAX_TICKS);
		}
	}

	private static float calculateFinalVolume() {
		String sound = BetterThanAmbianceSounds.UNDERWATER_AMBIENCE;
		return getVolume(sound) * getUnderwaterVolumeMultiplier() * ambienceFadeVolume;
	}

	private static float calculateDepthVolume(int depth) {
		if (depth < MIN_DEPTH_FOR_AMBIENCE) {
			return 0.0f;
		}
		if (depth >= FULL_DEPTH_FOR_AMBIENCE) {
			return 1.0f;
		}
		// Linear interpolation from MIN_DEPTH to FULL_DEPTH
		return (float) (depth - MIN_DEPTH_FOR_AMBIENCE) / (float) (FULL_DEPTH_FOR_AMBIENCE - MIN_DEPTH_FOR_AMBIENCE);
	}

	private static int getSurfaceY(World world, Player player) {
		int x = (int) Math.floor(player.x);
		int z = (int) Math.floor(player.z);
		// Find the first non-water block above the player TODO maybe make this just check for air?
		int startY = (int) Math.floor(player.y + 1.62);
		for (int y = startY; y < world.getHeightBlocks(); y++) {
			if (!BlockTypeMappings.WATER_BLOCKS.contains(world.getBlockId(x, y, z))) {
				return y;
			}
		}
		return world.getHeightBlocks();
	}

	private static boolean isPlayerHeadUnderwater(World world, Player player) {
		int x = (int) Math.floor(player.x);
		int y = (int) Math.floor(player.y + 1.62);
		int z = (int) Math.floor(player.z);
		return BlockTypeMappings.WATER_BLOCKS.contains(world.getBlockId(x, y, z));
	}

	private static void playRandomFromGroup(Minecraft mc, Player player, String[] group, float depthMultiplier) {
		if (group.length == 0) {
			return;
		}

		String sound = pickVorbisSoundOrNull(group);
		if (sound == null) {
			return;
		}
		float volume = getVolume(sound) * getUnderwaterVolumeMultiplier() * depthMultiplier;
		if (volume <= 0.01f) {
			return;
		}

		mc.sndManager.playSoundAt(
			sound,
			SoundCategory.WORLD_SOUNDS,
			(float) player.x,
			(float) player.y,
			(float) player.z,
			volume,
			1.0f
		);
	}

	private static String pickVorbisSoundOrNull(String[] group) {
		for (int attempt = 0; attempt < group.length; attempt++) {
			String sound = group[RANDOM.nextInt(group.length)];
			if (isSoundEventVorbis(sound)) {
				return sound;
			}
		}
		return null;
	}

	private static boolean isSoundEventVorbis(String soundEventId) {
		if (soundEventId == null) {
			return false;
		}
		Boolean cached = cachedVorbisAssets.get(soundEventId);
		if (cached != null) {
			return cached;
		}

		String assetPath = soundEventIdToAssetPath(soundEventId);
		boolean isVorbis = isAssetVorbis(assetPath);
		cachedVorbisAssets.put(soundEventId, isVorbis);

		if (!isVorbis && !Boolean.TRUE.equals(cachedVorbisWarnings.get(soundEventId))) {
			cachedVorbisWarnings.put(soundEventId, true);
			BetterThanAmbianceMod.LOGGER.warn("Skipping non-Vorbis OGG asset for {}: {}", soundEventId, assetPath);
		}

		return isVorbis;
	}

	private static String soundEventIdToAssetPath(String soundEventId) {
		int index = soundEventId.indexOf(':');
		String key = index >= 0 ? soundEventId.substring(index + 1) : soundEventId;
		return "/assets/" + BetterThanAmbianceMod.MOD_ID + "/sounds/" + key + ".ogg";
	}

	private static boolean isAssetVorbis(String classpathAssetPath) {
		try (InputStream in = UnderWaterSoundsManager.class.getResourceAsStream(classpathAssetPath)) {
			if (in == null) {
				return false;
			}
			byte[] buffer = new byte[4096];
			int len = in.read(buffer);
			if (len <= 0) {
				return false;
			}
			for (int i = 0; i <= len - 6; i++) {
				if (buffer[i] == 'v'
					&& buffer[i + 1] == 'o'
					&& buffer[i + 2] == 'r'
					&& buffer[i + 3] == 'b'
					&& buffer[i + 4] == 'i'
					&& buffer[i + 5] == 's') {
					return true;
				}
			}
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	private static float getVolume(String sound) {
		if (BetterThanAmbianceSounds.UNDERWATER_AMBIENCE.equals(sound)) return 0.65f;

		if (BetterThanAmbianceSounds.UNDERWATER_BASS_WHALE_1.equals(sound)) return 0.45f;
		if (BetterThanAmbianceSounds.UNDERWATER_BASS_WHALE_2.equals(sound)) return 0.50f;

		if (BetterThanAmbianceSounds.UNDERWATER_CRACKLES_1.equals(sound)) return 0.70f;

		if (BetterThanAmbianceSounds.UNDERWATER_DRIPLETS_1.equals(sound)) return 0.50f;
		if (BetterThanAmbianceSounds.UNDERWATER_DRIPLETS_2.equals(sound)) return 0.50f;

		if (BetterThanAmbianceSounds.UNDERWATER_DARK_2.equals(sound)) return 0.70f;

		return 1.0f;
	}

	private static boolean isUnderwaterEnabled() {
		OptionBoolean opt = getCachedBooleanOption();
		return opt == null || opt.value;
	}

	private static float getUnderwaterVolumeMultiplier() {
		OptionFloat opt = getCachedFloatOption();
		return opt != null ? opt.value : 1.0f;
	}

	private static OptionFloat getCachedFloatOption() {
		return cachedFloatOptions.computeIfAbsent("betterthanambiance.underwaterVolume", key -> {
			for (net.minecraft.client.option.Option<?> option : GameSettings.getAllOptions()) {
				if (option instanceof OptionFloat && option.id.equals(key)) {
					return (OptionFloat) option;
				}
			}
			return null;
		});
	}

	private static OptionBoolean getCachedBooleanOption() {
		return cachedBooleanOptions.computeIfAbsent("betterthanambiance.underwaterEnabled", key -> {
			for (net.minecraft.client.option.Option<?> option : GameSettings.getAllOptions()) {
				if (option instanceof OptionBoolean && option.id.equals(key)) {
					return (OptionBoolean) option;
				}
			}
			return null;
		});
	}

	private static int randomBetweenInclusive(int min, int max) {
		if (max <= min) {
			return min;
		}
		return min + RANDOM.nextInt(max - min + 1);
	}

	private static void reset() {
		wasUnderwater = false;
		loopAmbienceCooldownTicks = 0;
		additionsCooldownTicks = 0;
		rareCooldownTicks = 0;
		ultraRareCooldownTicks = 0;
		ambienceFadeVolume = 0.0f;
		ambienceTargetVolume = 0.0f;

		stopSound();
	}
}
