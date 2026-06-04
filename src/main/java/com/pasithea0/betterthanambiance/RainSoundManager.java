package com.pasithea0.betterthanambiance;

import net.minecraft.client.sound.SoundEngine;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.weather.IPrecipitation;
import net.minecraft.core.world.weather.Weather;
import net.minecraft.core.world.weather.Weathers;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.client.option.OptionFloat;

import java.util.*;

public class RainSoundManager extends SoundManager {
    public static final RainSoundManager INSTANCE = new RainSoundManager();

    private static final int SEARCH_RADIUS = 6;
    private static final float MIN_WEATHER_INTENSITY = 0.1f;
    private static final float GLOBAL_GAIN = 2.0f;

    private static final int SOUND_COOLDOWN_MIN = 25;
    private static final int SOUND_COOLDOWN_MAX = 40;
    private static final int TICK_INTERVAL = 3;

    private final Map<String, SoundData> activeSoundTypes = new HashMap<>();

    private int tickCounter = 0;
    private boolean lastRainState = false;

    private static class SoundData {
        final int cooldown;
        final long playTime;

        SoundData(int cooldown) {
            this.cooldown = cooldown;
            this.playTime = System.currentTimeMillis();
        }
    }

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

    @Override
    protected void onEnterInvalidState() {
        if (mc != null && mc.isGamePaused) {
            cleanup();
        }
    }

    @Override
    protected void tickInternal() {
        tickCounter++;

        if (tickCounter % TICK_INTERVAL != 0) {
            return;
        }

        updateActiveSounds();

        boolean currentlyRaining = isRaining();
        float currentIntensity = currentlyRaining ? world.getWeatherManager().getWeatherIntensity() : 0.0f;

        if (!currentlyRaining) {
            if (lastRainState) {
                cleanup();
                lastRainState = false;
            }
            return;
        }

        int playerX = (int) Math.floor(player.x);
        int playerY = (int) Math.floor(player.y);
        int playerZ = (int) Math.floor(player.z);

        if (activeSoundTypes.isEmpty()) {
            playNextRainSound(playerX, playerY, playerZ, currentIntensity);
        }

        lastRainState = currentlyRaining;
    }

    private void playNextRainSound(int playerX, int playerY, int playerZ, float intensity) {
        List<Integer> coveringBlocks = getCoveringBlocks(playerX, playerY, playerZ);
        List<SoundCandidate> soundCandidates = findRainSounds(playerX, playerY, playerZ, coveringBlocks);
        if (soundCandidates.isEmpty()) {
            return;
        }

        Map<String, List<SoundCandidate>> candidatesByType = new HashMap<>();
        for (SoundCandidate candidate : soundCandidates) {
            String materialType = getMaterialType(candidate.soundName);
            if (materialType != null) {
                candidatesByType.computeIfAbsent(materialType, k -> new ArrayList<>()).add(candidate);
            }
        }

        for (List<SoundCandidate> candidates : candidatesByType.values()) {
            if (!candidates.isEmpty()) {
                SoundCandidate candidate = candidates.get(RANDOM.nextInt(candidates.size()));
                if (shouldPlaySound(candidate.soundName, candidate.position)) {
                    boolean isUnderCover = !coveringBlocks.isEmpty();
                    playRainSound(candidate, intensity, isUnderCover);
                }
            }
        }
    }

    private void updateActiveSounds() {
        long currentTime = System.currentTimeMillis();
        activeSoundTypes.entrySet().removeIf(entry -> {
            SoundData sound = entry.getValue();
            return currentTime - sound.playTime > sound.cooldown * 50;
        });
    }

    private boolean shouldPlaySound(String soundName, BlockPosition position) {
        if (soundName == null || soundName.equals("ambient.weather.rain")) {
            return false;
        }
        String materialType = getMaterialType(soundName);
        if (materialType == null) {
            return false;
        }
        return !activeSoundTypes.containsKey(materialType);
    }

    private void playRainSound(SoundCandidate candidate, float intensity, boolean isUnderCover) {
        float baseVolume = calculateBaseVolume(intensity);

        double dx = (candidate.position.x + 0.5) - player.x;
        double dy = (candidate.position.y + 0.5) - player.y;
        double dz = (candidate.position.z + 0.5) - player.z;
        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        float distanceMultiplier = 1.0f / (1.0f + distance);
        baseVolume *= distanceMultiplier;

        float volume = applyVolumeModifiers(baseVolume, candidate.soundName, isUnderCover);

        if (volume <= 0.01f) {
            return;
        }

        float pitch = 0.8f + RANDOM.nextFloat() * 0.4f;

        SoundCategory soundCategory = resolveRainCategory();
        float x = (float) (candidate.position.x + 0.5);
        float y = (float) (candidate.position.y + 0.5);
        float z = (float) (candidate.position.z + 0.5);

        SoundEngine snd = mc.sndManager;
        if (snd != null) {
            snd.playSoundAt(candidate.soundName, soundCategory, x, y, z, volume, pitch);
        } else {
            world.playSoundEffect(null, soundCategory, x, y, z, candidate.soundName, volume, pitch);
        }

        String materialType = getMaterialType(candidate.soundName);
        if (materialType != null) {
            int cooldown = RANDOM.nextInt(SOUND_COOLDOWN_MAX - SOUND_COOLDOWN_MIN + 1) + SOUND_COOLDOWN_MIN;
            activeSoundTypes.put(materialType, new SoundData(cooldown));
        }
    }

    private float calculateBaseVolume(float intensity) {
        return 0.3f * intensity * intensity;
    }

    private float applyVolumeModifiers(float baseVolume, String soundName, boolean isUnderCover) {
        float volume = baseVolume;
        volume *= getMaterialVolumeMultiplier(soundName);

        if (isUnderCover) {
            String muffledName = getMaterialMuffledOptionName(soundName);
            if (muffledName != null) {
                OptionFloat muffledVolume = getFloatOption(muffledName);
                if (muffledVolume != null) {
                    volume *= muffledVolume.value;
                }
            }
        }

        return volume * GLOBAL_GAIN;
    }

    private static String getMaterialMuffledOptionName(String soundToPlay) {
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

    private SoundCategory resolveRainCategory() {
        OptionBoolean useWeatherSounds = getBooleanOption("betterthanambiance.useWeatherSounds");
        return (useWeatherSounds != null && useWeatherSounds.value) ?
            SoundCategory.WORLD_SOUNDS : SoundCategory.WEATHER_SOUNDS;
    }

    private boolean isRaining() {
        Weather currentWeather = world.getCurrentWeather();
        if (currentWeather == null || !(currentWeather instanceof IPrecipitation)) {
            return false;
        }

        if (currentWeather == Weathers.OVERWORLD_SNOW ||
            currentWeather == Weathers.OVERWORLD_WINTER_SNOW) {
            return false;
        }

        float intensity = world.getWeatherManager().getWeatherIntensity();
        return intensity > MIN_WEATHER_INTENSITY;
    }

    private List<Integer> getCoveringBlocks(int playerX, int playerY, int playerZ) {
        List<Integer> coveringBlocks = new ArrayList<>();
        int rainLevel = world.findTopSolidBlock(playerX, playerZ);

        if (playerY >= rainLevel - 1) {
            return coveringBlocks;
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

    private List<SoundCandidate> findRainSounds(int centerX, int centerY, int centerZ, List<Integer> coveringBlocks) {
        List<SoundCandidate> candidates = new ArrayList<>();
        int radiusSquared = SEARCH_RADIUS * SEARCH_RADIUS;
        boolean isUnderCover = !coveringBlocks.isEmpty();

        for (int x = centerX - SEARCH_RADIUS; x <= centerX + SEARCH_RADIUS; x++) {
            for (int z = centerZ - SEARCH_RADIUS; z <= centerZ + SEARCH_RADIUS; z++) {
                for (int y = centerY - SEARCH_RADIUS; y <= centerY + SEARCH_RADIUS; y++) {
                    int dx = x - centerX;
                    int dy = y - centerY;
                    int dz = z - centerZ;
                    int distanceSquared = dx*dx + dy*dy + dz*dz;

                    if (distanceSquared > radiusSquared) {
                        continue;
                    }

                    int surfaceY = world.findTopSolidBlock(x, z);

                    if (y != surfaceY - 1) {
                        continue;
                    }

                    if (world.canBlockBeRainedOn(x, surfaceY, z)) {
                        int blockId = world.getBlockId(x, y, z);

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

    private float getMaterialVolumeMultiplier(String soundToPlay) {
        String optionName = getMaterialVolumeOptionName(soundToPlay);
        if (optionName != null) {
            OptionFloat opt = getFloatOption(optionName);
            return opt != null ? opt.value : 1.0f;
        }
        return 1.0f;
    }

    private static String getMaterialVolumeOptionName(String soundToPlay) {
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

    private static String getMaterialType(String soundName) {
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

    private void cleanup() {
        activeSoundTypes.clear();
    }

    public void forceCleanup() {
        cleanup();
        clearOptionCache();
        lastRainState = false;
        tickCounter = 0;
    }

}
