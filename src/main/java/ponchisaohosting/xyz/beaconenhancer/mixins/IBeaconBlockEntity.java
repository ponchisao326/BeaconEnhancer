package ponchisaohosting.xyz.beaconenhancer.mixins;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BeaconBlockEntity.class)
public interface IBeaconBlockEntity {

    @Accessor("level")
    int beaconlevel();

    @Accessor("primary")
    StatusEffect primary();

    @Accessor("secondary")
    StatusEffect secondary();

}
