package folk.sisby.swingthrough.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private MinecraftClient client;
	@Unique private double cachedOriginalReach = 0.0F;

	@ModifyVariable(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;squaredDistanceTo(Lnet/minecraft/util/math/Vec3d;)D"), ordinal = 1)
	private double cacheOriginalReach(double original) {
		cachedOriginalReach = this.client.world != null && client.crosshairTarget instanceof BlockHitResult bhr && this.client.world.getBlockState(bhr.getBlockPos()).getCollisionShape(this.client.world, bhr.getBlockPos()).isEmpty() ? original : 0;
		return original;
	}

	@ModifyArg(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"), index = 5)
	private double useOriginalReachForEntityRaycast(double original) {
		return cachedOriginalReach != 0 ? cachedOriginalReach : original;
	}

	@ModifyVariable(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/EntityHitResult;getPos()Lnet/minecraft/util/math/Vec3d;"), ordinal = 1)
	private Entity discardEmptyBlockHit(Entity hitEntity, float tickDelta) {
		if (cachedOriginalReach > 0.0F && this.client.player != null && this.client.player.squaredDistanceTo(hitEntity) < cachedOriginalReach && hitEntity instanceof LivingEntity && !hitEntity.isSpectator() && hitEntity.isAttackable() && !(hitEntity instanceof Tameable het && het.getOwnerUuid() == this.client.player.getUuid()) && !hitEntity.equals(this.client.player.getVehicle())) {
			client.crosshairTarget = null;
		}
		return hitEntity;
	}
}
