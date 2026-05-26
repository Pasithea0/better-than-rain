package com.pasithea0.betterthanambiance.gui;

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

public class GuiOptionsPageBetterThanAmbiance implements ClientStartEntrypoint {


    private static OptionFloat getFloatOption(String name) {
        for (net.minecraft.client.option.Option<?> option : GameSettings.getAllOptions()) {
            if (option instanceof OptionFloat && option.id.equals(name)) {
                return (OptionFloat) option;
            }
        }
        return null;
    }

    private static OptionBoolean getBooleanOption(String name) {
        for (net.minecraft.client.option.Option<?> option : GameSettings.getAllOptions()) {
            if (option instanceof OptionBoolean && option.id.equals(name)) {
                return (OptionBoolean) option;
            }
        }
        return null;
    }

    public static final OptionsPage BetterThanAmbiancePage = OptionsPages.register(new OptionsPage("betterthanambiance.options.title", Items.BUCKET_IRON.getDefaultStack())
            .withComponent(
                    new OptionsCategory("betterthanambiance.options.category")
                            .withComponent(new BooleanOptionComponent(getBooleanOption("betterthanambiance.useWeatherSounds"))))
            .withComponent(new OptionsCategory("betterthanambiance.options.materials")
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.metalRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.metalMuffledVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.glassRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.glassMuffledVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.fabricRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.fabricMuffledVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.stoneRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.stoneMuffledVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.woodRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.woodMuffledVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.plasticRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.plasticMuffledVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.lavaRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.lavaMuffledVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.foliageRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.foliageMuffledVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.waterRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.waterMuffledVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.noteblockRainVolume")))
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.noteblockMuffledVolume"))))
            .withComponent(new OptionsCategory("betterthanambiance.options.thunder")
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.thunderVolume"))))
            .withComponent(new OptionsCategory("betterthanambiance.options.crickets")
                    .withComponent(new FloatOptionComponent(getFloatOption("betterthanambiance.cricketsVolume")))));

    public static ScreenOptions betterThanRainOptionsScreen(final Screen parent) {
        return new ScreenOptions(parent, BetterThanAmbiancePage);
    }

    @Override
    public void beforeClientStart() {
    }

    @Override
    public void afterClientStart() {
    }
}
