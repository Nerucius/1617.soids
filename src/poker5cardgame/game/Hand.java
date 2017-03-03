/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.game;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import poker5cardgame.game.Card.Rank;
import poker5cardgame.game.HandRanker.HandRank;

/**
 *
 * @author gdempegu11.alumnes
 */
public class Hand {

    private final List<Card> hand;
    private int handValue;

    // The empty constructor is only to test (TODO delete somewhen)
    public Hand() {
        this.hand = new ArrayList();
    }

    public Hand(Deck deck) {
        this.hand = new ArrayList();
        this.draw5FromDeck(deck);
        this.computeHandValue();
    }
    
    /**
     * @param h Other Hand to compare to
     * @return 1 if Win, 0 if tie, -1 if loses.
     */
    // TODO change because HandRank comparasisons have been changed
    public int compareTo(Hand h){
        HandRank ra = HandRanker.getHandRank(this);
        HandRank rb = HandRanker.getHandRank(h);
        if (ra.wins(rb))
            return 1;
        else if(ra.loses(rb))
            return -1;
        
        // TODO This is wrong, needs to first look into the Rank's card value.
        Card highestA = this.getHighCard();
        Card highestB = this.getHighCard();
        return highestA.compareTo(highestB);
    }
    
    public List<Card> getCards() {
        return this.hand;
    }
    
    // TODO message for @alex: He implementat aquest metode pel HandRanker
    public List<Rank> getValues() {
        List<Rank> values = new ArrayList<>();
        
        for(Card c  : hand)
            values.add(c.getRank());
       
        return values;
    }
    
    // TODO message for @alex: He implementat aquest metode pel HandRanker
    public Card getCard(int i) {
        return this.hand.get(i);
    }
    
    public int getHandValue() {
        return this.handValue;
    }
    
    // message for @alex: He implementat aquest metode pel HandRanker
    // message for @sonia: No entenc que esta pasant aqui, magia negra pel ranker?
    // no m'atreveixo a tocar res. a classe ho mirem
    // think how to do it better ?
    // TODO ponderator = 0? (count)
    public void computeHandValue()
    {
        int product = 1;
        List<Rank> values = this.getValues();

        for (Card card : this.getCards()) {
            if (values.isEmpty())
                break;

            int ponderator = 0;
            while (values.contains(card.getRank())) {
                ponderator++;
                values.remove(card.getRank());
            }

            if (ponderator != 0)
                product *= ponderator * card.getValue();
        }
        this.handValue = product;
    }
    
    public int getSuitValue() {
        int product = 1;
        for(Card card : hand)
            product *= card.getSuitValue();
        return product;
    }
    
    public void sort() {
        Collections.sort(hand);
    }
    
    public void draw5FromDeck(Deck deck) {
        for(int i = 0; i<5; i++)
            putCard(deck.draw());
    }
    
    public void putCard(Card c) {
        if(hand.size() < 5)
            hand.add(c);
        else
            System.err.println("Too many cards added to hand.");
    }

    public void discard(Card... cards) {
        for (Card c : cards)
            if(!hand.remove(c))
                System.err.println("Tried to remove a non existing card.");
    }

    @Override
    public String toString() {
        return this.hand.toString();
    }

    private Card getHighCard() {
        Card high = hand.get(0);
        for(Card c : hand)
            high = c.getValue() > high.getValue() ? c : high;
        return high;
    }

}
