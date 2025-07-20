package com.pasithea0.betterthanrain;

import com.pasithea0.betterthanrain.settings.IBetterThanRainOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;
import net.minecraft.core.world.weather.Weather;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Manages rain sound detection and playback based on blocks near the player.
 */
public class RainSoundManager {
    private static final Random RANDOM = new Random();
    private static final int SOUND_COOLDOWN_MIN = 20;
    private static final int SOUND_COOLDOWN_MAX = 50;
    private static final int MAX_CONCURRENT_SOUNDS = 10;
    private static final Map<String, Integer> soundCooldowns = new HashMap<>();

    private static final float GLOBAL_GAIN = 2.0f;

    public static void tick(Minecraft mc) {
        if (mc.currentWorld == null || mc.thePlayer == null) {
            return;
        }

        RainSoundManager manager = new RainSoundManager();
        manager.tickInternal(mc);
    }

    private void tickInternal(Minecraft mc) {
        // Update cooldowns for all sound types
        soundCooldowns.entrySet().removeIf(entry -> {
            entry.setValue(entry.getValue() - 1);
            return entry.getValue() <= 0;
        });

        if (!isRaining(mc.currentWorld)) {
            return;
        }

        Player player = mc.thePlayer;
        World world = mc.currentWorld;

        int playerX = (int) Math.floor(player.x);
        int playerY = (int) Math.floor(player.y);
        int playerZ = (int) Math.floor(player.z);

        boolean isUnderCover = isPlayerUnderCover(world, playerX, playerY, playerZ);
        Set<String> soundsToPlay = findRainSounds(world, playerX, playerY, playerZ, isUnderCover);

        // Limit concurrent sounds and filter out sounds on cooldown
        int soundsPlayed = 0;
        for (String soundToPlay : soundsToPlay) {
            if (soundsPlayed >= MAX_CONCURRENT_SOUNDS) {
                break;
            }

            if (soundToPlay != null && !soundToPlay.equals("ambient.weather.rain") && !soundCooldowns.containsKey(soundToPlay)) {
                IBetterThanRainOptions settings = (IBetterThanRainOptions) mc.gameSettings;

                float baseVolume = 0.3f * world.weatherManager.getWeatherIntensity() * world.weatherManager.getWeatherPower();
                float volume = baseVolume * settings.betterthanrain$getMasterRainVolume().value;

                float materialMultiplier = getMaterialVolumeMultiplier(soundToPlay, settings);
                volume *= materialMultiplier;

                if (isUnderCover) {
                    volume *= settings.betterthanrain$getMuffledVolume().value;
                }

                volume *= GLOBAL_GAIN;

                float pitch = 0.8f + RANDOM.nextFloat() * 0.4f;

                SoundCategory soundCategory = settings.betterthanrain$getUseWeatherSounds().value ?
                    SoundCategory.WORLD_SOUNDS : SoundCategory.WEATHER_SOUNDS;

                world.playSoundEffect(null, soundCategory,
                    player.x, player.y, player.z, soundToPlay, volume, pitch);

                // Add cooldown for this specific sound type
                soundCooldowns.put(soundToPlay, RANDOM.nextInt(SOUND_COOLDOWN_MAX - SOUND_COOLDOWN_MIN + 1) + SOUND_COOLDOWN_MIN);
                soundsPlayed++;
            }
        }
    }

    private boolean isRaining(World world) {
        Weather currentWeather = world.getCurrentWeather();
        if (currentWeather == null) {
            return false;
        }

        return currentWeather.isPrecipitation && world.weatherManager.getWeatherIntensity() > 0.1f;
    }

    private boolean isPlayerUnderCover(World world, int playerX, int playerY, int playerZ) {
        int rainLevel = world.findTopSolidBlock(playerX, playerZ);

        if (playerY >= rainLevel - 1) {
            return false;
        }

        for (int y = playerY + 1; y <= rainLevel; y++) {
            int blockId = world.getBlockId(playerX, y, playerZ);
            if (blockId > 0 && blockId < Blocks.solid.length) {
                if (Blocks.solid[blockId]) {
                    return true;
                }

                Block<?> block = Blocks.blocksList[blockId];
                if (block != null && !block.isSolidRender()) {
                    continue;
                }

                return true;
            }
        }

        return false;
    }

    private Set<String> findRainSounds(World world, int centerX, int centerY, int centerZ, boolean isUnderCover) {
        int searchRadius = 4;
        Set<String> foundSounds = new HashSet<>();

        for (int x = centerX - searchRadius; x <= centerX + searchRadius; x++) {
            for (int z = centerZ - searchRadius; z <= centerZ + searchRadius; z++) {
                int surfaceY = world.findTopSolidBlock(x, z);

                if (Math.abs(surfaceY - centerY) > 5) {
                    continue;
                }

                if (world.canBlockBeRainedOn(x, surfaceY, z)) {
                    int blockId = world.getBlockId(x, surfaceY - 1, z);

                    // Special handling for glass
                    boolean effectivelyUnderCover = isUnderCover;
                    if (BlockTypeMappings.GLASS_BLOCKS.contains(blockId)) {
                        effectivelyUnderCover = (centerY < surfaceY - 1);
                    }

                    String sound = getRainSoundForBlock(blockId, effectivelyUnderCover);

                    if (sound != null && !sound.equals("ambient.weather.rain")) {
                        foundSounds.add(sound);

                        if (foundSounds.size() >= MAX_CONCURRENT_SOUNDS) {
                            break;
                        }
                    }
                }
            }

            if (foundSounds.size() >= MAX_CONCURRENT_SOUNDS) {
                break;
            }
        }

        return foundSounds;
    }

    private String getRainSoundForBlock(int blockId, boolean isUnderCover) {
        if (blockId <= 0) {
            return null;
        }

        if (BlockTypeMappings.METAL_BLOCKS.contains(blockId)) {
            return isUnderCover ? BetterThanRainSounds.RAIN_SOUNDS_METAL_MUFFLED
                                : BetterThanRainSounds.RAIN_SOUNDS_METAL;
        }

        if (BlockTypeMappings.GLASS_BLOCKS.contains(blockId)) {
            return isUnderCover ? BetterThanRainSounds.RAIN_SOUNDS_GLASS_MUFFLED
                                : BetterThanRainSounds.RAIN_SOUNDS_GLASS;
        }

        if (BlockTypeMappings.FABRIC_BLOCKS.contains(blockId)) {
            return isUnderCover ? BetterThanRainSounds.RAIN_SOUNDS_FABRIC_MUFFLED
                                : BetterThanRainSounds.RAIN_SOUNDS_FABRIC;
        }

        if (BlockTypeMappings.FOLIAGE_BLOCKS.contains(blockId)) {
            return BetterThanRainSounds.RAIN_SOUNDS_FOLIAGE;
        }

        if (BlockTypeMappings.WATER_BLOCKS.contains(blockId)) {
            return BetterThanRainSounds.RAIN_SOUNDS_WATER;
        }

        if (BlockTypeMappings.LAVA_BLOCKS.contains(blockId)) {
            return BetterThanRainSounds.RAIN_SOUNDS_LAVA;
        }

        if (BlockTypeMappings.NOTEBLOCK_BLOCKS.contains(blockId)) {
            return BetterThanRainSounds.RAIN_SOUNDS_NOTEBLOCK;
        }

        if (BlockTypeMappings.STONE_BLOCKS.contains(blockId)) {
            return BetterThanRainSounds.RAIN_SOUNDS_STONE;
        }

        if (BlockTypeMappings.WOOD_BLOCKS.contains(blockId)) {
            return BetterThanRainSounds.RAIN_SOUNDS_WOOD;
        }

        if (BlockTypeMappings.PLASTIC_BLOCKS.contains(blockId)) {
            return BetterThanRainSounds.RAIN_SOUNDS_PLASTIC;
        }

        return null;
    }

    private float getMaterialVolumeMultiplier(String soundToPlay, IBetterThanRainOptions settings) {
        if (soundToPlay.contains("rain_sounds_metal")) {
            return settings.betterthanrain$getMetalRainVolume().value;
        } else if (soundToPlay.contains("rain_sounds_glass")) {
            return settings.betterthanrain$getGlassRainVolume().value;
        } else if (soundToPlay.contains("rain_sounds_fabric")) {
            return settings.betterthanrain$getFabricRainVolume().value;
        } else if (soundToPlay.contains("rain_sounds_lava")) {
            return settings.betterthanrain$getLavaRainVolume().value;
        } else if (soundToPlay.contains("rain_sounds_foliage")) {
            return settings.betterthanrain$getFoliageRainVolume().value;
        } else if (soundToPlay.contains("rain_sounds_water")) {
            return settings.betterthanrain$getWaterRainVolume().value;
        } else if (soundToPlay.contains("rain_sounds_noteblock")) {
            return settings.betterthanrain$getNoteblockRainVolume().value;
        } else if (soundToPlay.contains("rain_sounds_stone")) {
            return settings.betterthanrain$getStoneRainVolume().value;
        } else if (soundToPlay.contains("rain_sounds_wood")) {
            return settings.betterthanrain$getWoodRainVolume().value;
        } else if (soundToPlay.contains("rain_sounds_plastic")) {
            return settings.betterthanrain$getPlasticRainVolume().value;
        }

        return 1.0f;
    }
}
