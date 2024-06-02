package ponchisaohosting.xyz.beaconenhancer.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeaconEnhancerClient implements ClientModInitializer {

    public static final String MOD_ID = "beaconenhancer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initiating BeaconEnhancer");
        LOGGER.info("BeaconEnhancer autor: Ponchisao326");
    }
}
