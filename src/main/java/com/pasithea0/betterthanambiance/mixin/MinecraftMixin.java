package com.pasithea0.betterthanambiance.mixin;

import com.pasithea0.betterthanambiance.*;
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
        RainSoundManager.INSTANCE.tick(mc);
        CricketSoundManager.INSTANCE.tick(mc);
        ThunderSoundManager.INSTANCE.tick(mc);
        UnderWaterSoundsManager.INSTANCE.tick(mc);
    }
}
