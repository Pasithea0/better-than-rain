package com.pasithea0.betterthanambiance;

import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.biome.Biomes;
import net.minecraft.core.world.season.Season;
import net.minecraft.core.world.season.Seasons;
import net.minecraft.core.world.weather.IPrecipitation;
import net.minecraft.core.world.weather.Weather;
import net.minecraft.core.world.weather.Weathers;

public class OverworldSoundManager extends SoundManager {
    public static final OverworldSoundManager INSTANCE = new OverworldSoundManager();

    private static final int OCEAN_RAY_LENGTH = 32;
    private static final int OCEAN_MIN_WATER_COUNT = 10;
    private static final int OCEAN_SOUND_DISTANCE = 24;

    private static final int FOREST_SEARCH_RADIUS = 8;
    private static final int FOREST_MIN_TREE_COUNT = 3;

    private final AmbientLoop oceanLoop = new AmbientLoop(
        BetterThanAmbianceSounds.OCEAN_LOOP_ID, "bta_ocean", 0.6f, 0.003f, 0.008f);
    private final AmbientLoop birdsLoop = new AmbientLoop(
        BetterThanAmbianceSounds.BIRDS_LOOP_ID, "bta_birds", 0.2f, 0.001f, 0.003f);

    private float oceanOffsetX;
    private float oceanOffsetZ;
    private int oceanDirectionX;
    private int oceanDirectionZ;

    @Override
    protected void onEnterInvalidState() {
        oceanLoop.stop();
        birdsLoop.stop();
    }

    @Override
    protected void tickInternal() {
        if (!isOverworldEnabled()) {
            onEnterInvalidState();
            return;
        }

        initSoundSystem();

        float multiplier = getOverworldVolumeMultiplier();

        boolean hasOcean = updateOceanDirection();
        if (hasOcean) {
            oceanOffsetX = oceanDirectionX * OCEAN_SOUND_DISTANCE;
            oceanOffsetZ = oceanDirectionZ * OCEAN_SOUND_DISTANCE;
        }
        oceanLoop.tick(hasOcean ? 1.0f : 0.0f, multiplier, mc, player, oceanOffsetX, oceanOffsetZ);
        birdsLoop.tick(calculateBirdsTarget(), multiplier, mc, player);
    }

    private float calculateBirdsTarget() {
        Weather weather = world.getCurrentWeather();
        if (weather instanceof IPrecipitation || weather == Weathers.OVERWORLD_FOG) {
            return 0.0f;
        }

        Season season = world.getSeasonManager().getCurrentSeason();
        if (season != Seasons.OVERWORLD_SUMMER) {
            return 0.0f;
        }

        int x = (int) Math.floor(player.x);
        int y = (int) Math.floor(player.y);
        int z = (int) Math.floor(player.z);
        Biome biome = world.getBiomeProvider().getBiome(x, y, z);

        if (biome == Biomes.OVERWORLD_RAINFOREST) {
            return 1.0f;
        }

        if (biome == Biomes.OVERWORLD_FOREST || biome == Biomes.OVERWORLD_BIRCH_FOREST) {
            return hasNearbyTrees(x, y, z) ? 0.8f : 0.0f;
        }

        return 0.0f;
    }

    private boolean hasNearbyTrees(int px, int py, int pz) {
        int treeCount = 0;
        for (int dx = -FOREST_SEARCH_RADIUS; dx <= FOREST_SEARCH_RADIUS; dx++) {
            for (int dz = -FOREST_SEARCH_RADIUS; dz <= FOREST_SEARCH_RADIUS; dz++) {
                int x = px + dx;
                int z = pz + dz;
                int y = world.findTopSolidBlock(x, z);
                int id = world.getBlockId(x, y + 1, z);
                if (isLog(id)) {
                    treeCount++;
                    if (treeCount >= FOREST_MIN_TREE_COUNT) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isLog(int id) {
        return id == Blocks.LOG_OAK.id()
            || id == Blocks.LOG_PINE.id()
            || id == Blocks.LOG_BIRCH.id()
            || id == Blocks.LOG_CHERRY.id()
            || id == Blocks.LOG_EUCALYPTUS.id()
            || id == Blocks.LOG_OAK_MOSSY.id()
            || id == Blocks.LOG_THORN.id()
            || id == Blocks.LOG_PALM.id();
    }

    private boolean updateOceanDirection() {
        int px = (int) Math.floor(player.x);
        int pz = (int) Math.floor(player.z);
        int surfaceY = world.findTopSolidBlock(px, pz);

        int bestCount = 0;
        int bestDirX = 0;
        int bestDirZ = 0;

        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] dir : dirs) {
            int dx = dir[0];
            int dz = dir[1];
            int waterCount = 0;
            boolean hasSand = false;

            for (int step = 1; step <= OCEAN_RAY_LENGTH; step++) {
                int x = px + dx * step;
                int z = pz + dz * step;
                int y = world.findTopSolidBlock(x, z);
                int aboveId = world.getBlockId(x, y + 1, z);

                if (aboveId == Blocks.FLUID_WATER_FLOWING.id() || aboveId == Blocks.FLUID_WATER_STILL.id()) {
                    waterCount++;
                    if (!hasSand) {
                        hasSand = isBesideSand(x, y + 1, z);
                    }
                } else {
                    int surfId = world.getBlockId(x, y, z);
                    if (surfId == Blocks.FLUID_WATER_FLOWING.id() || surfId == Blocks.FLUID_WATER_STILL.id()) {
                        waterCount++;
                        if (!hasSand) {
                            hasSand = isBesideSand(x, y, z);
                        }
                    }
                }
            }

            if (waterCount >= OCEAN_MIN_WATER_COUNT && hasSand && waterCount > bestCount) {
                bestCount = waterCount;
                bestDirX = dx;
                bestDirZ = dz;
            }
        }

        oceanDirectionX = bestDirX;
        oceanDirectionZ = bestDirZ;
        return bestCount > 0;
    }

    private boolean isBesideSand(int x, int y, int z) {
        int sandId = Blocks.SAND.id();
        return world.getBlockId(x + 1, y, z) == sandId
            || world.getBlockId(x - 1, y, z) == sandId
            || world.getBlockId(x, y, z + 1) == sandId
            || world.getBlockId(x, y, z - 1) == sandId;
    }

    private boolean isOverworldEnabled() {
        return getBooleanOption("betterthanambiance.overworldEnabled") == null
            || getBooleanOption("betterthanambiance.overworldEnabled").value;
    }

    private float getOverworldVolumeMultiplier() {
        return getFloatValue("betterthanambiance.overworldVolume", 1.0f);
    }
}
