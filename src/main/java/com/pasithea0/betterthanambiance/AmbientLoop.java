package com.pasithea0.betterthanambiance;

import net.minecraft.client.Minecraft;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.SoundCategory;

public class AmbientLoop {
    private final String soundEvent;
    private final String sourceId;
    private final float baseVolume;
    private float fadeInSpeed;
    private float fadeOutSpeed;

    private float fadeVolume;
    private float targetVolume;

    public AmbientLoop(String soundEvent, String sourceId, float baseVolume) {
        this(soundEvent, sourceId, baseVolume, 0.002f, 0.005f);
    }

    public AmbientLoop(String soundEvent, String sourceId, float baseVolume, float fadeInSpeed, float fadeOutSpeed) {
        this.soundEvent = soundEvent;
        this.sourceId = sourceId;
        this.baseVolume = baseVolume;
        this.fadeInSpeed = fadeInSpeed;
        this.fadeOutSpeed = fadeOutSpeed;
    }

    public void tick(float target, float volumeMultiplier, Minecraft mc, Player player) {
        tick(target, volumeMultiplier, mc, player, 0.0f, 0.0f);
    }

    public void tick(float target, float volumeMultiplier, Minecraft mc, Player player, float offsetX, float offsetZ) {
        targetVolume = target;

        boolean playing = SoundManager.isSourcePlaying(sourceId);

        if (target <= 0.01f) {
            if (playing) {
                fadeVolume = Math.max(0.0f, fadeVolume - fadeOutSpeed);
                if (fadeVolume <= 0.0f) {
                    fadeVolume = 0.0f;
                    SoundManager.stopSource(sourceId);
                } else {
                    SoundManager.setSourceVolume(sourceId, baseVolume * volumeMultiplier * fadeVolume);
                }
            }
            return;
        }

        if (fadeVolume < target) {
            fadeVolume = Math.min(fadeVolume + fadeInSpeed, target);
        } else if (fadeVolume > target) {
            fadeVolume = Math.max(fadeVolume - fadeOutSpeed, target);
        }

        if (!playing) {
            if (fadeVolume > 0.05f) {
                SoundCategory category = SoundManager.isWorldSoundCategory()
                    ? SoundCategory.WORLD_SOUNDS : SoundCategory.WEATHER_SOUNDS;
                mc.sndManager.playSoundWithIdAtPos(soundEvent, category,
                    (float) player.x + offsetX, (float) player.y, (float) player.z + offsetZ,
                    0.001f, 1.0f, sourceId);
                SoundManager.setSourceVolume(sourceId, baseVolume * volumeMultiplier * fadeVolume);
            }
        } else {
            SoundManager.setSourceVolume(sourceId, baseVolume * volumeMultiplier * fadeVolume);
        }
    }

    public void stop() {
        fadeVolume = 0.0f;
        targetVolume = 0.0f;
        SoundManager.stopSource(sourceId);
    }
}
