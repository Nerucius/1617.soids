package poker5cardgame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Deck {

    private List<Card> deck;


    /**
     * Returns an empty Deck. Use Deck.generate() to fill with cards.
     */
    public Deck() {
        this.deck = new ArrayList<>();
        this.generate();
    }

    /**
     * Generate a new deck.
     *
     * @return Deck
     */
    private void generate() {
        deck.clear();

        // generate the deck
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                this.deck.add(new Card(suit, rank));
            }
        }
        // shuffle the deck
        Collections.shuffle(deck);
    }

    /**
     * Draw a card from the deck.
     * @return Card
     */
    public Card draw(){
        return this.deck.remove(deck.size() - 1);
    }

    /**
     * Get the size of the deck.
     * @return int
     */
    public int getSize() {
        return this.deck.size();
    }
    
    public class EmptyDeckException extends Exception {
        public EmptyDeckException() {
            super();
        }

        public EmptyDeckException(String message) {
            super(message);
        }

        public EmptyDeckException(String message, Throwable cause) {
            super(message, cause);
        }

        public EmptyDeckException(Throwable cause) {
            super(cause);
        }
    }
}
