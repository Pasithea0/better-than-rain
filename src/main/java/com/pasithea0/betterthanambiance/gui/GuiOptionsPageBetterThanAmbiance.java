package com.pasithea0.betterthanambiance.gui;

import com.pasithea0.betterthanambiance.SoundManager;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.options.ScreenOptions;
import net.minecraft.client.gui.options.components.FloatOptionComponent;
import net.minecraft.client.gui.options.components.BooleanOptionComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.core.item.Items;
import turniplabs.halplibe.util.ClientStartEntrypoint;

public class GuiOptionsPageBetterThanAmbiance implements ClientStartEntrypoint {

    public static final OptionsPage BetterThanAmbiancePage = OptionsPages.register(new OptionsPage("betterthanambiance.options.title", Items.RECORD_WAIT.getDefaultStack())
            .withComponent(
                    new OptionsCategory("betterthanambiance.options.category")
                            .withComponent(new BooleanOptionComponent(SoundManager.getBooleanOption("betterthanambiance.useWeatherSounds"))))
            .withComponent(new OptionsCategory("betterthanambiance.options.materials")
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.metalRainVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.metalMuffledVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.glassRainVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.glassMuffledVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.fabricRainVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.fabricMuffledVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.stoneRainVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.stoneMuffledVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.woodRainVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.woodMuffledVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.plasticRainVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.plasticMuffledVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.lavaRainVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.lavaMuffledVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.foliageRainVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.foliageMuffledVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.waterRainVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.waterMuffledVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.noteblockRainVolume")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.noteblockMuffledVolume"))))
            .withComponent(new OptionsCategory("betterthanambiance.options.thunder")
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.thunderVolume"))))
            .withComponent(new OptionsCategory("betterthanambiance.options.underwater")
                    .withComponent(new BooleanOptionComponent(SoundManager.getBooleanOption("betterthanambiance.underwaterEnabled")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.underwaterVolume"))))
            .withComponent(new OptionsCategory("betterthanambiance.options.overworld")
                    .withComponent(new BooleanOptionComponent(SoundManager.getBooleanOption("betterthanambiance.overworldEnabled")))
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.overworldVolume"))))
            .withComponent(new OptionsCategory("betterthanambiance.options.crickets")
                    .withComponent(new FloatOptionComponent(SoundManager.getFloatOption("betterthanambiance.cricketsVolume")))));

    public static ScreenOptions betterThanAmbianceOptionsScreen(final Screen parent) {
        return new ScreenOptions(parent, BetterThanAmbiancePage);
    }

    @Override
    public void beforeClientStart() {
    }

    @Override
    public void afterClientStart() {
    }
}
