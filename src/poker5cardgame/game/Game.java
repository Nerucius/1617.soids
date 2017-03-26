package poker5cardgame.game;

import poker5cardgame.ai.ArtificialIntelligence;
import poker5cardgame.ai.IntelligentServerAI;
import poker5cardgame.ai.RandomServerAI;
import poker5cardgame.io.Source;
import poker5cardgame.game.GameState.Action;
import static poker5cardgame.Log.*;

public class Game {

    private static final int INITIAL_BET = 100;
    private static final int INITIAL_SERVER_CHIPS = 10000;
    private static final int INITIAL_CLIENT_CHIPS = 1000;
    
    // TODO manage cartes dolentes
    // TODO manage all in en general
    // TODO maxbet i minbet per ia i que sempre jugui sense enviar errors
    // fet TODO es pot fer BET 5 !!!
    
    /**
     * Source to be used for receiving and sending data to another player.
     */
    private Source IOSource;
    /**
     * Source to be used to get the next move on the local game.
     */
    private Source playerSource;
    private GameData gameData;
    private GameState gameState;

    /**
     * Create a new Game Instance with the given source. Which is in charge of
     * providing the game with the means of communicating with the exterior
     * world in the language the game understands, Actions.
     *
     * @param IOSource input/output source
     * @param aiType Type of AI (Random / Advanced)
     */
    public Game(Source IOSource, ArtificialIntelligence.Type aiType) {
        this.IOSource = IOSource;

        this.gameData = new GameData();
        this.gameState = new GameState();

        switch (aiType) {
            case AI_RANDOM:
                playerSource = new RandomServerAI(gameData, gameState);
                break;
            case AI_INTELLIGENT:
                playerSource = new IntelligentServerAI(gameData, gameState);
                break;
            default:
                playerSource = new RandomServerAI(gameData, gameState);
        }

        GAME_DEBUG(gameData.cId, "Game: Creating new with AI " + aiType);
    }

    /**
     * Create a new Game. By default this creates a Random AI Game
     *
     * @param IOSource Source to be used for Sending/Receiving from the other
     * player
     */
    public Game(Source IOSource) {
        this(IOSource, ArtificialIntelligence.Type.AI_RANDOM);
    }

    /**
     * Create a new Game, using the given sources to Get and Send Moves
     *
     * @param IOSource Source to be used for Sending/Receiving from the other
     * player
     * @param playerSource Source to be used locally to get Moves
     */
    public Game(Source IOSource, Source playerSource) {
        this.IOSource = IOSource;
        this.playerSource = playerSource;
        this.gameData = new GameData();
        this.gameState = new GameState();
    }

    public GameState.State getState() {
        return gameState.state;
    }

    /**
     * Run the game with the next iteration of commands
     */
    public void update() {

        Move move = new Move();
        move.action = Action.NOOP;

        try {
            switch (getState()) {
                
                case INIT:
                    move = getValidMove(IOSource);
                    gameData.save(move, false);
                    break;

                case START:
                    move = new Move();
                    move.action = Action.ANTE_STAKES;
                    move.chips = INITIAL_BET;
                    move.cStakes = INITIAL_CLIENT_CHIPS;
                    move.sStakes = INITIAL_SERVER_CHIPS;                    
                    IOSource.sendMove(move);
                    gameData.save(move, true);
                    break;
                    
                case ACCEPT_ANTE:                    
                    move = getValidMove(IOSource);
                    gameData.save(move,false);
                    break;
                
                case QUIT:                  
                    move = new Move();
                    move.action = Action.QUIT;
                    IOSource.sendMove(move);
                    break;

                case PLAY:
                    move = new Move();
                    
                    // Generate the server and client hands
                    gameData.deck = new Deck();
                    gameData.cHand.draw5FromDeck(gameData.deck);
                    gameData.sHand.draw5FromDeck(gameData.deck);

                    move.action = Action.DEALER_HAND;
                    if (gameData.dealer == -1) // We want a random dealer only for the first game round
                        move.dealer = Math.random() > 0.5 ? 1 : 0;
                    else // Then, the dealer alternates in every game round
                        move.dealer = 1 - gameData.dealer;
                    move.cards = new Card[Hand.SIZE];
                    gameData.cHand.dumpArray(move.cards);
                    
                    gameState.setServerTurn(move.dealer == 1);
                    
                    IOSource.sendMove(move);
                    gameData.save(move, true);
                    break;
                
                case BETTING:
                    if(gameState.isServerTurn())
                    {
                        move = getValidMove(playerSource);
                        IOSource.sendMove(move);
                    }
                    else
                    {
                        move = getValidMove(IOSource);
                        if(move.action == Action.BET && !ArtificialIntelligence.possibleBet(gameData, move.chips, false))
                            throw new Exception("Logic Error. Not valid chips value."); 
                    }
                    gameData.save(move, gameState.isServerTurn());
                    gameState.setServerTurn(!gameState.isServerTurn());
                    break;

                case BETTING_DEALER:
                    if(gameState.isServerTurn())
                    {
                        move = getValidMove(playerSource);
                        IOSource.sendMove(move);
                    }
                    else
                    {
                        move = getValidMove(IOSource);
                        if(move.action == Action.BET && !ArtificialIntelligence.possibleBet(gameData, move.chips, false))
                            throw new Exception("Logic Error. Not valid chips value."); 
                    }
                    gameData.save(move, gameState.isServerTurn());
                    gameState.setServerTurn(!gameState.isServerTurn());
                    break;

                case COUNTER:
                    if(gameState.isServerTurn())
                    {
                        move = getValidMove(playerSource);
                        IOSource.sendMove(move);
                    }
                    else
                    {
                        move = getValidMove(IOSource);     
                        if(move.action == Action.RAISE && !ArtificialIntelligence.possibleRaise(gameData, move.chips, false))
                            throw new Exception("Logic Error. Not valid chips value."); 
                        if(move.action == Action.CALL && !ArtificialIntelligence.possibleCall(gameData, false))
                            throw new Exception("Logic Error. Not valid chips value.");                     
                    }
                    if(move.action == Action.FOLD)   
                        gameState.setFold(true);
                    gameData.save(move, gameState.isServerTurn());
                    gameState.setServerTurn(!gameState.isServerTurn());
                    break;

                case DRAW:
                    move = getValidMove(IOSource);             
                    // TODO: aixo ho ha de controlar el servidor, no el client, ara ho trasllado.
                    // If the indicated number is not matching the cards quantity,
                    // i.e., is grather than the entered number of cards, then
                    // the server sends an error message, but all the indicated 
                    // cards will be discarded.
                    // If the cards are not matching the hand cards, the discard method throws an exception.
                    // But this exception is managed in the clientGame, so it should not reach this exception point.
                    if(move.cDrawn > 0)
                        gameData.cHand.discard(move.cards);     
                    gameData.save(move, false);
                    break;

                case DRAW_SERVER:;
                    move = getValidMove(playerSource);

                    // Complete the server hand with the missing cards
                    gameData.sHand.putNCards(gameData.deck, move.sDrawn);

                    move.cDrawn = gameData.cDrawn;
                    move.cards = gameData.cHand.putNCards(gameData.deck, gameData.cDrawn);
                    
                    gameState.setServerTurn(gameData.dealer == 1);
                    
                    IOSource.sendMove(move);
                    gameData.save(move, true);
                    break;

                case SHOWDOWN:
                    // Show the cards only if show time (i.e. we are not here because of a fold case)
                    if (!gameState.isFold()) {
                        if (!gameState.isShowTime()) {
                            throw new Exception("Server can not show the cards now.");
                        }
                        
                        move = new Move();
                        move.action = Action.SHOW;
                        move.cards = new Card[Hand.SIZE];
                        gameData.sHand.dumpArray(move.cards); 
                        
                        // Manage winner with handranker
                        gameData.sHand.generateRankerInformation();
                        gameData.cHand.generateRankerInformation();
                        if (gameData.sHand.wins(gameData.cHand))
                            move.winner = 0;
                        else if(gameData.cHand.wins(gameData.sHand))
                            move.winner = 1;
                        else
                            move.winner = 2;

                        IOSource.sendMove(move);
                        gameData.save(move, true);
                    }

                    // Ended the game round, so prepare the next one
                    gameState.setFold(false);
                    gameState.setShowTime(false);
                    
                    move = new Move();
                    if(!ArtificialIntelligence.possibleNewRound(gameData))
                    {
                        move.action = Action.TERMINATE;
                        throw new Exception("Logic Error. Not enough chips for the initial bet. Terminates the communication.");
                    }
                    
                    move.action = Action.STAKES;
                    move.cStakes = gameData.cChips;
                    move.sStakes = gameData.sChips;   
                    move.winner = gameData.winner;
                    gameData.save(move, true);
                    IOSource.sendMove(move);
                    break;
            }

            gameState.apply(move.action);
            
            GAME_DEBUG(gameData.cId, "Processed move " + move);

        } catch (Exception ex) {
            this.sendErrorMsg(ex.getMessage());
            if (move.action == Action.TERMINATE)
                gameState.apply(move.action);
                //System.exit(1);           
        }

        System.out.println("\nServer Data: " + gameData + gameState +"\n");
        GAME_DEBUG(gameData.cId, "Updated State: " + gameState + " | Data: " + gameData + '\n');
    }

    private Move updateClient() throws Exception {
        GAME_DEBUG(gameData.cId, "Game: Updating Client");

        // Get next valid move from the other player
        Move cMove = getValidMove(IOSource);

        // manage the move information
        switch (getState()) {
            case INIT:
                //gameData.cId = cMove.id;
                break;
                
            case BETTING:
                if (cMove.action.equals(Action.BET)) {
                    this.manageBetClient(cMove);
                }
                break;

            case BETTING_DEALER:
                if (cMove.action.equals(Action.BET)) {
                    this.manageBetClient(cMove);
                }
                break;

            case COUNTER:
                if (cMove.action.equals(Action.RAISE)) {
                    this.manageRaiseClient(cMove);
                }
                if (cMove.action.equals(Action.CALL)) {
                    this.manageCallClient();
                }
                if (cMove.action.equals(Action.FOLD)) {
                    this.manageFoldClient();
                }
                break;

            case DRAW:
                //gameData.cDrawn = cMove.cDrawn;
                if(gameData.cDrawn > 0) {
                    gameData.cHand.discard(cMove.cards);
                }
                break;

        }

        gameData.save(cMove, gameState.isServerTurn());
        GAME_DEBUG(gameData.cId, "Game: Client Move: " + cMove);
        return cMove;
    }

    private Move updateServer() throws Exception {
        System.out.println("\nServer Data: " + gameData +"\n");

        GAME_DEBUG(gameData.cId, "Game: Updating Server");

        Move sMove = new Move();

        switch (getState()) {
            case START:
                // default behaviour independent of the AI
                sMove.action = Action.ANTE_STAKES;

                // send the game conditions
                sMove.chips = gameData.initialBet;
                sMove.cStakes = gameData.cChips;
                sMove.sStakes = gameData.sChips;
                break;

            case PLAY:
                // default behaviour independent of the AI
                sMove.action = Action.DEALER_HAND;

                // the game is accepted, so set the minimum bet as the bet of each player
                this.setInitialBetServer();
                this.setInitialBetClient();

                // choose the dealer randomly (0: server; 1: client)
                gameData.dealer = Math.random() > 0.5 ? 1 : 0;
                sMove.dealer = gameData.dealer;
               // gameState.setServerTurn(gameData.dealer == 1); // the non dealer has the next turn

                // generate the server and client hands
                gameData.deck = new Deck();
                gameData.cHand.draw5FromDeck(gameData.deck);
                gameData.sHand.draw5FromDeck(gameData.deck);

                // send the client hand
                sMove.cards = new Card[Hand.SIZE];
                gameData.cHand.dumpArray(sMove.cards);

                break;

            case BETTING:
                sMove = getValidMove(playerSource);
                if (sMove.action.equals(Action.BET)) {
                    this.manageBetServer(sMove);
                }
                break;

            case BETTING_DEALER:
                sMove = getValidMove(playerSource);
                if (sMove.action.equals(Action.BET)) {
                    this.manageBetServer(sMove);
                }
                break;

            case COUNTER:
                sMove = getValidMove(playerSource);
                if (sMove.action.equals(Action.RAISE)) {
                    this.manageRaiseServer(sMove);
                }
                if (sMove.action.equals(Action.CALL)) {
                    this.manageCallServer();
                }
                if (sMove.action.equals(Action.FOLD)) {
                    this.manageFoldServer();
                }
                break;

            case DRAW_SERVER:
                // get the move from the ai (who decides which cards the server discards)
                sMove = getValidMove(playerSource);

                // complete the server hand with the missing cards
                gameData.sDrawn = sMove.sDrawn;
                gameData.sHand.putNCards(gameData.deck, gameData.sDrawn);

                // set the client drawn cards
                sMove.cDrawn = gameData.cDrawn;
                //sMove.cards = new Card[sMove.cDrawn];                
                sMove.cards = gameData.cHand.putNCards(gameData.deck, gameData.cDrawn);
                break;

            case SHOWDOWN:
                // Default behaviour independent of the AI
                if (!gameState.isFold()) {
                    if (!gameState.isShowTime()) {
                        throw new Exception("Server can not show the cards now.");
                    }
                    sMove.action = Action.SHOW;
                    sMove.cards = new Card[Hand.SIZE];
                    //gameData.sHand.getCards().toArray(sMove.cards);
                    gameData.sHand.dumpArray(sMove.cards); // TODO pq aquesta funcio si ja ho fa la linia dadalt?
                    IOSource.sendMove(sMove);
                }

                // reset game round flags information   
                gameState.setFold(false);
                gameState.setShowTime(false);
                gameData.sDrawn = 0;
                gameData.cDrawn = 0;

                // manage Winner with handranker
                gameData.sHand.generateRankerInformation();
                gameData.cHand.generateRankerInformation();
                if (gameData.sHand.wins(gameData.cHand)) {
                    this.allChipsToServer();
                    gameData.winner = 0;                 
                } else {
                    this.allChipsToClient();
                    gameData.winner = 1;
                }

                sMove = new Move();
                sMove.action = Action.STAKES;
                sMove.cStakes = gameData.cChips;   // STAKES parameter
                sMove.sStakes = gameData.sChips;   // STAKES parameter
                sMove.winner = gameData.winner;

                break;
        }

        GAME_DEBUG(gameData.cId, "Game: Server Move: " + sMove);
        IOSource.sendMove(sMove);
        return sMove;

    }

    private void setInitialBetServer() throws Exception {
        if (gameData.sChips >= gameData.initialBet) {
            gameData.sBet += gameData.initialBet;
            gameData.sChips -= gameData.initialBet;
        } else {
            gameState.state = GameState.State.QUIT;
            throw new Exception("Logic Error. Not enough chips for the minimum bet.");
        }
    }

    // TODO use that!!!!
    private void setInitialBetClient() throws Exception {
        if (gameData.cChips >= gameData.initialBet) {
            gameData.cChips -= gameData.initialBet;
            gameData.cBet += gameData.initialBet;
        } else {
            gameState.state = GameState.State.QUIT;
            throw new Exception("Logic Error. Not enough chips for the minimum bet.");
        }
    }

    private void manageBetServer(Move move) throws Exception {
        if (gameData.sChips >= move.chips) {
            gameData.sChips -= move.chips;
            gameData.sBet += move.chips;
        } else {
            throw new Exception("Logic Error. Not valid chips value.");
        }
    }

    private void manageBetClient(Move move) throws Exception {
        if (gameData.cChips >= move.chips) {
            gameData.cChips -= move.chips;
            gameData.cBet += move.chips;
        } else {
            throw new Exception("Logic Error. Not valid chips value.");
        }
    }

    private void manageRaiseServer(Move move) throws Exception {
        this._manageCallServer(move.chips);
    }

    private void manageRaiseClient(Move move) throws Exception {
        this._manageCallClient(move.chips);
    }

    private void manageCallServer() throws Exception {
        this._manageCallServer(0);
    }

    private void _manageCallServer(int raise) throws Exception {
        int amountToBet = gameData.cBet - gameData.sBet + raise;
        if (gameData.sChips >= amountToBet) {
            gameData.sChips -= amountToBet;
            gameData.sBet += amountToBet;
        } else {
            throw new Exception("Logic Error. Not enough chips to call or raise.");
        }
    }

    private void manageCallClient() throws Exception {
        this._manageCallClient(0);
    }

    private void _manageCallClient(int raise) throws Exception {
        int amountToBet = gameData.sBet - gameData.cBet + raise;
        if (gameData.cChips >= amountToBet) {
            gameData.cChips -= amountToBet;
            gameData.cBet += amountToBet;
        } else {
            throw new Exception("Logic Error. Not enough chips to call or raise.");
        }
    }

    private void manageFoldServer() {
        gameState.setFold(true);
        this.allChipsToClient();
    }

    private void manageFoldClient() {
        gameState.setFold(true);
        this.allChipsToServer();
    }

    private void allChipsToServer() {
        gameData.sChips += gameData.sBet + gameData.cBet;
        this.resetBets();
    }

    private void allChipsToClient() {
        gameData.cChips += gameData.sBet + gameData.cBet;
        this.resetBets();
    }

    private void resetBets() {
        gameData.cBet = 0;
        gameData.sBet = 0;
    }

    private void sendErrorMsg(String msg) {
        try {
            Move errMove = new Move();
            errMove.action = Action.ERROR;
            errMove.error = msg;
            IOSource.sendMove(errMove);
        } catch (Exception ex) { /* Ignored */ }
    }

    private Move getValidMove(Source src) {
        // get the next  move
        Move move = src.getNextMove();

        while (!gameState.getValidActions().contains(move.action)) {
            this.sendErrorMsg("Protocol Error. Received move: " + move.action
                    + ". Expecting moves: " + gameState.getValidActions());
            move = src.getNextMove();
        }
        // return the valid move
        return move;
    }
}
