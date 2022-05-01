package immersive_airships;

import immersive_airships.network.ClientNetworkManager;

public class ClientMain {
    public static void postLoad() {
        //finish the items
        ItemsClient.postLoad();

        Main.networkManager = new ClientNetworkManager();
    }
}
