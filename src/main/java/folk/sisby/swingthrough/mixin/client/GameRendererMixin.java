package folk.sisby.swingthrough.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	private boolean swingthrough$validBlock = false;
	private boolean swingthrough$validEntity = false;

	@ModifyVariable(method = "findCrosshairTarget", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/hit/HitResult;getPos()Lnet/minecraft/util/math/Vec3d;"), ordinal = 0)
	private HitResult cacheReach(HitResult hitResult, Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta) {
		swingthrough$validBlock = hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult && MinecraftClient.getInstance().world.getBlockState(((BlockHitResult) hitResult).getBlockPos()).getCollisionShape(MinecraftClient.getInstance().world, ((BlockHitResult) hitResult).getBlockPos()).isEmpty();
		return hitResult;
	}

	@ModifyReceiver(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/HitResult;getType()Lnet/minecraft/util/hit/HitResult$Type;"))
	private HitResult treatTransparentAsMissed(HitResult instance) {
		return swingthrough$validBlock ? BlockHitResult.createMissed(instance.getPos(), Direction.EAST, BlockPos.ofFloored(instance.getPos())) : instance;
	}

	@ModifyVariable(method = "findCrosshairTarget", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"), ordinal = 0)
	private EntityHitResult checkEntityValid(EntityHitResult hitResult, Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta) {
		Entity hitEntity = hitResult == null ? null : hitResult.getEntity();
		swingthrough$validEntity = hitEntity != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().getCameraEntity().getPos().squaredDistanceTo(hitEntity.getPos()) < MathHelper.square(entityInteractionRange) && hitEntity instanceof LivingEntity && !hitEntity.isSpectator() && hitEntity.isAttackable() && !hitEntity.equals(MinecraftClient.getInstance().player.getVehicle());
		return hitResult;
	}

	@ModifyVariable(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;squaredDistanceTo(Lnet/minecraft/util/math/Vec3d;)D", ordinal = 1), ordinal = 4)
	private double ignoreBlockHit(double original) {
		return swingthrough$validBlock && swingthrough$validEntity ? Double.MAX_VALUE : original;
	}
}
