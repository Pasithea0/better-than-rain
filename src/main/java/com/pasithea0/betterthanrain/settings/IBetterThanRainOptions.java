package com.pasithea0.betterthanrain.settings;

import net.minecraft.client.option.OptionFloat;
import net.minecraft.client.option.OptionBoolean;

public interface IBetterThanRainOptions {
    OptionFloat betterthanrain$getMasterRainVolume();

    OptionFloat betterthanrain$getMetalRainVolume();
    OptionFloat betterthanrain$getGlassRainVolume();
    OptionFloat betterthanrain$getFabricRainVolume();
    OptionFloat betterthanrain$getLavaRainVolume();
    OptionFloat betterthanrain$getFoliageRainVolume();
    OptionFloat betterthanrain$getWaterRainVolume();
    OptionFloat betterthanrain$getNoteblockRainVolume();

    OptionFloat betterthanrain$getMuffledVolumeMultiplier();

    OptionBoolean betterthanrain$getUseWeatherSounds();
}
