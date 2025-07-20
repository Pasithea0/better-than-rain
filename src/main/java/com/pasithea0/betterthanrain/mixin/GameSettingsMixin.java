package com.pasithea0.betterthanrain.mixin;

import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.OptionFloat;
import net.minecraft.client.option.OptionBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import com.pasithea0.betterthanrain.settings.IBetterThanRainOptions;

@Mixin(value = GameSettings.class, remap = false, priority = 2000)
public class GameSettingsMixin implements IBetterThanRainOptions {

    @Unique
    private final GameSettings thisAs = (GameSettings) ((Object) this);

    @Unique
    public OptionFloat masterRainVolume = new OptionFloat(this.thisAs, "betterthanrain.masterRainVolume", 1.0f);

    @Unique
    public OptionFloat metalRainVolume = new OptionFloat(this.thisAs, "betterthanrain.metalRainVolume", 1.0f);

    @Unique
    public OptionFloat glassRainVolume = new OptionFloat(this.thisAs, "betterthanrain.glassRainVolume", 1.0f);

    @Unique
    public OptionFloat fabricRainVolume = new OptionFloat(this.thisAs, "betterthanrain.fabricRainVolume", 1.0f);

    @Unique
    public OptionFloat lavaRainVolume = new OptionFloat(this.thisAs, "betterthanrain.lavaRainVolume", 0.8f);

    @Unique
    public OptionFloat foliageRainVolume = new OptionFloat(this.thisAs, "betterthanrain.foliageRainVolume", 0.8f);

    @Unique
    public OptionFloat waterRainVolume = new OptionFloat(this.thisAs, "betterthanrain.waterRainVolume", 0.8f);

    @Unique
    public OptionFloat noteblockRainVolume = new OptionFloat(this.thisAs, "betterthanrain.noteblockRainVolume", 1.0f);

    @Unique
    public OptionFloat muffledVolumeMultiplier = new OptionFloat(this.thisAs, "betterthanrain.muffledVolumeMultiplier", 1.0f);

    @Unique
    public OptionBoolean useWeatherSounds = new OptionBoolean(this.thisAs, "betterthanrain.useWeatherSounds", false);

    @Override
    public OptionFloat betterthanrain$getMasterRainVolume() {
        return this.masterRainVolume;
    }

    @Override
    public OptionFloat betterthanrain$getMetalRainVolume() {
        return this.metalRainVolume;
    }

    @Override
    public OptionFloat betterthanrain$getGlassRainVolume() {
        return this.glassRainVolume;
    }

    @Override
    public OptionFloat betterthanrain$getFabricRainVolume() {
        return this.fabricRainVolume;
    }

    @Override
    public OptionFloat betterthanrain$getLavaRainVolume() {
        return this.lavaRainVolume;
    }

    @Override
    public OptionFloat betterthanrain$getFoliageRainVolume() {
        return this.foliageRainVolume;
    }

    @Override
    public OptionFloat betterthanrain$getWaterRainVolume() {
        return this.waterRainVolume;
    }

    @Override
    public OptionFloat betterthanrain$getNoteblockRainVolume() {
        return this.noteblockRainVolume;
    }

    @Override
    public OptionFloat betterthanrain$getMuffledVolumeMultiplier() {
        return this.muffledVolumeMultiplier;
    }

    @Override
    public OptionBoolean betterthanrain$getUseWeatherSounds() {
        return this.useWeatherSounds;
    }
}
