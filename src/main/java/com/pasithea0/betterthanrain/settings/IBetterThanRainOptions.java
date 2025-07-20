package com.pasithea0.betterthanrain.settings;

import net.minecraft.client.option.OptionFloat;
import net.minecraft.client.option.OptionBoolean;

public interface IBetterThanRainOptions {
    OptionFloat betterthanrain$getMasterRainVolume();
	OptionFloat betterthanrain$getMuffledVolume();

    OptionFloat betterthanrain$getMetalRainVolume();
    OptionFloat betterthanrain$getGlassRainVolume();
    OptionFloat betterthanrain$getFabricRainVolume();
    OptionFloat betterthanrain$getLavaRainVolume();
    OptionFloat betterthanrain$getFoliageRainVolume();
    OptionFloat betterthanrain$getWaterRainVolume();
    OptionFloat betterthanrain$getNoteblockRainVolume();
    OptionFloat betterthanrain$getStoneRainVolume();
    OptionFloat betterthanrain$getWoodRainVolume();
    OptionFloat betterthanrain$getPlasticRainVolume();

    OptionBoolean betterthanrain$getUseWeatherSounds();
}
