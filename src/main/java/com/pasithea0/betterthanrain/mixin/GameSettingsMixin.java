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
    public OptionFloat muffledVolume = new OptionFloat(this.thisAs, "betterthanrain.muffledVolume", 1.0f);

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
    public OptionFloat waterRainVolume = new OptionFloat(this.thisAs, "betterthanrain.waterRainVolume", 0.7f);

    @Unique
    public OptionFloat noteblockRainVolume = new OptionFloat(this.thisAs, "betterthanrain.noteblockRainVolume", 0.5f);

    @Unique
    public OptionFloat stoneRainVolume = new OptionFloat(this.thisAs, "betterthanrain.stoneRainVolume", 1.0f);

    @Unique
    public OptionFloat woodRainVolume = new OptionFloat(this.thisAs, "betterthanrain.woodRainVolume", 1.0f);

    @Unique
    public OptionFloat plasticRainVolume = new OptionFloat(this.thisAs, "betterthanrain.plasticRainVolume", 8.0f);

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
    public OptionFloat betterthanrain$getStoneRainVolume() {
        return this.stoneRainVolume;
    }

    @Override
    public OptionFloat betterthanrain$getWoodRainVolume() {
        return this.woodRainVolume;
    }

    @Override
    public OptionFloat betterthanrain$getPlasticRainVolume() {
        return this.plasticRainVolume;
    }

    @Override
    public OptionFloat betterthanrain$getMuffledVolume() {
        return this.muffledVolume;
    }

    @Override
    public OptionBoolean betterthanrain$getUseWeatherSounds() {
        return this.useWeatherSounds;
    }
}
