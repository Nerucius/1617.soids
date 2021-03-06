package poker5cardgame;

import poker5cardgame.game.ClientGame;
import poker5cardgame.ai.ArtificialIntelligence;

import static poker5cardgame.Log.*;

/**
 * Client launcher class
 */
public class Client {

    static String remoteAddr = "localhost";
    static int remotePort = 1212;
    static int mode = 0;

    static ClientGame client;

    public static void main(String... args) {
        
        // Enable full console logging for incoming and outgoing
        LOG_CLIENT = true;
        LOG_SERVER = true;

        try {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];

                // Help argument
                if (arg.equals("-h")) {
                    System.out.println("Us: java Client -s <maquina_servidora> -p <port> [-i 0|1|2]");
                    return;
                }

                // Server address argument
                if (arg.equals("-s")) {
                    remoteAddr = args[++i];
                }

                // Port Argument
                if (arg.equals("-p")) {
                    String arg2 = args[++i];
                    remotePort = Integer.valueOf(arg2);
                }

                // AI Mode Argument
                if (arg.equals("-i")) {
                    String arg2 = args[++i];
                    mode = Integer.valueOf(arg2);
                }
                


            }
        } catch (Exception e) {
            NET_ERROR("Client: Exception reading paramenters:");
            e.printStackTrace();
            System.exit(1);
        }

        startClient();

    }

    /**
     * Connect to a server and play
     */
    private static void startClient() {
        System.out.println("Starting Client...");
        System.out.println("Remote Addr: " + remoteAddr);
        System.out.println("Port: " + remotePort);
        System.out.println("Interactive: " + mode);

        ClientGame client;
        
        if(mode == 0){
            // Manual mode, engage maximun FANCY
            LOG_CLIENT = false;
            LOG_SERVER = false;
            FANCY_CLIENT = true;
            INFO_CLIENT = true;
            INFO_SERVER = true;
        }
        
        // Read AI type
        ArtificialIntelligence.Type type = ArtificialIntelligence.Type.fromCode(mode);
        if (type != null)
            client = new ClientGame(type);
        else
            client = new ClientGame();

        client.connect(remoteAddr, remotePort);

        while (client.isConnected()) {
            client.update();
        }

        /*
        Move move = new Move();
        move.action = GameState.Action.START;
        move.id = 1234;
        client.getSource().sendMove(move);

        System.out.println(client.getSource().getNextMove());
         */
    }

}
