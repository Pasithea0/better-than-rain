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
import net.minecraft.client.option.OptionFloat;
import net.minecraft.client.option.OptionBoolean;

public class GuiOptionsPageBetterThanRain implements ClientStartEntrypoint {
    public static final GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;

    private static OptionFloat getFloatOption(String name) {
        for (net.minecraft.client.option.Option<?> option : GameSettings.options) {
            if (option instanceof OptionFloat && option.name.equals(name)) {
                return (OptionFloat) option;
            }
        }
        return null;
    }

    private static OptionBoolean getBooleanOption(String name) {
        for (net.minecraft.client.option.Option<?> option : GameSettings.options) {
            if (option instanceof OptionBoolean && option.name.equals(name)) {
                return (OptionBoolean) option;
            }
        }
        return null;
    }

    public static final OptionsPage BetterThanRainPage = OptionsPages.register(new OptionsPage("betterthanrain.options.title", Items.BUCKET_WATER.getDefaultStack())
            .withComponent(
                    new OptionsCategory("betterthanrain.options.master")
                            .withComponent(new FloatOptionComponent(getFloatOption("betterthanrain.masterRainVolume")))
                            .withComponent(new FloatOptionComponent(getFloatOption("betterthanrain.muffledVolume")))
                            .withComponent(new BooleanOptionComponent(getBooleanOption("betterthanrain.useWeatherSounds"))))
            .withComponent(new OptionsCategory("betterthanrain.options.materials")
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanrain.metalRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanrain.glassRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanrain.fabricRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanrain.stoneRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanrain.woodRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanrain.plasticRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanrain.lavaRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanrain.foliageRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanrain.waterRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanrain.noteblockRainVolume")))));

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
