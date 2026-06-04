package com.pasithea0.betterthanambiance;

import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.client.option.OptionFloat;
import net.minecraft.core.sound.SoundCategory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class UnderWaterSoundsManager extends SoundManager {
    public static final UnderWaterSoundsManager INSTANCE = new UnderWaterSoundsManager();

    private static final Map<String, Boolean> cachedVorbisAssets = new HashMap<>();
    private static final Map<String, Boolean> cachedVorbisWarnings = new HashMap<>();

    private static Object soundSystem = null;
    private static Method playingMethod = null;
    private static Method stopMethod = null;
    private static Method setVolumeMethod = null;
    private static boolean reflectionInitialized = false;

    private static final float ADDITIONS_CHANCE = 0.70f;
    private static final float RARE_CHANCE = 0.09f;
    private static final float ULTRA_RARE_CHANCE = 0.01f;

    private static final int ADDITIONS_INTERVAL_MIN_TICKS = 80;
    private static final int ADDITIONS_INTERVAL_MAX_TICKS = 140;

    private static final int RARE_INTERVAL_MIN_TICKS = 160;
    private static final int RARE_INTERVAL_MAX_TICKS = 260;

    private static final int ULTRA_RARE_INTERVAL_MIN_TICKS = 320;
    private static final int ULTRA_RARE_INTERVAL_MAX_TICKS = 520;

    private static final int MIN_DEPTH_FOR_AMBIENCE = 5;
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

    private static final String AMBIENCE_LOOP_ID = "betterthanambiance_underwater_ambience";

    private static final float FADE_IN_SPEED = 0.005f;
    private static final float FADE_OUT_SPEED = 0.02f;

    private boolean wasUnderwater = false;
    private int additionsCooldownTicks = 0;
    private int rareCooldownTicks = 0;
    private int ultraRareCooldownTicks = 0;
    private float ambienceFadeVolume = 0.0f;
    private float ambienceTargetVolume = 0.0f;

    private static void initSoundSystem() {
        if (reflectionInitialized) return;
        reflectionInitialized = true;
        try {
            Method getter = SoundEngine.class.getMethod("getSoundSystem");
            soundSystem = getter.invoke(null);
            if (soundSystem == null) {
                BetterThanAmbianceMod.LOGGER.warn("SoundEngine.getSoundSystem() returned null");
                return;
            }
            Class<?> ssClass = soundSystem.getClass();
            playingMethod = ssClass.getMethod("playing", String.class);
            stopMethod = ssClass.getMethod("stop", String.class);
            setVolumeMethod = ssClass.getMethod("setVolume", String.class, float.class);
            BetterThanAmbianceMod.LOGGER.info("Hooked into SoundSystem via getSoundSystem()");
        } catch (Exception e) {
            BetterThanAmbianceMod.LOGGER.warn("Failed to get SoundSystem: {}", e.getMessage());
        }
    }

    private static boolean isPlaying() {
        if (soundSystem == null || playingMethod == null) return false;
        try {
            return (Boolean) playingMethod.invoke(soundSystem, AMBIENCE_LOOP_ID);
        } catch (Exception e) {
            return false;
        }
    }

    private static void stopAmbience() {
        if (soundSystem == null || stopMethod == null) return;
        try {
            stopMethod.invoke(soundSystem, AMBIENCE_LOOP_ID);
        } catch (Exception e) {
        }
    }

    private static void setAmbienceVolume(float volume) {
        if (soundSystem == null || setVolumeMethod == null) return;
        try {
            setVolumeMethod.invoke(soundSystem, AMBIENCE_LOOP_ID, volume);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onEnterInvalidState() {
        reset();
    }

    @Override
    protected void tickInternal() {
        if (!isUnderwaterEnabled()) {
            reset();
            return;
        }

        initSoundSystem();

        boolean underwater = isPlayerHeadUnderwater();
        if (!underwater) {
            if (wasUnderwater) {
                reset();
            }
            wasUnderwater = false;
            return;
        }

        if (!wasUnderwater) {
            additionsCooldownTicks = randomBetweenInclusive(ADDITIONS_INTERVAL_MIN_TICKS, ADDITIONS_INTERVAL_MAX_TICKS);
            rareCooldownTicks = randomBetweenInclusive(RARE_INTERVAL_MIN_TICKS, RARE_INTERVAL_MAX_TICKS);
            ultraRareCooldownTicks = randomBetweenInclusive(ULTRA_RARE_INTERVAL_MIN_TICKS, ULTRA_RARE_INTERVAL_MAX_TICKS);
            ambienceFadeVolume = 0.0f;
        }

        wasUnderwater = true;

        int surfaceY = getSurfaceY();
        int playerHeadY = (int) Math.floor(player.y + 1.62);
        int depth = surfaceY - playerHeadY;
        ambienceTargetVolume = calculateDepthVolume(depth);

        updateAmbienceLoop();

        if (additionsCooldownTicks-- <= 0) {
            if (RANDOM.nextFloat() < ADDITIONS_CHANCE) {
                playRandomFromGroup(ADDITION_SOUNDS, ambienceFadeVolume);
            }
            additionsCooldownTicks = randomBetweenInclusive(ADDITIONS_INTERVAL_MIN_TICKS, ADDITIONS_INTERVAL_MAX_TICKS);
        }

        if (rareCooldownTicks-- <= 0) {
            if (RANDOM.nextFloat() < RARE_CHANCE) {
                playRandomFromGroup(RARE_SOUNDS, ambienceFadeVolume);
            }
            rareCooldownTicks = randomBetweenInclusive(RARE_INTERVAL_MIN_TICKS, RARE_INTERVAL_MAX_TICKS);
        }

        if (ultraRareCooldownTicks-- <= 0) {
            if (RANDOM.nextFloat() < ULTRA_RARE_CHANCE) {
                playRandomFromGroup(ULTRA_RARE_SOUNDS, ambienceFadeVolume);
            }
            ultraRareCooldownTicks = randomBetweenInclusive(ULTRA_RARE_INTERVAL_MIN_TICKS, ULTRA_RARE_INTERVAL_MAX_TICKS);
        }
    }

    private void updateAmbienceLoop() {
        if (soundSystem == null) return;

        boolean playing = isPlaying();

        if (ambienceTargetVolume <= 0.01f) {
            if (playing) {
                ambienceFadeVolume -= FADE_OUT_SPEED;
                if (ambienceFadeVolume <= 0.0f) {
                    ambienceFadeVolume = 0.0f;
                    stopAmbience();
                } else {
                    setAmbienceVolume(calculateFinalVolume());
                }
            }
            return;
        }

        if (ambienceFadeVolume < ambienceTargetVolume) {
            ambienceFadeVolume = Math.min(ambienceFadeVolume + FADE_IN_SPEED, ambienceTargetVolume);
        } else if (ambienceFadeVolume > ambienceTargetVolume) {
            ambienceFadeVolume = Math.max(ambienceFadeVolume - FADE_OUT_SPEED, ambienceTargetVolume);
        }

        if (!playing) {
            String sound = pickVorbisSoundOrNull(LOOP_AMBIENCE_SOUNDS);
            if (sound != null && ambienceFadeVolume > 0.05f) {
                SoundCategory category = resolveCategory(SoundCategory.WORLD_SOUNDS);
                mc.sndManager.playSoundWithIdAtPos(sound, category,
                    (float) player.x, (float) player.y, (float) player.z,
                    0.001f, 1.0f, AMBIENCE_LOOP_ID);
                setAmbienceVolume(calculateFinalVolume());
            }
        } else {
            setAmbienceVolume(calculateFinalVolume());
        }
    }

    private float calculateFinalVolume() {
        return getVolume(BetterThanAmbianceSounds.UNDERWATER_AMBIENCE) * getUnderwaterVolumeMultiplier() * ambienceFadeVolume;
    }

    private static float calculateDepthVolume(int depth) {
        if (depth < MIN_DEPTH_FOR_AMBIENCE) {
            return 0.0f;
        }
        if (depth >= FULL_DEPTH_FOR_AMBIENCE) {
            return 1.0f;
        }
        return (float) (depth - MIN_DEPTH_FOR_AMBIENCE) / (float) (FULL_DEPTH_FOR_AMBIENCE - MIN_DEPTH_FOR_AMBIENCE);
    }

    private int getSurfaceY() {
        int x = (int) Math.floor(player.x);
        int z = (int) Math.floor(player.z);
        for (int y = world.getHeightBlocks() - 1; y >= 0; y--) {
            if (BlockTypeMappings.WATER_BLOCKS.contains(world.getBlockId(x, y, z))) {
                return y + 1;
            }
        }
        return (int) Math.floor(player.y + 1.62);
    }

    private boolean isPlayerHeadUnderwater() {
        int x = (int) Math.floor(player.x);
        int y = (int) Math.floor(player.y + 1.62);
        int z = (int) Math.floor(player.z);
        return BlockTypeMappings.WATER_BLOCKS.contains(world.getBlockId(x, y, z));
    }

    private void playRandomFromGroup(String[] group, float depthMultiplier) {
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

        SoundCategory category = resolveCategory(SoundCategory.WORLD_SOUNDS);
        mc.sndManager.playSoundAt(
            sound,
            category,
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

    private boolean isUnderwaterEnabled() {
        OptionBoolean opt = getBooleanOption("betterthanambiance.underwaterEnabled");
        return opt == null || opt.value;
    }

    private float getUnderwaterVolumeMultiplier() {
        OptionFloat opt = getFloatOption("betterthanambiance.underwaterVolume");
        return opt != null ? opt.value : 1.0f;
    }

    private void reset() {
        wasUnderwater = false;
        additionsCooldownTicks = 0;
        rareCooldownTicks = 0;
        ultraRareCooldownTicks = 0;
        ambienceFadeVolume = 0.0f;
        ambienceTargetVolume = 0.0f;

        stopAmbience();
    }
}
