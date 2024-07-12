package dev.hooman.mixin;

import dev.hooman.AntiLootBlowUp;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

	@Shadow
	public HitResult crosshairTarget;

	@Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
	public void antilootblowup$dontAttackCrystal(CallbackInfoReturnable<Boolean> cir) {
		if(crosshairTarget == null) {
			LoggerFactory.getLogger("anti-loot-blow-up").error("crosshairTarget is null; this shouldn't happen!");
		}

		if(crosshairTarget.getType() == HitResult.Type.ENTITY) {
			Entity entity = ((EntityHitResult)crosshairTarget).getEntity();
			if(AntiLootBlowUp.isCrystal(entity) && AntiLootBlowUp.cannotExplode()) {
				cir.setReturnValue(false);
			}
		}
	}

	@Inject(method = "doItemUse()V", at = @At("HEAD"), cancellable = true)
	public void antilootblowup$dontBlowAnchor(CallbackInfo ci) {
		if (crosshairTarget == null) {
			LoggerFactory.getLogger("anti-loot-blow-up").error("crosshairTarget is null; this shouldn't happen!");
		}

		if (crosshairTarget.getType() == HitResult.Type.BLOCK && MinecraftClient.getInstance().world != null) {
			BlockState blockState = MinecraftClient.getInstance().world.getBlockState(((BlockHitResult)crosshairTarget).getBlockPos());
			if(blockState.getBlock() instanceof RespawnAnchorBlock && blockState.get(RespawnAnchorBlock.CHARGES) > 0 && AntiLootBlowUp.cannotExplode()) {
				ci.cancel();
			}
		}
	}
}
