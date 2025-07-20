package com.pasithea0.betterthanrain.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.options.ScreenOptions;
import net.minecraft.client.gui.options.components.FloatOptionComponent;
import net.minecraft.client.gui.options.components.BooleanOptionComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.option.GameSettings;
import net.minecraft.core.item.Items;
import turniplabs.halplibe.util.ClientStartEntrypoint;
import com.pasithea0.betterthanrain.settings.IBetterThanRainOptions;

public class GuiOptionsPageBetterThanRain implements ClientStartEntrypoint {
    public static final GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
    public static final IBetterThanRainOptions rainOptions = (IBetterThanRainOptions) gameSettings;

    public static final OptionsPage BetterThanRainPage = OptionsPages.register(new OptionsPage("betterthanrain.options.title", Items.BUCKET_WATER.getDefaultStack())
            .withComponent(
                    new OptionsCategory("betterthanrain.options.master")
                            .withComponent(new FloatOptionComponent(rainOptions.betterthanrain$getMasterRainVolume()))
                            .withComponent(new FloatOptionComponent(rainOptions.betterthanrain$getMuffledVolumeMultiplier()))
                            .withComponent(new BooleanOptionComponent(rainOptions.betterthanrain$getUseWeatherSounds())))
            .withComponent(new OptionsCategory("betterthanrain.options.materials")
                    .withComponent(new FloatOptionComponent(rainOptions.betterthanrain$getMetalRainVolume()))
                    .withComponent(new FloatOptionComponent(rainOptions.betterthanrain$getGlassRainVolume()))
                    .withComponent(new FloatOptionComponent(rainOptions.betterthanrain$getFabricRainVolume()))
                    .withComponent(new FloatOptionComponent(rainOptions.betterthanrain$getLavaRainVolume()))
                    .withComponent(new FloatOptionComponent(rainOptions.betterthanrain$getFoliageRainVolume()))
                    .withComponent(new FloatOptionComponent(rainOptions.betterthanrain$getWaterRainVolume()))
                    .withComponent(new FloatOptionComponent(rainOptions.betterthanrain$getNoteblockRainVolume()))));

    public static ScreenOptions betterThanRainOptionsScreen(final Screen parent) {
        return new ScreenOptions(parent, BetterThanRainPage);
    }

    @Override
    public void beforeClientStart() {
    }

    @Override
    public void afterClientStart() {
    }
}
