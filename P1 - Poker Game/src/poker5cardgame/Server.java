package poker5cardgame;

import poker5cardgame.ai.ArtificialIntelligence;
import poker5cardgame.network.MTGameServer;
import poker5cardgame.network.SGameServer;

/**
 * Server launcher class
 */
public class Server {

    static poker5cardgame.network.Server server;

    static int bindPort = 1212;
    static int mode = 1;
    /** 0 for MT, 1 for Selector */
    static int serverType = 0;

    public static void main(String... args) {

        try {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];

                // Help argument
                if (arg.equals("-h")) {
                    System.out.println("Us: java Server -p <port> [-i 1|2] [-t 0|1]");
                    return;
                }

                // Port Argument
                if (arg.equals("-p")) {
                    String arg2 = args[++i];
                    bindPort = Integer.valueOf(arg2);
                }

                // AI Mode Argument
                if (arg.equals("-i")) {
                    String arg2 = args[++i];
                    mode = Integer.valueOf(arg2);
                }

                // Server Type Argument
                if (arg.equals("-t")) {
                    String arg2 = args[++i];
                    serverType = Integer.valueOf(arg2);
                }

            }
        } catch (Exception e) {
            System.err.println("Server: Exception reading paramenters:");
            e.printStackTrace();
            System.exit(1);
        }

        startServer();

    }

    public static void startServer() {
        System.out.println("Starting Server...");
        System.out.println("Port: " + bindPort);
        System.out.println("Interactive: " + mode);

        // Switch the Type of server here
        switch (serverType) {
            case 0:
                server = new MTGameServer();    // MultiThreaded
                break;
            case 1:
                server = new SGameServer();   // Selector
                break;
            default:
                System.out.println("Invalid server type");
                System.exit(1);
        }

        System.out.println("Type of Server: " + server.getClass().getCanonicalName());

        // Set AI Type from parameter
        ArtificialIntelligence.Type type = ArtificialIntelligence.Type.fromCode(mode);
        server.setAIType(type);

        //server = new SEchoServer();
        server.bind(bindPort);
        server.start();

    }
}
