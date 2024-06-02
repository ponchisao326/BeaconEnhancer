package ponchisaohosting.xyz.beaconenhancer.mixins;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ponchisaohosting.xyz.beaconenhancer.BeaconEnhancer;

import java.util.Iterator;
import java.util.List;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin {

    @Inject(method = "updateLevel", at = @At("RETURN"), cancellable = true)
    private static void rangeModifier(World world, int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        // Obtener el beacon en la posición dada
        BeaconBlockEntity beacon = (BeaconBlockEntity) world.getBlockEntity(new BlockPos(x, y, z));
        if (beacon == null) {
            return;
        }
        int beaconLevel = ((IBeaconBlockEntity) beacon).beaconlevel();

        // Verificar si el beacon está en su nivel máximo (nivel 4)
        if (beaconLevel < 4) {
            return; // No hacer nada si el beacon no está en el nivel máximo
        }

        int customLevel = calculateCustomLevel(world, x, y, z, beacon);
        cir.setReturnValue(customLevel);
    }

    private static int calculateCustomLevel(World world, int x, int y, int z, BeaconBlockEntity beacon) {
        int customLevel = 1;
        BlockPos actualBlock = new BlockPos(x, y, z);
        int netherStarCounter = BeaconEnhancer.getData(BeaconEnhancer.levelName, actualBlock);

        double baseRadius = 50.0; // Radio base de un beacon maxeado
        double additionalArea = 7850.0; // Área adicional por cada estrella del Nether

        // Calcular el nuevo radio basado en el número de estrellas del Nether
        double customRadius = Math.sqrt((baseRadius * baseRadius) + (netherStarCounter * additionalArea / Math.PI));

        for (int level = 1; level <= 4; level++) {
            int k = y - level;
            if (k < world.getBottomY()) {
                break;
            }
            boolean flag = true;
            for (int i = x - level; i <= x + level && flag; ++i) {
                for (int j = z - level; j <= z + level; ++j) {
                    if (!world.getBlockState(new BlockPos(i, k, j)).isIn(BlockTags.BEACON_BASE_BLOCKS)) {
                        flag = false;
                        break;
                    }
                }
            }
            if (!flag) {
                break;
            }
            if ((level * 10 + 10) * (level * 10 + 10) <= customRadius * customRadius) {
                customLevel = level;
            }
        }

        // Aplicar efectos del beacon a los jugadores dentro del rango
        applyBeaconEffects(world, x, y, z, customRadius, beacon);

        return customLevel;
    }

    private static void applyBeaconEffects(World world, int x, int y, int z, double customRadius, BeaconBlockEntity beacon) {
        int beaconLevel = ((IBeaconBlockEntity) beacon).beaconlevel();
        StatusEffect primaryEffect = ((IBeaconBlockEntity) beacon).primary();
        StatusEffect secondaryEffect = ((IBeaconBlockEntity) beacon).secondary();

        for (PlayerEntity player : world.getPlayers()) {
            BlockPos playerPos = player.getBlockPos();
            double distanceSquared = playerPos.getSquaredDistance(x, y, z);
            if (distanceSquared <= customRadius * customRadius) {
                applyPlayerEffects(world, playerPos, beaconLevel, primaryEffect, secondaryEffect);
            }
        }
    }

    private static void applyPlayerEffects(World world, BlockPos pos, int beaconLevel, @Nullable StatusEffect primaryEffect, @Nullable StatusEffect secondaryEffect) {
        if (!world.isClient && primaryEffect != null) {
            double d = (double) (beaconLevel * 10 + 10);
            int i = 0;
            if (beaconLevel >= 4 && primaryEffect == secondaryEffect) {
                i = 1;
            }

            int j = (9 + beaconLevel * 2) * 20;
            Box box = (new Box(pos)).expand(d).stretch(0.0, (double) world.getHeight(), 0.0);
            List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, box);
            Iterator<PlayerEntity> var11 = list.iterator();

            PlayerEntity playerEntity;
            while (var11.hasNext()) {
                playerEntity = var11.next();
                playerEntity.addStatusEffect(new StatusEffectInstance(primaryEffect, j, i, true, true));
            }

            if (beaconLevel >= 4 && primaryEffect != secondaryEffect && secondaryEffect != null) {
                var11 = list.iterator();
                while (var11.hasNext()) {
                    playerEntity = var11.next();
                    playerEntity.addStatusEffect(new StatusEffectInstance(secondaryEffect, j, 0, true, true));
                }
            }
        }
    }
}
