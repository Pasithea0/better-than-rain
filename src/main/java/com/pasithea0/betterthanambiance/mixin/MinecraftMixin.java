package com.pasithea0.betterthanambiance.mixin;

import com.pasithea0.betterthanambiance.CricketSoundManager;
import com.pasithea0.betterthanambiance.RainSoundManager;
import com.pasithea0.betterthanambiance.ThunderSoundManager;
import com.pasithea0.betterthanambiance.UnderWaterSoundsManager;
import net.minecraft.client.Minecraft;
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
		RainSoundManager.tick(mc);
        CricketSoundManager.tick(mc);
		ThunderSoundManager.tick(mc);
        UnderWaterSoundsManager.tick(mc);
    }
}
