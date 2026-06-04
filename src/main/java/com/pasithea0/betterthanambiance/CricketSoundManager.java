package com.pasithea0.betterthanambiance;

import net.minecraft.client.sound.SoundEngine;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.client.option.OptionFloat;

public class CricketSoundManager extends SoundManager {
    public static final CricketSoundManager INSTANCE = new CricketSoundManager();

    private static final int MAX_LIGHT_LEVEL = 3;
    private static final int BACKGROUND_COOLDOWN_TICKS = 200;

    private int backgroundCooldown = 0;

    @Override
    protected void tickInternal() {
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

            float volumeScale = getCricketsVolume();
            float volume = 0.4f * volumeScale;

            SoundCategory category = getCricketsCategory();

            snd.playSoundAt(BetterThanAmbianceSounds.CRICKETS_BACKGROUND,
                    category, x, y, z, volume, 1.0f);
        }
        backgroundCooldown = BACKGROUND_COOLDOWN_TICKS;
    }

    private float getCricketsVolume() {
        OptionFloat opt = getFloatOption("betterthanambiance.cricketsVolume");
        return opt != null ? opt.value : 1.0f;
    }

    private SoundCategory getCricketsCategory() {
        return isWorldSoundCategory() ? SoundCategory.WORLD_SOUNDS : SoundCategory.ENTITY_SOUNDS;
    }
}
