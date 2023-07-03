package folk.sisby.swingthrough.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Objects;

@SuppressWarnings("ConstantConditions")
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private MinecraftClient client;
	@Unique private Double cachedReach = null;

	@ModifyVariable(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;squaredDistanceTo(Lnet/minecraft/util/math/Vec3d;)D", ordinal = 0), ordinal = 1)
	private double cacheReach(double original) {
		cachedReach = client.crosshairTarget.getType() == HitResult.Type.BLOCK && client.crosshairTarget instanceof BlockHitResult bhr && this.client.world.getBlockState(bhr.getBlockPos()).getCollisionShape(this.client.world, bhr.getBlockPos()).isEmpty() ? original : null;
		return original;
	}

	@ModifyArg(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"), index = 5)
	private double useOriginalReachForEntityRaycast(double original) {
		return Objects.requireNonNullElse(cachedReach, original);
	}

	@ModifyVariable(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/EntityHitResult;getPos()Lnet/minecraft/util/math/Vec3d;"), ordinal = 1)
	private Entity discardEmptyBlockHit(Entity hitEntity) {
		if (cachedReach != null && this.client.player != null && this.client.player.squaredDistanceTo(hitEntity) < cachedReach && hitEntity instanceof LivingEntity && !hitEntity.isSpectator() && hitEntity.isAttackable() && !(hitEntity instanceof Tameable het && het.getOwnerUuid() == this.client.player.getUuid()) && !hitEntity.equals(this.client.player.getVehicle())) {
			client.crosshairTarget = null;
		}
		return hitEntity;
	}
}
