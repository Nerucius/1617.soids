package poker5cardgame.game;

import java.util.Collections;
import java.util.Map;
import poker5cardgame.game.Card.Rank;

public class HandRanker {

    public enum HandRank {
        HIGH_CARD,
        ONE_PAIR,
        TWO_PAIR,
        THREE_OF_A_KIND,
        STRAIGHT,
        FLUSH,
        FULL_HOUSE,
        FOUR_OF_A_KIND,
        STRAIGHT_FLUSH;

        public boolean wins(HandRank other) {
            return this.compareTo(other) > 0;
        }

        public boolean ties(HandRank other) {
            return this.compareTo(other) == 0;
        }

        public boolean loses(HandRank other) {
            return this.compareTo(other) < 0;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Public Methods">
    public static HandRank getHandRank(Hand hand) {
        int hSuitId = hand.getSuitId();
        int hWeight = hand.getWeight();
        Map hDict = hand.getOcurDict();

        boolean straight = areSuccessive(hWeight);
        boolean flush = areSameSuit(hSuitId);
        int hDictProduct = computeDictProduct(hDict);

        switch (hDictProduct) {
            case 6:
                // Save required information to manage the tie case
                Rank trioFull = (Rank) Hand.getKeysByValue(hDict, 3).get(0);
                Rank pairFull = (Rank) Hand.getKeysByValue(hDict, 2).get(0);
                hand.putIntoRankDict(HandRank.THREE_OF_A_KIND, trioFull);
                hand.putIntoRankDict(HandRank.ONE_PAIR, pairFull);

                return HandRank.FULL_HOUSE;

            case 4:
                if (hDict.containsValue(4)) {
                    // Save required information to manage the tie case
                    Rank quatrain = (Rank) Hand.getKeysByValue(hDict, 4).get(0);
                    hand.putIntoRankDict(HandRank.FOUR_OF_A_KIND, quatrain);

                    return HandRank.FOUR_OF_A_KIND;
                }
                if (!flush) {
                    // Save required information to manage the tie case
                    Rank highPair = (Rank) Collections.max(Hand.getKeysByValue(hDict, 2));
                    Rank lowPair = (Rank) Collections.min(Hand.getKeysByValue(hDict, 2));
                    hand.putIntoRankDict(HandRank.ONE_PAIR, highPair);
                    hand.putIntoRankDict(HandRank.TWO_PAIR, lowPair);

                    return HandRank.TWO_PAIR;
                }
                // Information about the cards should be treated appart (not in rankDict)
                return HandRank.FLUSH;

            case 3:
                if (!flush) {
                    Rank trio = (Rank) Hand.getKeysByValue(hDict, 3).get(0);
                    hand.putIntoRankDict(HandRank.THREE_OF_A_KIND, trio);
                    // TODO: Information about the 2 remaining cards should be treated appart (not in rankDict)
                    return HandRank.THREE_OF_A_KIND;
                }
                return HandRank.FLUSH;

            case 2:
                if (!flush) {
                    Rank pair = (Rank) Collections.min(Hand.getKeysByValue(hDict, 2));
                    hand.putIntoRankDict(HandRank.ONE_PAIR, pair);
                    // TODO: Information about the 3 remaining cards should be treated appart (not in rankDict)
                    return HandRank.ONE_PAIR;
                }
                return HandRank.FLUSH;

            case 1:
                if (straight) {
                    Rank highCard = (Rank) Collections.max(Hand.getKeysByValue(hDict, 1));
                    // Special case: A 2 3 4 5
                    if (hDict.containsKey(Rank.ACE) && hDict.containsKey(Rank.TWO)) {
                        highCard = Rank.FIVE;
                    }
                    hand.putIntoRankDict(HandRank.HIGH_CARD, highCard);
                    if (flush) {
                        return HandRank.STRAIGHT_FLUSH;
                    }
                    return HandRank.STRAIGHT;
                }
                if (flush) {
                    return HandRank.FLUSH;
                }
                return HandRank.HIGH_CARD;

            default:
                return null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Private Methods">
    private static boolean areSuccessive(int hWeight) {
        return Card.SUCCESSIVE_CARDS.contains(hWeight);
    }

    private static boolean areSameSuit(int hSuitId) {
        // The hand suit id has been generated by the product of the ids of each 
        // suit of the hand, so if all five cards are the same suit, the result 
        // of this product will be a number so that the fifth root is equal to an
        // integer (the suit ids are integer and prime).
        return (float) Math.pow(hSuitId, 1. / 5) % 1 == 0;
    }

    private static int computeDictProduct(Map hDict) {
        // Multiply the number of occurrences of each card rank
        int product = 1;
        for (Object value : hDict.values()) {
            product *= (int) value;
        }
        return product;
    }
    // </editor-fold>
}
