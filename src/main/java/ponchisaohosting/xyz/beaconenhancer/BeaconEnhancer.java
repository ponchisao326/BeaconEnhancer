package ponchisaohosting.xyz.beaconenhancer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.io.*;
import java.nio.file.Path;

public class BeaconEnhancer implements ModInitializer {

    public static String levelName;
    public static int netherStar;
    public static World actualWorld;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // Registra el evento para cuando un jugador se conecta al servidor
            ServerPlayConnectionEvents.JOIN.register((handler, sender, server1) -> {
                Path world = handler.player.getWorld().getServer().getSavePath(WorldSavePath.ROOT);
                levelName = world.getParent().getFileName().toString();
                actualWorld = handler.player.getWorld();
            });
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }
            if (world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.BEACON)) {
                ItemStack heldItem = player.getStackInHand(hand);
                if (heldItem.getItem() == Items.NETHER_STAR) {
                    BlockPos pos = hitResult.getBlockPos();
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    if (blockEntity instanceof BeaconBlockEntity) {
                        if (!player.isSneaking()) {
                            player.getStackInHand(hand).decrement(1);
                            updateTXT(levelName, pos);
                            int netherStarCounter = BeaconEnhancer.getData(BeaconEnhancer.levelName, pos);

                            double baseRadius = 50.0; // Radio base de un beacon maxeado
                            double additionalArea = 7850.0; // Área adicional por cada estrella del Nether

                            // Calcular el nuevo radio basado en el número de estrellas del Nether
                            double customRadius = Math.sqrt((baseRadius * baseRadius) + (netherStarCounter * additionalArea / Math.PI));

                            player.sendMessage(Text.of("The new beacon radius is: " + customRadius), false);

                            // Mostrar partículas alrededor del beacon
                            if (world instanceof ServerWorld) {
                                ServerWorld serverWorld = (ServerWorld) world;
                                showParticles(serverWorld, pos);
                            }
                        } else {
                            int netherStarCounter = BeaconEnhancer.getData(BeaconEnhancer.levelName, pos);

                            double baseRadius = 50.0; // Radio base de un beacon maxeado
                            double additionalArea = 7850.0; // Área adicional por cada estrella del Nether

                            // Calcular el nuevo radio basado en el número de estrellas del Nether
                            double customRadius = Math.sqrt((baseRadius * baseRadius) + (netherStarCounter * additionalArea / Math.PI));

                            if (world instanceof ServerWorld) {
                                ServerWorld serverWorld = (ServerWorld) world;
                                showRadiusParticles(serverWorld, pos, customRadius);
                            }
                        }

                        return ActionResult.SUCCESS;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    public void updateTXT(String world, BlockPos pos) {
        File worldDir = new File("beacon-enhancer/" + world);

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        String posFilePath = worldDir.toString() + "/" + x + "_" + y + "_" + z + ".txt";

        File posFile = new File(posFilePath);
        if (posFile.exists()) {
            try (FileReader reader = new FileReader(posFile)) {
                BufferedReader br = new BufferedReader(reader);
                int linea = Integer.parseInt(br.readLine());
                netherStar = linea + 1;
                try (FileWriter file = new FileWriter(posFilePath)) {
                    file.write(Integer.toString(netherStar)); // Escribir el contenido del JSON
                    file.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getData(String world, BlockPos pos) {
        File worldDir = new File("beacon-enhancer/" + world);

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        String posFilePath = worldDir.toString() + "/" + x + "_" + y + "_" + z + ".txt";

        File posFile = new File(posFilePath);
        if (posFile.exists()) {
            try (FileReader reader = new FileReader(posFile)) {
                BufferedReader br = new BufferedReader(reader);
                int linea = Integer.parseInt(br.readLine());
                return linea;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void showParticles(ServerWorld world, BlockPos pos) {
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 1.2;
        double centerZ = pos.getZ() + 0.5;

        int particleCount = 500;
        double radius = 1.0;

        for (int i = 0; i < particleCount; ++i) {
            double angle = 2 * Math.PI * i / particleCount;
            double offsetX = Math.cos(angle) * radius;
            double offsetY = 0.0; // No cambiamos la altura
            double offsetZ = Math.sin(angle) * radius;

            double posX = centerX + offsetX;
            double posY = centerY + offsetY;
            double posZ = centerZ + offsetZ;

            // Ajusta las velocidades de las partículas si es necesario
            double velocityX = 0.0;
            double velocityY = 0.0;
            double velocityZ = 0.0;

            world.spawnParticles(ParticleTypes.DRAGON_BREATH, posX, posY, posZ, 1, velocityX, velocityY, velocityZ, 0.1);
        }
    }

    private void showRadiusParticles(ServerWorld world, BlockPos pos, double customRadius) {
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 1.2;
        double centerZ = pos.getZ() + 0.5;

        int particleCount = 100; // Número de partículas a generar

        for (int i = 0; i < particleCount; ++i) {
            float theta = (float) (Math.random() * Math.PI * 2); // Ángulo aleatorio alrededor del círculo
            float phi = (float) (Math.random() * Math.PI); // Ángulo aleatorio desde la parte superior (0 a pi)

            // Convertir coordenadas esféricas a coordenadas cartesianas
            float x = (float) (Math.cos(theta) * Math.sin(phi));
            float y = (float) (Math.cos(phi));
            float z = (float) (Math.sin(theta) * Math.sin(phi));

            // Escalar el vector al radio deseado
            Vector3f direction = new Vector3f(x, y, z).normalize().mul((float) customRadius);

            // Desplazar la posición por el centro
            Vector3f particlePos = new Vector3f((float) centerX, (float) centerY, (float) centerZ).add(direction);

            // Generar la partícula
            world.spawnParticles(ParticleTypes.DRAGON_BREATH, particlePos.x, particlePos.y, particlePos.z, 1, 0.0, 0.0, 0.0, 0.1);
        }
    }


}
