package ponchisaohosting.xyz.beaconenhancer.mixins;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin {

    @Unique
    private int clickCount;
    @Inject(method = "updateLevel", at = @At("RETURN"), cancellable = true)
    private static void rangeModifier(World world, int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        // Modifica el nivel del beacon basado en el radio personalizado
        int customLevel = calculateCustomLevel(world, x, y, z);
        cir.setReturnValue(customLevel);
    }

    private static int calculateCustomLevel(World world, int x, int y, int z) {
        // Lógica para calcular el nivel del beacon basado en un radio personalizado
        // Puedes ajustar esta lógica según tus necesidades
        int customLevel = 1;
        double customRadius = 1000.0; // Radio personalizado
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
            if ((level * 10 + 10) * (level * 10 + 10) <= customRadius * customRadius) { // Aquí verificamos si el radio personalizado es alcanzable
                customLevel = level;
            }

        }

        // Aplicar efectos del beacon a los jugadores dentro del rango
        applyBeaconEffects(world, x, y, z, customRadius);

        return customLevel;
    }

    private static void applyBeaconEffects(World world, int x, int y, int z, double customRadius) {
        // Obtener el beacon en la posición dada
        BeaconBlockEntity beacon = (BeaconBlockEntity) world.getBlockEntity(new BlockPos(x, y, z));
        if (beacon != null) {
            // Ajustar la duración a 16 segundos (320 ticks)
            int duration = 320;

            // Obtener el efecto primario y secundario seleccionados por el usuario en el beacon
            StatusEffect primaryEffect = ((IBeaconBlockEntity) beacon).primary();
            StatusEffect secondaryEffect = ((IBeaconBlockEntity) beacon).secondary();

            // Aplicar efectos a los jugadores dentro del rango del beacon
            for (PlayerEntity player : world.getPlayers()) {
                BlockPos playerPos = player.getBlockPos();
                double distanceSquared = playerPos.getSquaredDistance(x, y, z);
                if (distanceSquared <= customRadius * customRadius) {
                    // Dentro del rango del beacon, aplicar los nuevos efectos
                    applyPlayerEffects(world, playerPos, ((IBeaconBlockEntity) beacon).beaconlevel(), primaryEffect, secondaryEffect);
                }
            }
        }
    }

    private static void applyPlayerEffects(World world, BlockPos pos, int beaconLevel, @Nullable StatusEffect primaryEffect, @Nullable StatusEffect secondaryEffect) {
        if (!world.isClient && primaryEffect != null) {
            double d = (double)(beaconLevel * 10 + 10);
            int i = 0;
            if (beaconLevel >= 4 && primaryEffect == secondaryEffect) {
                i = 1;
            }

            int j = (9 + beaconLevel * 2) * 20;
            Box box = (new Box(pos)).expand(d).stretch(0.0, (double)world.getHeight(), 0.0);
            List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, box);
            Iterator var11 = list.iterator();

            PlayerEntity playerEntity;
            while(var11.hasNext()) {
                playerEntity = (PlayerEntity)var11.next();
                playerEntity.addStatusEffect(new StatusEffectInstance(primaryEffect, j, i, true, true));
            }

            if (beaconLevel >= 4 && primaryEffect != secondaryEffect && secondaryEffect != null) {
                var11 = list.iterator();

                while(var11.hasNext()) {
                    playerEntity = (PlayerEntity)var11.next();
                    playerEntity.addStatusEffect(new StatusEffectInstance(secondaryEffect, j, 0, true, true));
                }
            }

        }
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("clickCount", clickCount);
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    private void readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("clickCount")) {
            clickCount = nbt.getInt("clickCount");
        }
    }

    // Implementación del método incrementClickCount
    public void incrementClickCount() {
        this.clickCount++;
    }

    // Implementación del método getClickCount
    public int getClickCount() {
        return this.clickCount;
    }

}
