package com.pasithea0.betterthanambiance.mixin;

import com.pasithea0.betterthanambiance.CricketSoundManager;
import com.pasithea0.betterthanambiance.RainSoundManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.world.weather.Weathers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, remap = false)
public class MinecraftMixin {

    @Inject(method = "runTick", at = @At("TAIL"))
    private void onRunTick(CallbackInfo ci) {
        Minecraft mc = (Minecraft)(Object)this;
        if (mc.isGamePaused || mc.currentWorld == null || mc.thePlayer == null) {
            return;
        }
        net.minecraft.core.world.weather.Weather currentWeather = mc.currentWorld.getCurrentWeather();
        if (currentWeather != null &&
            (currentWeather instanceof net.minecraft.core.world.weather.IPrecipitation) &&
            currentWeather != Weathers.OVERWORLD_SNOW &&
            currentWeather != Weathers.OVERWORLD_WINTER_SNOW &&
            mc.currentWorld.getWeatherManager().getWeatherIntensity() > 0.1f) {
            RainSoundManager.tick(mc);
        }
        CricketSoundManager.tick(mc);
    }
}
