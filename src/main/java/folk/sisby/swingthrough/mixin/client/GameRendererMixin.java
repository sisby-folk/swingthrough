package folk.sisby.swingthrough.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	private Double swingthrough$reach = null;

	@ModifyVariable(method = "findCrosshairTarget", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/lang/Math;max(DD)D", ordinal = 0), ordinal = 0)
	private double cacheReach(double original) {
		swingthrough$reach = MinecraftClient.getInstance().crosshairTarget.getType() == HitResult.Type.BLOCK && MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult && MinecraftClient.getInstance().world.getBlockState(((BlockHitResult) MinecraftClient.getInstance().crosshairTarget).getBlockPos()).getCollisionShape(MinecraftClient.getInstance().world, ((BlockHitResult) MinecraftClient.getInstance().crosshairTarget).getBlockPos()).isEmpty() ? original : null;
		return original;
	}

	@ModifyArg(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;", remap = false), index = 5)
	private double useOriginalReachForEntityRaycast(double original) {
		return swingthrough$reach == null ? original : swingthrough$reach;
	}

	@ModifyVariable(method = "findCrosshairTarget", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"))
	private EntityHitResult discardEmptyBlockHit(EntityHitResult hitResult) {
		Entity hitEntity = hitResult == null ? null : hitResult.getEntity();
		if (hitEntity != null && swingthrough$reach != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().getCameraEntity().getPos().squaredDistanceTo(hitEntity.getPos()) < swingthrough$reach && hitEntity instanceof LivingEntity && !hitEntity.isSpectator() && hitEntity.isAttackable() && !hitEntity.equals(MinecraftClient.getInstance().player.getVehicle())) {
			MinecraftClient.getInstance().crosshairTarget = null;
		}
		return hitResult;
	}
}
