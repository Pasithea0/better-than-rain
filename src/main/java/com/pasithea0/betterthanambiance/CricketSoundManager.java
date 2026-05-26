package com.pasithea0.betterthanambiance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;

/**
 * Plays cricket background sound when standing on a block tagged FIREFLIES_CAN_SPAWN
 * and light level is 3 or less.
 */
public class CricketSoundManager {
    private static final int MAX_LIGHT_LEVEL = 3;
    /** Minimum ticks between starting the background cricket sound. */
    private static final int BACKGROUND_COOLDOWN_TICKS = 200;

    private static int backgroundCooldown = 0;

    public static void tick(Minecraft mc) {
        if (mc.isGamePaused || mc.currentWorld == null || mc.thePlayer == null) {
            return;
        }
        World world = mc.currentWorld;
        Player player = mc.thePlayer;

        int playerX = (int) Math.floor(player.x);
        int playerY = (int) Math.floor(player.y);
        int playerZ = (int) Math.floor(player.z);

        int blockUnderY = playerY - 1;
        int light = world.getFullBlockLightValue(playerX, playerY, playerZ);
        if (light > MAX_LIGHT_LEVEL) {
            backgroundCooldown = 0;
            return;
        }

        Block blockUnder = world.getBlock(playerX, blockUnderY, playerZ);
        if (blockUnder == null || !blockUnder.hasTag(BlockTags.FIREFLIES_CAN_SPAWN)) {
            return;
        }

        if (backgroundCooldown > 0) {
            backgroundCooldown--;
            return;
        }

        SoundEngine snd = mc.sndManager;
        if (snd != null) {
            float x = (float) player.x;
            float y = (float) player.y;
            float z = (float) player.z;

            // Scale by the crickets volume option (defaults to 1.0)
            float volumeScale = getCricketsVolume();
            float volume = 0.4f * volumeScale;

            // Choose sound category: ENTITY_SOUNDS by default, or WORLD_SOUNDS
            // when \"Use World Sound Category\" is enabled (same toggle rain uses).
            SoundCategory category = getCricketsCategory();

            // Fixed pitch so the sample plays as-authored
            snd.playSoundAt(BetterThanAmbianceSounds.CRICKETS_BACKGROUND,
                    category, x, y, z, volume, 1.0f);
        }
        backgroundCooldown = BACKGROUND_COOLDOWN_TICKS;
    }

    private static float getCricketsVolume() {
        for (net.minecraft.client.option.Option<?> option : net.minecraft.client.option.GameSettings.getAllOptions()) {
            if (option instanceof net.minecraft.client.option.OptionFloat && option.id.equals("betterthanambiance.cricketsVolume")) {
                return ((net.minecraft.client.option.OptionFloat) option).value;
            }
        }
        return 1.0f;
    }

    private static SoundCategory getCricketsCategory() {
        boolean useWorldCategory = false;
        for (net.minecraft.client.option.Option<?> option : net.minecraft.client.option.GameSettings.getAllOptions()) {
            if (option instanceof net.minecraft.client.option.OptionBoolean
                    && option.id.equals("betterthanambiance.useWeatherSounds")) {
                useWorldCategory = ((net.minecraft.client.option.OptionBoolean) option).value;
                break;
            }
        }
        return useWorldCategory ? SoundCategory.WORLD_SOUNDS : SoundCategory.ENTITY_SOUNDS;
    }
}
