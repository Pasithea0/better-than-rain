package com.pasithea0.betterthanambiance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;
import net.minecraft.core.world.weather.Weather;
import net.minecraft.core.world.weather.Weathers;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.client.option.OptionFloat;
import net.minecraft.client.option.GameSettings;

import java.util.*;

/**
 * Manages rain sound detection and playback based on blocks near the player.
 */
public class RainSoundManager {
    private static final Random RANDOM = new Random();

    // Performance constants
    private static final int SEARCH_RADIUS = 6;
    private static final float MIN_WEATHER_INTENSITY = 0.1f;
    private static final float GLOBAL_GAIN = 2.0f;

    // Sound management constants
    private static final int SOUND_COOLDOWN_MIN = 25;
    private static final int SOUND_COOLDOWN_MAX = 40;
    private static final int TICK_INTERVAL = 3; // Faster for bouncing sounds

    // Cached data structures
    private static final Map<String, SoundData> activeSoundTypes = new HashMap<>();
    private static final Map<String, OptionFloat> cachedFloatOptions = new HashMap<>();
    private static final Map<String, OptionBoolean> cachedBooleanOptions = new HashMap<>();

    private static int tickCounter = 0;
    private static boolean lastRainState = false;

    private static class SoundData {
        final int cooldown;
        final long playTime;

        SoundData(int cooldown) {
            this.cooldown = cooldown;
            this.playTime = System.currentTimeMillis();
        }
    }

    // Simple position class for caching
    private static class BlockPosition {
        final int x, y, z;

        BlockPosition(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof BlockPosition)) return false;
            BlockPosition pos = (BlockPosition) obj;
            return x == pos.x && y == pos.y && z == pos.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }

    private static class SoundCandidate {
        final String soundName;
        final BlockPosition position;

        SoundCandidate(String soundName, BlockPosition position) {
            this.soundName = soundName;
            this.position = position;
        }
    }

    public static void tick(Minecraft mc) {
        if (mc.currentWorld == null || mc.thePlayer == null || mc.isGamePaused) {
            if (mc.isGamePaused) {
                cleanup();
            }
            return;
        }

        tickCounter++;

        // Only process every TICK_INTERVAL ticks for performance
        if (tickCounter % TICK_INTERVAL != 0) {
            return;
        }

        RainSoundManager manager = new RainSoundManager();
        manager.tickInternal(mc);
    }

    private void tickInternal(Minecraft mc) {
        updateActiveSounds();

        World world = mc.currentWorld;
        Player player = mc.thePlayer;

        boolean currentlyRaining = isRaining(world);
        float currentIntensity = currentlyRaining ? world.weatherManager.getWeatherIntensity() : 0.0f;

        // Early exit if not raining
        if (!currentlyRaining) {
            if (lastRainState) {
                // Rain just stopped, cleanup
                cleanup();
                lastRainState = false;
            }
            return;
        }

        // Cache player position
        int playerX = (int) Math.floor(player.x);
        int playerY = (int) Math.floor(player.y);
        int playerZ = (int) Math.floor(player.z);

        // Check if we need to play a new sound (bouncing effect)
        if (activeSoundTypes.isEmpty()) {
            playNextRainSound(mc, world, player, playerX, playerY, playerZ, currentIntensity);
        }

        lastRainState = currentlyRaining;
    }

    private void playNextRainSound(Minecraft mc, World world, Player player, int playerX, int playerY, int playerZ, float intensity) {
        List<Integer> coveringBlocks = getCoveringBlocks(world, playerX, playerY, playerZ);
        List<SoundCandidate> soundCandidates = findRainSounds(world, playerX, playerY, playerZ, coveringBlocks);
        if (soundCandidates.isEmpty()) {
            return;
        }
        // Group candidates by material type
        Map<String, List<SoundCandidate>> candidatesByType = new HashMap<>();
        for (SoundCandidate candidate : soundCandidates) {
            String materialType = getMaterialType(candidate.soundName);
            if (materialType != null) {
                candidatesByType.computeIfAbsent(materialType, k -> new ArrayList<>()).add(candidate);
            }
        }
        // Play one sound per available material type
        for (List<SoundCandidate> candidates : candidatesByType.values()) {
            if (!candidates.isEmpty()) {
                SoundCandidate candidate = candidates.get(RANDOM.nextInt(candidates.size()));
                if (shouldPlaySound(candidate.soundName, candidate.position)) {
                    GameSettings settings = Minecraft.getMinecraft().gameSettings;
                    boolean isUnderCover = !coveringBlocks.isEmpty();
                    playRainSound(mc, world, player, candidate, settings, intensity, isUnderCover);
                }
            }
        }
    }

    private void updateActiveSounds() {
        long currentTime = System.currentTimeMillis();
        activeSoundTypes.entrySet().removeIf(entry -> {
            SoundData sound = entry.getValue();
            return currentTime - sound.playTime > sound.cooldown * 50; // Convert ticks to milliseconds
        });
    }

    private boolean shouldPlaySound(String soundName, BlockPosition position) {
        if (soundName == null || soundName.equals("ambient.weather.rain")) {
            return false;
        }
        String materialType = getMaterialType(soundName);
        if (materialType == null) return false;
        // Only check if this material type is on cooldown
        return !activeSoundTypes.containsKey(materialType);
    }

    private void playRainSound(Minecraft mc, World world, Player player, SoundCandidate candidate,
                              GameSettings settings, float intensity, boolean isUnderCover) {

        float baseVolume = calculateBaseVolume(intensity);

        // Distance from player to sound source (block center) for spatial falloff
        double dx = (candidate.position.x + 0.5) - player.x;
        double dy = (candidate.position.y + 0.5) - player.y;
        double dz = (candidate.position.z + 0.5) - player.z;
        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Stronger distance falloff so sound clearly comes from the block (inverse distance)
        float distanceMultiplier = 1.0f / (1.0f + distance);
        baseVolume *= distanceMultiplier;

        float volume = applyVolumeModifiers(baseVolume, candidate.soundName, settings, isUnderCover);

        if (volume <= 0.01f) {
            return; // Don't play inaudible sounds
        }

        float pitch = 0.8f + RANDOM.nextFloat() * 0.4f;

        SoundCategory soundCategory = getSoundCategory(settings);
        float x = (float) (candidate.position.x + 0.5);
        float y = (float) (candidate.position.y + 0.5);
        float z = (float) (candidate.position.z + 0.5);

        // Use client SoundEngine positional API so the sound is truly 3D (comes from the block)
        SoundEngine snd = mc.sndManager;
        if (snd != null) {
            snd.playSoundAt(candidate.soundName, soundCategory, x, y, z, volume, pitch);
        } else {
            world.playSoundEffect(null, soundCategory, x, y, z, candidate.soundName, volume, pitch);
        }

        // Track this sound by material type
        String materialType = getMaterialType(candidate.soundName);
        if (materialType != null) {
            int cooldown = RANDOM.nextInt(SOUND_COOLDOWN_MAX - SOUND_COOLDOWN_MIN + 1) + SOUND_COOLDOWN_MIN;
            activeSoundTypes.put(materialType, new SoundData(cooldown));
        }
    }

    private float calculateBaseVolume(float intensity) {
        return 0.3f * intensity * intensity; // Quadratic scaling for more natural feel
    }

    private float applyVolumeModifiers(float baseVolume, String soundName, GameSettings settings, boolean isUnderCover) {
        float volume = baseVolume;

        // Apply material-specific volume
        volume *= getMaterialVolumeMultiplier(soundName, settings);

        // Apply material-specific muffled volume if under cover
        if (isUnderCover) {
            String muffledName = getMaterialMuffledOptionName(soundName);
            if (muffledName != null) {
                OptionFloat muffledVolume = getCachedFloatOption(settings, muffledName);
                if (muffledVolume != null) {
                    volume *= muffledVolume.value;
                }
            }
        }

        return volume * GLOBAL_GAIN;
    }

    private String getMaterialMuffledOptionName(String soundToPlay) {
        if (soundToPlay.contains("metal")) return "betterthanambiance.metalMuffledVolume";
        if (soundToPlay.contains("glass")) return "betterthanambiance.glassMuffledVolume";
        if (soundToPlay.contains("fabric")) return "betterthanambiance.fabricMuffledVolume";
        if (soundToPlay.contains("lava")) return "betterthanambiance.lavaMuffledVolume";
        if (soundToPlay.contains("foliage")) return "betterthanambiance.foliageMuffledVolume";
        if (soundToPlay.contains("water")) return "betterthanambiance.waterMuffledVolume";
        if (soundToPlay.contains("noteblock")) return "betterthanambiance.noteblockMuffledVolume";
        if (soundToPlay.contains("stone")) return "betterthanambiance.stoneMuffledVolume";
        if (soundToPlay.contains("wood")) return "betterthanambiance.woodMuffledVolume";
        if (soundToPlay.contains("plastic")) return "betterthanambiance.plasticMuffledVolume";
        return null;
    }

    private SoundCategory getSoundCategory(GameSettings settings) {
        OptionBoolean useWeatherSounds = getCachedBooleanOption(settings, "betterthanambiance.useWeatherSounds");
        return (useWeatherSounds != null && useWeatherSounds.value) ?
            SoundCategory.WORLD_SOUNDS : SoundCategory.WEATHER_SOUNDS;
    }

    private boolean isRaining(World world) {
        Weather currentWeather = world.getCurrentWeather();
        if (currentWeather == null || !currentWeather.isPrecipitation) {
            return false;
        }

        // Exclude snow-type weathers so we only react to rain-like precipitation
        if (currentWeather == Weathers.OVERWORLD_SNOW ||
            currentWeather == Weathers.OVERWORLD_WINTER_SNOW) {
            return false;
        }

        float intensity = world.weatherManager.getWeatherIntensity();
        return intensity > MIN_WEATHER_INTENSITY;
    }

    private List<Integer> getCoveringBlocks(World world, int playerX, int playerY, int playerZ) {
        List<Integer> coveringBlocks = new ArrayList<>();
        int rainLevel = world.findTopSolidBlock(playerX, playerZ);

        if (playerY >= rainLevel - 1) {
            return coveringBlocks; // Empty list means not under cover
        }

        for (int y = playerY + 1; y <= rainLevel; y++) {
            int blockId = world.getBlockId(playerX, y, playerZ);
            if (blockId > 0 && blockId < Blocks.solid.length) {
                if (Blocks.solid[blockId]) {
                    coveringBlocks.add(blockId);
                } else {
                    Block<?> block = Blocks.blocksList[blockId];
                    if (block != null && block.isSolidRender()) {
                        coveringBlocks.add(blockId);
                    }
                }
            }
        }

        return coveringBlocks;
    }

    private List<SoundCandidate> findRainSounds(World world, int centerX, int centerY, int centerZ, List<Integer> coveringBlocks) {
        List<SoundCandidate> candidates = new ArrayList<>();
        int radiusSquared = SEARCH_RADIUS * SEARCH_RADIUS;
        boolean isUnderCover = !coveringBlocks.isEmpty();

        // Search in a spherical volume around the player
        for (int x = centerX - SEARCH_RADIUS; x <= centerX + SEARCH_RADIUS; x++) {
            for (int z = centerZ - SEARCH_RADIUS; z <= centerZ + SEARCH_RADIUS; z++) {
                for (int y = centerY - SEARCH_RADIUS; y <= centerY + SEARCH_RADIUS; y++) {
                    // Check if block is within spherical radius
                    int dx = x - centerX;
                    int dy = y - centerY;
                    int dz = z - centerZ;
                    int distanceSquared = dx*dx + dy*dy + dz*dz;

                    if (distanceSquared > radiusSquared) {
                        continue;
                    }

                    int surfaceY = world.findTopSolidBlock(x, z);

                    // Only check blocks that can be rained on at their surface level
                    if (y != surfaceY - 1) {
                        continue;
                    }

                    if (world.canBlockBeRainedOn(x, surfaceY, z)) {
                        int blockId = world.getBlockId(x, y, z);

                        // Check if we're specifically under this type of block
                        boolean effectivelyUnderCover = isUnderCover &&
                            coveringBlocks.stream().anyMatch(coverId ->
                                BlockTypeMappings.getMaterialType(coverId) == BlockTypeMappings.getMaterialType(blockId));

                        String sound = getRainSoundForBlock(blockId, effectivelyUnderCover);

                        if (sound != null && !sound.equals("ambient.weather.rain")) {
                            candidates.add(new SoundCandidate(sound, new BlockPosition(x, y, z)));
                        }
                    }
                }
            }
        }

        return candidates;
    }

    private String getRainSoundForBlock(int blockId, boolean isUnderCover) {
        if (blockId <= 0) {
            return null;
        }

        // Optimized material detection with early returns
        if (BlockTypeMappings.METAL_BLOCKS.contains(blockId)) {
            return isUnderCover ? BetterThanAmbianceSounds.RAIN_SOUNDS_METAL_MUFFLED
                                : BetterThanAmbianceSounds.RAIN_SOUNDS_METAL;
        }

        if (BlockTypeMappings.METAL_BLOCKS_THIN.contains(blockId)) {
            return isUnderCover ? BetterThanAmbianceSounds.RAIN_SOUNDS_METAL_THIN : null;
        }

        if (BlockTypeMappings.GLASS_BLOCKS.contains(blockId)) {
            return isUnderCover ? BetterThanAmbianceSounds.RAIN_SOUNDS_GLASS_MUFFLED
                                : BetterThanAmbianceSounds.RAIN_SOUNDS_GLASS;
        }

        if (BlockTypeMappings.FABRIC_BLOCKS.contains(blockId)) {
            return isUnderCover ? BetterThanAmbianceSounds.RAIN_SOUNDS_FABRIC_MUFFLED
                                : BetterThanAmbianceSounds.RAIN_SOUNDS_FABRIC;
        }

        if (BlockTypeMappings.FABRIC_BLOCKS_THIN.contains(blockId)) {
            return isUnderCover ? BetterThanAmbianceSounds.RAIN_SOUNDS_FABRIC_THIN : null;
        }

        if (BlockTypeMappings.FOLIAGE_BLOCKS.contains(blockId)) {
            return BetterThanAmbianceSounds.RAIN_SOUNDS_FOLIAGE;
        }

        if (BlockTypeMappings.WATER_BLOCKS.contains(blockId)) {
            return BetterThanAmbianceSounds.RAIN_SOUNDS_WATER;
        }

        if (BlockTypeMappings.LAVA_BLOCKS.contains(blockId)) {
            return BetterThanAmbianceSounds.RAIN_SOUNDS_LAVA;
        }

        if (BlockTypeMappings.NOTEBLOCK_BLOCKS.contains(blockId)) {
            return BetterThanAmbianceSounds.RAIN_SOUNDS_NOTEBLOCK;
        }

        if (BlockTypeMappings.STONE_BLOCKS.contains(blockId)) {
            return isUnderCover ? BetterThanAmbianceSounds.RAIN_SOUNDS_STONE_MUFFLED
                                : BetterThanAmbianceSounds.RAIN_SOUNDS_STONE;
        }

        if (BlockTypeMappings.WOOD_BLOCKS.contains(blockId)) {
            return isUnderCover ? BetterThanAmbianceSounds.RAIN_SOUNDS_WOOD_MUFFLED : null;
        }

        if (BlockTypeMappings.PLASTIC_BLOCKS.contains(blockId)) {
            return BetterThanAmbianceSounds.RAIN_SOUNDS_PLASTIC;
        }

        return null;
    }

    // Optimized option caching methods
    private static OptionFloat getCachedFloatOption(GameSettings settings, String name) {
        return cachedFloatOptions.computeIfAbsent(name, key -> {
            for (net.minecraft.client.option.Option<?> option : GameSettings.options) {
                if (option instanceof OptionFloat && option.name.equals(key)) {
                    return (OptionFloat) option;
                }
            }
            return null;
        });
    }

    private static OptionBoolean getCachedBooleanOption(GameSettings settings, String name) {
        return cachedBooleanOptions.computeIfAbsent(name, key -> {
            for (net.minecraft.client.option.Option<?> option : GameSettings.options) {
                if (option instanceof OptionBoolean && option.name.equals(key)) {
                    return (OptionBoolean) option;
                }
            }
            return null;
        });
    }

    private float getMaterialVolumeMultiplier(String soundToPlay, GameSettings settings) {
        // Use a lookup table for better performance
        String optionName = getMaterialVolumeOptionName(soundToPlay);
        if (optionName != null) {
            OptionFloat opt = getCachedFloatOption(settings, optionName);
            return opt != null ? opt.value : 1.0f;
        }
        return 1.0f;
    }

    private String getMaterialVolumeOptionName(String soundToPlay) {
        if (soundToPlay.contains("metal")) return "betterthanambiance.metalRainVolume";
        if (soundToPlay.contains("glass")) return "betterthanambiance.glassRainVolume";
        if (soundToPlay.contains("fabric")) return "betterthanambiance.fabricRainVolume";
        if (soundToPlay.contains("lava")) return "betterthanambiance.lavaRainVolume";
        if (soundToPlay.contains("foliage")) return "betterthanambiance.foliageRainVolume";
        if (soundToPlay.contains("water")) return "betterthanambiance.waterRainVolume";
        if (soundToPlay.contains("noteblock")) return "betterthanambiance.noteblockRainVolume";
        if (soundToPlay.contains("stone")) return "betterthanambiance.stoneRainVolume";
        if (soundToPlay.contains("wood")) return "betterthanambiance.woodRainVolume";
        if (soundToPlay.contains("plastic")) return "betterthanambiance.plasticRainVolume";
        return null;
    }

    // Helper to get material type string from sound name
    private String getMaterialType(String soundName) {
        if (soundName == null) return null;
        if (soundName.contains("metal")) return "metal";
        if (soundName.contains("glass")) return "glass";
        if (soundName.contains("fabric")) return "fabric";
        if (soundName.contains("lava")) return "lava";
        if (soundName.contains("foliage")) return "foliage";
        if (soundName.contains("water")) return "water";
        if (soundName.contains("noteblock")) return "noteblock";
        if (soundName.contains("stone")) return "stone";
        if (soundName.contains("wood")) return "wood";
        if (soundName.contains("plastic")) return "plastic";
        return null;
    }

    // Cleanup method for better memory management
    private static void cleanup() {
        activeSoundTypes.clear();
        // Don't clear option caches as they should persist
    }

    // Public method to force cleanup (useful for world changes)
    public static void forceCleanup() {
        cleanup();
        cachedFloatOptions.clear();
        cachedBooleanOptions.clear();
        lastRainState = false;
        tickCounter = 0;
    }
}
