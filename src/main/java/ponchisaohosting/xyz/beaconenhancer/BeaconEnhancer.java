package ponchisaohosting.xyz.beaconenhancer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import ponchisaohosting.xyz.beaconenhancer.mixins.BeaconBlockEntityAccessor;
import ponchisaohosting.xyz.beaconenhancer.mixins.BeaconBlockEntityMixin;

public class BeaconEnhancer implements ModInitializer {

    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.BEACON)) {
                ItemStack heldItem = player.getStackInHand(hand);
                if (heldItem.getItem() == Items.NETHER_STAR) {
                    BlockPos pos = hitResult.getBlockPos();
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    if (blockEntity instanceof BeaconBlockEntity) {
                        BeaconBlockEntity beaconEntity = (BeaconBlockEntity) blockEntity;
                        BeaconBlockEntityAccessor beaconAccessor = (BeaconBlockEntityAccessor) beaconEntity;
                        beaconAccessor.incrementClickCount();
                        world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                        return ActionResult.SUCCESS;
                    }

                }
            }
            return ActionResult.PASS;
        });
    }

}