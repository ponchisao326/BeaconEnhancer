package ponchisaohosting.xyz.beaconenhancer.mixins;

import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ponchisaohosting.xyz.beaconenhancer.BeaconEnhancer;

import java.io.*;

import static ponchisaohosting.xyz.beaconenhancer.BeaconEnhancer.netherStar;

@Mixin(World.class)
public class WorldMixin {

    @Shadow @Final private boolean debugWorld;

    @Inject(method = "setBlockState*", at = @At("HEAD"))
    private void setBlockStateMixin(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> ci) {
        if (state.getBlock() instanceof BeaconBlock) {
            createTXT(BeaconEnhancer.levelName, pos);
        }
    }

    @Inject(method = "removeBlock", at = @At("HEAD"))
    private void setBlockRemoved(BlockPos pos, boolean move, CallbackInfoReturnable<Boolean> cir) {
        if (BeaconEnhancer.actualWorld.getBlockState(pos).getBlock() instanceof BeaconBlock) {
            deleteTXT(BeaconEnhancer.levelName, pos);
        }
    }

    @Unique
    private static void createTXT(String world, BlockPos pos) {
        File theDir = new File("beacon-enhancer");
        File worldDir = new File("beacon-enhancer/" + world);
        if (!theDir.exists()){
            theDir.mkdirs();
        }
        if (!worldDir.exists()){
            worldDir.mkdirs();
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        netherStar = 0;
        File posFile = new File(worldDir.toString() + "/" + x + "_" + y + "_" + z + ".txt");
        if (!posFile.exists()){
            try (FileWriter file = new FileWriter(worldDir.toString() + "/" +  x + "_" + y + "_" + z + ".txt")) {
                file.write(Integer.toString(netherStar));
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Unique
    private static void deleteTXT(String world, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        File worldDir = new File("beacon-enhancer/" + world +  "/" + x + "_" + y + "_" + z + ".txt");
        try {
            worldDir.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
