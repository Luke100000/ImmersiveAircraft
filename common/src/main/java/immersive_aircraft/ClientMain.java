package immersive_aircraft;

import immersive_aircraft.network.ClientNetworkManager;

public class ClientMain {
    public static void postLoad() {
        //finish the items
        ItemsClient.postLoad();

        Main.networkManager = new ClientNetworkManager();
    }
}
