package com.pasithea0.betterthanrain;

import com.pasithea0.betterthanrain.BetterThanRainSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;
import net.minecraft.core.world.weather.Weather;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Manages rain sound detection and playback based on blocks near the player.
 * Uses a simple position-based detection system instead of trying to hook into existing rain sounds.
 */
public class RainSoundManager {
    private static final Random RANDOM = new Random();
    private static final int SOUND_COOLDOWN = 8; // Ticks between sound plays
    private static int cooldownTimer = 0;

    // Block sets for material detection
    private static final Set<Integer> METAL_BLOCKS = new HashSet<>();
    private static final Set<Integer> GLASS_BLOCKS = new HashSet<>();
    private static final Set<Integer> FABRIC_BLOCKS = new HashSet<>();
    private static final Set<Integer> FOLIAGE_BLOCKS = new HashSet<>();
    private static final Set<Integer> WATER_BLOCKS = new HashSet<>();
    private static final Set<Integer> LAVA_BLOCKS = new HashSet<>();
    private static final int NOTEBLOCK_ID = 530;

    static {
        // Metal blocks
        METAL_BLOCKS.add(431); // Block of Iron
        METAL_BLOCKS.add(432); // Block of Gold
        METAL_BLOCKS.add(437); // Block of Steel

        // Glass blocks
        GLASS_BLOCKS.add(730); // Ice
        GLASS_BLOCKS.add(190); // Glass
        GLASS_BLOCKS.add(191); // Tinted Glass
        GLASS_BLOCKS.add(192); // Reinforced Glass

        // Fabric blocks (wool variants and lamps)
        FABRIC_BLOCKS.add(110); // Wool (all colors use same base ID)
        FABRIC_BLOCKS.add(850); // Lamp idle
        FABRIC_BLOCKS.add(851); // Lamp active
        FABRIC_BLOCKS.add(852); // Lamp inverted idle
        FABRIC_BLOCKS.add(853); // Lamp inverted active

        // Foliage blocks (leaves)
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

        // Water blocks
        WATER_BLOCKS.add(270); // Flowing Water
        WATER_BLOCKS.add(271); // Still Water

        // Lava blocks
        LAVA_BLOCKS.add(272); // Flowing Lava
        LAVA_BLOCKS.add(273); // Still Lava
        LAVA_BLOCKS.add(801); // Igneous Cobbled Netherrack
        LAVA_BLOCKS.add(420); // Nether Coal Ore
        LAVA_BLOCKS.add(436); // Block of Nether Coal
        LAVA_BLOCKS.add(233); // Molten Pumice
    }

    /**
     * Called every tick from the mixin
     */
    public static void tick(Minecraft mc) {
        // Only run on client and when player exists
        if (mc.currentWorld == null || mc.thePlayer == null) {
            return;
        }

        RainSoundManager manager = new RainSoundManager();
        manager.tickInternal(mc);
    }

    private void tickInternal(Minecraft mc) {
        // Decrement cooldown
        if (cooldownTimer > 0) {
            cooldownTimer--;
            return;
        }

        // Check if it's raining
        if (!isRaining(mc.currentWorld)) {
            return;
        }

        Player player = mc.thePlayer;
        World world = mc.currentWorld;

        // Get player position
        int playerX = (int) Math.floor(player.x);
        int playerY = (int) Math.floor(player.y);
        int playerZ = (int) Math.floor(player.z);

        // Check if player is under cover (sheltered from rain)
        boolean isUnderCover = isPlayerUnderCover(world, playerX, playerY, playerZ);

        // Check for rain-eligible blocks around the player
        String soundToPlay = findRainSound(world, playerX, playerY, playerZ, isUnderCover);

        if (soundToPlay != null && !soundToPlay.equals("ambient.weather.rain")) {
            // Play the rain sound
            float volume = 0.3f * world.weatherManager.getWeatherIntensity() * world.weatherManager.getWeatherPower();
            float pitch = 0.8f + RANDOM.nextFloat() * 0.4f; // Random pitch variation

            world.playSoundEffect(null, SoundCategory.WEATHER_SOUNDS,
                player.x, player.y, player.z, soundToPlay, volume, pitch);

            // Set cooldown to prevent spam
            cooldownTimer = SOUND_COOLDOWN;
        }
    }

    /**
     * Check if it's currently raining
     */
    private boolean isRaining(World world) {
        Weather currentWeather = world.getCurrentWeather();
        if (currentWeather == null) {
            return false;
        }

        // Check if current weather has precipitation and intensity > 0
        return currentWeather.isPrecipitation &&
               world.weatherManager.getWeatherIntensity() > 0.1f;
    }

    /**
     * Check if the player is under cover (sheltered from rain)
     */
    private boolean isPlayerUnderCover(World world, int playerX, int playerY, int playerZ) {
        // Check blocks above the player up to rain level
        int rainLevel = world.findTopSolidBlock(playerX, playerZ);

        // If the player is close to or above the rain level, they're not under cover
        if (playerY >= rainLevel - 1) {
            return false;
        }

        // Check for solid blocks above the player that would block rain
        for (int y = playerY + 1; y <= rainLevel; y++) {
            int blockId = world.getBlockId(playerX, y, playerZ);
            if (blockId > 0 && blockId < Blocks.solid.length) {
                // Check if this block is solid using BTA's solid array
                if (Blocks.solid[blockId]) {
                    return true; // Found solid cover above
                }

                // Also check the block's render method as a fallback
                Block<?> block = Blocks.blocksList[blockId];
                if (block != null && !block.isSolidRender()) {
                    continue; // Not solid, keep checking
                }

                return true; // Found solid cover
            }
        }

        return false;
    }

    /**
     * Find the appropriate rain sound for blocks around the player
     */
    private String findRainSound(World world, int centerX, int centerY, int centerZ, boolean isUnderCover) {
        // Sample blocks in a 7x7 area around the player
        int searchRadius = 3;

        for (int x = centerX - searchRadius; x <= centerX + searchRadius; x++) {
            for (int z = centerZ - searchRadius; z <= centerZ + searchRadius; z++) {
                // Check if this position can be rained on
                int surfaceY = world.findTopSolidBlock(x, z);

                // Only check blocks close to player's height (within 5 blocks up/down)
                if (Math.abs(surfaceY - centerY) > 5) {
                    continue;
                }

                // Check if the surface block can actually be rained on
                if (world.canBlockBeRainedOn(x, surfaceY, z)) {
                    int blockId = world.getBlockId(x, surfaceY - 1, z); // Block below the rain hit point
                    String sound = getRainSoundForBlock(blockId, isUnderCover);

                    if (!sound.equals("ambient.weather.rain")) {
                        return sound; // Found a special material
                    }
                }
            }
        }

        return null; // No special materials found
    }

    /**
     * Get the appropriate rain sound for a specific block, with muffled variants when under cover
     */
    private String getRainSoundForBlock(int blockId, boolean isUnderCover) {
        if (blockId <= 0) {
            return null;
        }

        // Check specific block types using our curated lists
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

        if (blockId == NOTEBLOCK_ID) {
            return BetterThanRainSounds.RAIN_SOUNDS_NOTEBLOCK;
        }

        // Default to generic rain sound for unrecognized blocks
        return null;
    }
}
