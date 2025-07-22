package com.pasithea0.betterthanrain.mixin;

import com.pasithea0.betterthanrain.RainSoundManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, remap = false)
public class MinecraftMixin {

    @Inject(method = "runTick", at = @At("TAIL"))
    private void onRunTick(CallbackInfo ci) {
        // Only call the rain sound manager if it's actually raining
        Minecraft mc = (Minecraft)(Object)this;
        if (mc.currentWorld != null && mc.thePlayer != null) {
            net.minecraft.core.world.weather.Weather currentWeather = mc.currentWorld.getCurrentWeather();
            if (currentWeather != null && currentWeather.isPrecipitation && mc.currentWorld.weatherManager.getWeatherIntensity() > 0.1f) {
                RainSoundManager.tick(mc);
            }
        }
    }
}
