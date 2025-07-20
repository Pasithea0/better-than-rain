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
        // Call our rain sound manager every tick
        RainSoundManager.tick((Minecraft)(Object)this);
    }
}
