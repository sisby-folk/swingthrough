package folk.sisby.swingthrough.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	private Double swingthrough$reach = null;

	@ModifyVariable(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;squaredDistanceTo(Lnet/minecraft/util/math/Vec3d;)D", ordinal = 0), ordinal = 1)
	private double cacheReach(double original) {
		swingthrough$reach = MinecraftClient.getInstance().crosshairTarget.getType() == HitResult.Type.BLOCK && MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult && MinecraftClient.getInstance().world.getBlockState(((BlockHitResult) MinecraftClient.getInstance().crosshairTarget).getBlockPos()).getCollisionShape(MinecraftClient.getInstance().world, ((BlockHitResult) MinecraftClient.getInstance().crosshairTarget).getBlockPos()).isEmpty() ? original : null;
		return original;
	}

	@ModifyArg(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "method_18075", remap = false), index = 5)
	private double useOriginalReachForEntityRaycast(double original) {
		return swingthrough$reach == null ? original : swingthrough$reach;
	}

	@ModifyVariable(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/EntityHitResult;getPos()Lnet/minecraft/util/math/Vec3d;"), ordinal = 1)
	private Entity discardEmptyBlockHit(Entity hitEntity) {
		if (swingthrough$reach != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().getCameraEntity().getPos().squaredDistanceTo(hitEntity.getPos()) < swingthrough$reach && hitEntity instanceof LivingEntity && !hitEntity.isSpectator() && hitEntity.isAttackable() && !hitEntity.equals(MinecraftClient.getInstance().player.getVehicle())) {
			MinecraftClient.getInstance().crosshairTarget = null;
		}
		return hitEntity;
	}
}
