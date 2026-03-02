package coffee.axle.coffeeclient.coffeeauth.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Placeholder mixin — hooks into the client tick for future use.
 */
@Mixin(Minecraft.class)
public class MixinRunTick {

    @Inject(method = "runTick", at = @At("HEAD"))
    private void onRunTick(CallbackInfo ci) {
        // Reserved for future tick-based auth checks.
    }
}
