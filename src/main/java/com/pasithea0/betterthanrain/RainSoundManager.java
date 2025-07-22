package com.pasithea0.betterthanrain;

import net.minecraft.client.Minecraft;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;
import net.minecraft.core.world.weather.Weather;
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
    private static final int SOUND_COOLDOWN_MIN = 20;
    private static final int SOUND_COOLDOWN_MAX = 50;
    private static final int TICK_INTERVAL = 3; // Faster for bouncing sounds

    // Cached data structures
    private static final Map<String, SoundData> activeSounds = new HashMap<>();
    private static final Set<BlockPosition> checkedPositions = new HashSet<>();
    private static final Map<String, OptionFloat> cachedFloatOptions = new HashMap<>();
    private static final Map<String, OptionBoolean> cachedBooleanOptions = new HashMap<>();

    private static int tickCounter = 0;
    private static boolean lastRainState = false;

    // Sound data class for better management
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
        if (mc.currentWorld == null || mc.thePlayer == null) {
            cleanup();
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
        if (activeSounds.isEmpty()) {
            playNextRainSound(world, player, playerX, playerY, playerZ, currentIntensity);
        }

        lastRainState = currentlyRaining;
    }

    private void playNextRainSound(World world, Player player, int playerX, int playerY, int playerZ, float intensity) {
        List<Integer> coveringBlocks = getCoveringBlocks(world, playerX, playerY, playerZ);
        List<SoundCandidate> soundCandidates = findRainSounds(world, playerX, playerY, playerZ, coveringBlocks);

        if (soundCandidates.isEmpty()) {
            return;
        }

        // Pick a random sound candidate for natural bouncing
        SoundCandidate candidate = soundCandidates.get(RANDOM.nextInt(soundCandidates.size()));

        if (shouldPlaySound(candidate.soundName, candidate.position)) {
            GameSettings settings = Minecraft.getMinecraft().gameSettings;
            boolean isUnderCover = !coveringBlocks.isEmpty();
            playRainSound(world, player, candidate, settings, intensity, isUnderCover);
        }
    }

    private void updateActiveSounds() {
        long currentTime = System.currentTimeMillis();
        activeSounds.entrySet().removeIf(entry -> {
            SoundData sound = entry.getValue();
            return currentTime - sound.playTime > sound.cooldown * 50; // Convert ticks to milliseconds
        });
    }

    private boolean shouldPlaySound(String soundName, BlockPosition position) {
        if (soundName == null || soundName.equals("ambient.weather.rain")) {
            return false;
        }

        // Check if this specific position is on cooldown
        String positionKey = soundName + "_" + position.x + "_" + position.y + "_" + position.z;
        return !activeSounds.containsKey(positionKey);
    }

    private void playRainSound(World world, Player player, SoundCandidate candidate,
                              GameSettings settings, float intensity, boolean isUnderCover) {

        float baseVolume = calculateBaseVolume(intensity);

        // Calculate distance-based volume
        float distance = (float) Math.sqrt(
            (player.x - candidate.position.x) * (player.x - candidate.position.x) +
            (player.y - candidate.position.y) * (player.y - candidate.position.y) +
            (player.z - candidate.position.z) * (player.z - candidate.position.z)
        );

        // Apply distance falloff (closer = louder)
        float distanceMultiplier = Math.max(0.1f, 1.0f - (distance / SEARCH_RADIUS));
        baseVolume *= distanceMultiplier;

        float volume = applyVolumeModifiers(baseVolume, candidate.soundName, settings, isUnderCover);

        if (volume <= 0.01f) {
            return; // Don't play inaudible sounds
        }

        float pitch = 0.8f + RANDOM.nextFloat() * 0.4f;

        // Use cached sound category
        SoundCategory soundCategory = getSoundCategory(settings);

        // Play sound at the exact block position (not random!)
        world.playSoundEffect(null, soundCategory,
                            candidate.position.x + 0.5, candidate.position.y + 0.5, candidate.position.z + 0.5,
                            candidate.soundName, volume, pitch);

        // Track this sound with its position
        int cooldown = RANDOM.nextInt(SOUND_COOLDOWN_MAX - SOUND_COOLDOWN_MIN + 1) + SOUND_COOLDOWN_MIN;
        String positionKey = candidate.soundName + "_" + candidate.position.x + "_" + candidate.position.y + "_" + candidate.position.z;
        activeSounds.put(positionKey, new SoundData(cooldown));
    }

    private float calculateBaseVolume(float intensity) {
        return 0.3f * intensity * intensity; // Quadratic scaling for more natural feel
    }

    private float applyVolumeModifiers(float baseVolume, String soundName, GameSettings settings, boolean isUnderCover) {
        float volume = baseVolume;

        // Apply master rain volume
        OptionFloat masterVolume = getCachedFloatOption(settings, "betterthanrain.masterRainVolume");
        if (masterVolume != null) {
            volume *= masterVolume.value;
        }

        // Apply material-specific volume
        volume *= getMaterialVolumeMultiplier(soundName, settings);

        // Apply muffled volume if under cover
        if (isUnderCover) {
            OptionFloat muffledVolume = getCachedFloatOption(settings, "betterthanrain.muffledVolume");
            if (muffledVolume != null) {
                volume *= muffledVolume.value;
            }
        }

        return volume * GLOBAL_GAIN;
    }

    private SoundCategory getSoundCategory(GameSettings settings) {
        OptionBoolean useWeatherSounds = getCachedBooleanOption(settings, "betterthanrain.useWeatherSounds");
        return (useWeatherSounds != null && useWeatherSounds.value) ?
            SoundCategory.WORLD_SOUNDS : SoundCategory.WEATHER_SOUNDS;
    }

    private boolean isRaining(World world) {
        Weather currentWeather = world.getCurrentWeather();
        return currentWeather != null &&
               currentWeather.isPrecipitation &&
               world.weatherManager.getWeatherIntensity() > MIN_WEATHER_INTENSITY;
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
            return isUnderCover ? BetterThanRainSounds.RAIN_SOUNDS_METAL_MUFFLED
                                : BetterThanRainSounds.RAIN_SOUNDS_METAL;
        }

        if (BlockTypeMappings.METAL_BLOCKS_THIN.contains(blockId)) {
            return isUnderCover ? BetterThanRainSounds.RAIN_SOUNDS_METAL_THIN : null;
        }

        if (BlockTypeMappings.GLASS_BLOCKS.contains(blockId)) {
            return isUnderCover ? BetterThanRainSounds.RAIN_SOUNDS_GLASS_MUFFLED
                                : BetterThanRainSounds.RAIN_SOUNDS_GLASS;
        }

        if (BlockTypeMappings.FABRIC_BLOCKS.contains(blockId)) {
            return isUnderCover ? BetterThanRainSounds.RAIN_SOUNDS_FABRIC_MUFFLED
                                : BetterThanRainSounds.RAIN_SOUNDS_FABRIC;
        }

        if (BlockTypeMappings.FABRIC_BLOCKS_THIN.contains(blockId)) {
            return isUnderCover ? BetterThanRainSounds.RAIN_SOUNDS_FABRIC_THIN : null;
        }

        // Special blocks that always play regardless of cover
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
            return isUnderCover ? BetterThanRainSounds.RAIN_SOUNDS_STONE_MUFFLED
                                : BetterThanRainSounds.RAIN_SOUNDS_STONE;
        }

        if (BlockTypeMappings.WOOD_BLOCKS.contains(blockId)) {
            return isUnderCover ? BetterThanRainSounds.RAIN_SOUNDS_WOOD_MUFFLED : null;
        }

        if (BlockTypeMappings.PLASTIC_BLOCKS.contains(blockId)) {
            return BetterThanRainSounds.RAIN_SOUNDS_PLASTIC;
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
        if (soundToPlay.contains("metal")) return "betterthanrain.metalRainVolume";
        if (soundToPlay.contains("glass")) return "betterthanrain.glassRainVolume";
        if (soundToPlay.contains("fabric")) return "betterthanrain.fabricRainVolume";
        if (soundToPlay.contains("lava")) return "betterthanrain.lavaRainVolume";
        if (soundToPlay.contains("foliage")) return "betterthanrain.foliageRainVolume";
        if (soundToPlay.contains("water")) return "betterthanrain.waterRainVolume";
        if (soundToPlay.contains("noteblock")) return "betterthanrain.noteblockRainVolume";
        if (soundToPlay.contains("stone")) return "betterthanrain.stoneRainVolume";
        if (soundToPlay.contains("wood")) return "betterthanrain.woodRainVolume";
        if (soundToPlay.contains("plastic")) return "betterthanrain.plasticRainVolume";
        return null;
    }

    // Cleanup method for better memory management
    private static void cleanup() {
        activeSounds.clear();
        checkedPositions.clear();
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
