package com.pasithea0.betterthanrain;

import com.pasithea0.betterthanrain.settings.IBetterThanRainOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;
import net.minecraft.core.world.weather.Weather;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Manages rain sound detection and playback based on blocks near the player.
 */
public class RainSoundManager {
    private static final Random RANDOM = new Random();
    private static final int SOUND_COOLDOWN_MIN = 10;
    private static final int SOUND_COOLDOWN_MAX = 30;
    private static int cooldownTimer = 0;

    private static final Set<Integer> METAL_BLOCKS = new HashSet<>();
    private static final Set<Integer> GLASS_BLOCKS = new HashSet<>();
    private static final Set<Integer> FABRIC_BLOCKS = new HashSet<>();
    private static final Set<Integer> FOLIAGE_BLOCKS = new HashSet<>();
    private static final Set<Integer> WATER_BLOCKS = new HashSet<>();
    private static final Set<Integer> LAVA_BLOCKS = new HashSet<>();
    private static final Set<Integer> NOTEBLOCK_BLOCKS = new HashSet<>();

    static {
        METAL_BLOCKS.add(431); // Block of Iron
        METAL_BLOCKS.add(432); // Block of Gold
        METAL_BLOCKS.add(437); // Block of Steel

        GLASS_BLOCKS.add(730); // Ice
        GLASS_BLOCKS.add(190); // Glass
        GLASS_BLOCKS.add(191); // Tinted Glass
        GLASS_BLOCKS.add(192); // Reinforced Glass

        FABRIC_BLOCKS.add(110); // Wool
        FABRIC_BLOCKS.add(850); // Lamp idle
        FABRIC_BLOCKS.add(851); // Lamp active
        FABRIC_BLOCKS.add(852); // Lamp inverted idle
        FABRIC_BLOCKS.add(853); // Lamp inverted active
		FABRIC_BLOCKS.add(910); // Paper Wall

        FOLIAGE_BLOCKS.add(290); // Oak Leaves
        FOLIAGE_BLOCKS.add(291); // Retro Leaves
        FOLIAGE_BLOCKS.add(292); // Pine Leaves
        FOLIAGE_BLOCKS.add(293); // Birch Leaves
        FOLIAGE_BLOCKS.add(294); // Cherry Leaves
        FOLIAGE_BLOCKS.add(295); // Eucalyptus Leaves
        FOLIAGE_BLOCKS.add(296); // Shrub Leaves
        FOLIAGE_BLOCKS.add(297); // Flowering Cherry Leaves
        FOLIAGE_BLOCKS.add(298); // Cacao Leaves
        FOLIAGE_BLOCKS.add(299); // Thorn Leaves
        FOLIAGE_BLOCKS.add(300); // Palm Leaves

        WATER_BLOCKS.add(270); // Flowing Water
        WATER_BLOCKS.add(271); // Still Water

        LAVA_BLOCKS.add(272); // Flowing Lava
        LAVA_BLOCKS.add(273); // Still Lava
        LAVA_BLOCKS.add(801); // Igneous Cobbled Netherrack
        LAVA_BLOCKS.add(420); // Nether Coal Ore
        LAVA_BLOCKS.add(436); // Block of Nether Coal
        LAVA_BLOCKS.add(233); // Molten Pumice

        NOTEBLOCK_BLOCKS.add(530); // Noteblock
    }

    public static void tick(Minecraft mc) {
        if (mc.currentWorld == null || mc.thePlayer == null) {
            return;
        }

        RainSoundManager manager = new RainSoundManager();
        manager.tickInternal(mc);
    }

    private void tickInternal(Minecraft mc) {
        if (cooldownTimer > 0) {
            cooldownTimer--;
            return;
        }

        if (!isRaining(mc.currentWorld)) {
            return;
        }

        Player player = mc.thePlayer;
        World world = mc.currentWorld;

        int playerX = (int) Math.floor(player.x);
        int playerY = (int) Math.floor(player.y);
        int playerZ = (int) Math.floor(player.z);

        boolean isUnderCover = isPlayerUnderCover(world, playerX, playerY, playerZ);
        String soundToPlay = findRainSound(world, playerX, playerY, playerZ, isUnderCover);

        if (soundToPlay != null && !soundToPlay.equals("ambient.weather.rain")) {
            IBetterThanRainOptions settings = (IBetterThanRainOptions) mc.gameSettings;

            float baseVolume = 0.3f * world.weatherManager.getWeatherIntensity() * world.weatherManager.getWeatherPower();
            float volume = baseVolume * settings.betterthanrain$getMasterRainVolume().value;

            float materialMultiplier = getMaterialVolumeMultiplier(soundToPlay, settings);
            volume *= materialMultiplier;

            if (isUnderCover) {
                volume *= settings.betterthanrain$getMuffledVolumeMultiplier().value;
            }

            float pitch = 0.8f + RANDOM.nextFloat() * 0.4f;

            SoundCategory soundCategory = settings.betterthanrain$getUseWeatherSounds().value ?
                SoundCategory.WORLD_SOUNDS : SoundCategory.WEATHER_SOUNDS;

            world.playSoundEffect(null, soundCategory,
                player.x, player.y, player.z, soundToPlay, volume, pitch);

            cooldownTimer = RANDOM.nextInt(SOUND_COOLDOWN_MAX - SOUND_COOLDOWN_MIN + 1) + SOUND_COOLDOWN_MIN;
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

    private String findRainSound(World world, int centerX, int centerY, int centerZ, boolean isUnderCover) {
        int searchRadius = 3;

        for (int x = centerX - searchRadius; x <= centerX + searchRadius; x++) {
            for (int z = centerZ - searchRadius; z <= centerZ + searchRadius; z++) {
                int surfaceY = world.findTopSolidBlock(x, z);

                if (Math.abs(surfaceY - centerY) > 5) {
                    continue;
                }

                if (world.canBlockBeRainedOn(x, surfaceY, z)) {
                    int blockId = world.getBlockId(x, surfaceY - 1, z);
                    String sound = getRainSoundForBlock(blockId, isUnderCover);

                    if (sound != null && !sound.equals("ambient.weather.rain")) {
                        return sound;
                    }
                }
            }
        }

        return null;
    }

    private String getRainSoundForBlock(int blockId, boolean isUnderCover) {
        if (blockId <= 0) {
            return null;
        }

        if (METAL_BLOCKS.contains(blockId)) {
            return isUnderCover ? BetterThanRainSounds.RAIN_SOUNDS_METAL_MUFFLED
                                : BetterThanRainSounds.RAIN_SOUNDS_METAL;
        }

        if (GLASS_BLOCKS.contains(blockId)) {
            return isUnderCover ? BetterThanRainSounds.RAIN_SOUNDS_GLASS_MUFFLED
                                : BetterThanRainSounds.RAIN_SOUNDS_GLASS;
        }

        if (FABRIC_BLOCKS.contains(blockId)) {
            return isUnderCover ? BetterThanRainSounds.RAIN_SOUNDS_FABRIC_MUFFLED
                                : BetterThanRainSounds.RAIN_SOUNDS_FABRIC;
        }

        if (FOLIAGE_BLOCKS.contains(blockId)) {
            return BetterThanRainSounds.RAIN_SOUNDS_FOLIAGE;
        }

        if (WATER_BLOCKS.contains(blockId)) {
            return BetterThanRainSounds.RAIN_SOUNDS_WATER;
        }

        if (LAVA_BLOCKS.contains(blockId)) {
            return BetterThanRainSounds.RAIN_SOUNDS_LAVA;
        }

        if (NOTEBLOCK_BLOCKS.contains(blockId)) {
            return BetterThanRainSounds.RAIN_SOUNDS_NOTEBLOCK;
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
        }

        return 1.0f;
    }
}
