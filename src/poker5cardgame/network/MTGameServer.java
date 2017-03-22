/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.network;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import poker5cardgame.game.Game;
import poker5cardgame.game.GameState;
import poker5cardgame.io.NetworkSource;

/**
 * 
 */
public class MTGameServer extends MultithreadServer{
    
    Map<Integer, Game> savedGames = new ConcurrentHashMap<>();

    @Override
    public void handleConnection(Socket sock) {
        NetworkSource source = new NetworkSource(sock);
        // TODO @alex 
        source.getCom().setTimeout(0); 
        
        Game game = new Game(source);
        int id = 0;
        // TODO @alex Add persistence
        /*int id = game.getPlayerID();
        if(savedGames.containsKey(id)){
            game = savedGames.get(id);
        }
        */
        
        while(game.getState() != GameState.State.QUIT){
            game.update();
        }
        
        savedGames.put(id, game);
    }
    
}