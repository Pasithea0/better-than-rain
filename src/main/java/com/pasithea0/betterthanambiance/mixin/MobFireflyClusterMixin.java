package com.pasithea0.betterthanambiance.mixin;

import com.pasithea0.betterthanambiance.BetterThanAmbianceSounds;
import net.minecraft.core.entity.animal.MobFireflyCluster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(value = MobFireflyCluster.class, remap = false)
public class MobFireflyClusterMixin {

    private static final Random RANDOM = new Random();

    private static String pickCricketSound() {
        return RANDOM.nextBoolean()
                ? BetterThanAmbianceSounds.CRICKETS_1
                : BetterThanAmbianceSounds.CRICKETS_2;
    }

    // Force Firefly Cluster ambient sound to be our cricket clips
    @Inject(method = "getLivingSound", at = @At("HEAD"), cancellable = true)
    private void betterthanambiance_playCricketSound(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(pickCricketSound());
        cir.cancel();
    }
}
