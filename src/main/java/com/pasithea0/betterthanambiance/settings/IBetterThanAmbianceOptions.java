package com.pasithea0.betterthanambiance.settings;

import net.minecraft.client.option.OptionFloat;
import net.minecraft.client.option.OptionBoolean;

public interface IBetterThanAmbianceOptions {
    OptionFloat betterthanambiance$getMasterRainVolume();
	OptionFloat betterthanambiance$getMuffledVolume();

    OptionFloat betterthanambiance$getMetalRainVolume();
    OptionFloat betterthanambiance$getGlassRainVolume();
    OptionFloat betterthanambiance$getFabricRainVolume();
    OptionFloat betterthanambiance$getLavaRainVolume();
    OptionFloat betterthanambiance$getFoliageRainVolume();
    OptionFloat betterthanambiance$getWaterRainVolume();
    OptionFloat betterthanambiance$getNoteblockRainVolume();
    OptionFloat betterthanambiance$getStoneRainVolume();
    OptionFloat betterthanambiance$getWoodRainVolume();
    OptionFloat betterthanambiance$getPlasticRainVolume();

    OptionBoolean betterthanambiance$getUseWeatherSounds();
}
