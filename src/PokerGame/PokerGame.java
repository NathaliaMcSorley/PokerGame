package PokerGame;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class PokerGame {

    public static void main(String[] args) throws IOException {
        Deck deck = new Deck();
        deck.shuffle();

        int players = 6;
        String file1 = "file1.csv";
        String file2 = "file2.txt";

        File file1R = new File(file1);
        if (!file1R.exists()) {
            file1R.createNewFile();
        }
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(file1R.getAbsoluteFile(), true));

        File file2R = new File(file2);
        if (!file2R.exists()) {
            file2R.createNewFile();
        }
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(file2R.getAbsoluteFile(), true));

        int totalTries = 500;
        double sumAvg = 0;
        int countWinningRanks[] = new int[9];
        int countTotalRanks[] = new int[9];

        String[] rankStrings = {"High Card", "One Pair", "Two Pair", "Three of Kind", "Straight", "Flush", "Full House", "Four of a Kind", "Straight Flush"};

        for (int round = 1; round <= totalTries; round++) {
            double countWins = 0;
            deck.shuffle();
            Hand hand = new Hand(deck);
            System.out.println("\nPlayer 1: " +rankStrings[hand.getRank()-1]);

            for (int game = 1; game <= totalTries; game++) {
                Hand hands[] = new Hand[5];
                //distribute cards
                for (int j = 2; j <= players; j++) {//
                    hands[j - 2] = new Hand(deck);
                }
                //check if player 1 wins
                int count = 0;
                for (int j = 2; j <= players; j++) {
                    int won = hand.compareTo(hands[j - 2]); //compare hand of player 1 to everybody else's
                    if (won == 1) {
                        count++;
                    }
                }

                if (count == 5) {
                    countWins++;
                    countWinningRanks[hand.getRank()-1]++;
                }

                countTotalRanks[hand.getRank()-1]++;

                for (int j = 2; j <= players; j++) {
                    hands[j - 2].putCardsBackInDeck(deck);
                }

                deck.shuffle();
            }

            double avgWin = (countWins / totalTries) * 100;
            System.out.println("Round " + round + ", Total games: " + totalTries + ", player 1 won: " + countWins + ", Avg: " + avgWin + "%");
            sumAvg += avgWin;
            //first file
            bw1.write(hand.getHandDetails() + "," + rankStrings[hand.getRank()-1] + "," + avgWin + "%" + System.getProperty("line.separator"));
            hand.putCardsBackInDeck(deck);
        }


        System.out.println("\nAverage of averages: " + (sumAvg / totalTries));
        bw1.close();

        //second file
        for(int i = 0; i < 9; i++){
            double percentOfHandsFallingInCurrentRank = ((double) countTotalRanks[i] / (double) (totalTries * totalTries)) * 100;
            double overAllWinPercentageOfCurrentRank = countTotalRanks[i] != 0.0 ? (((double) countWinningRanks[i] / (double) countTotalRanks[i]) * 100) : 0;

            bw2.write("Percent Of Hands Falling in Rank " + rankStrings[i] + " is " + percentOfHandsFallingInCurrentRank
                    + "% and overall win percentage is " + overAllWinPercentageOfCurrentRank + "%" + System.getProperty("line.separator"));
        }
        bw2.close();
        System.out.println("Files written!");
    }
}

//---------------------------------------------------------------------------------------------------------------------------------------
class Card {

    public short value, suit;
    public static String[] suits = {"Hearts", "Spades", "Diamonds", "Clubs"};
    public static String[] values = {"Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King"};

    Card(short suit, short value) {
        this.value = value;
        this.suit = suit;
    }

    public short getValue() {
        return value;
    }

    public short getSuit() {
        return suit;
    }

    public static String getValue(int r) {
        return values[r];
    }

    @Override
    public String toString() {
        return values[value] + " of " + suits[suit];
    }
}
//---------------------------------------------------------------------------------------------------------------------------------------

class Deck {

    static ArrayList<Card> cards;

    Deck() {  //Create Deck
        cards = new ArrayList<>();
        for (short suit = 0; suit <= 3; suit++) {
            for (short rank = 0; rank <= 12; rank++) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    public void shuffle() {  //Shuffle Deck
        Collections.shuffle(cards);
    }

    public Card drawCard() {  //Draw card and remove it from the deck
        return cards.remove(cards.size() - 1);
    }

    public void add(Card c) {  //Add card back to deck
        cards.add(c);
    }
}
//---------------------------------------------------------------------------------------------------------------------------------------

class Hand {

    Card[] cards;
    int[] value;

    Hand(Deck d) {
        cards = new Card[5];  //5 cards, index 0-4
        value = new int[6];  //6 values, index 0-5 where [0] == type of hand, [1][2][3][4][5] == ranks of the cards in the hand in descending order, [1] will break a tie if needed
        for (int x = 0; x < 5; x++) {  //Create hand
            cards[x] = d.drawCard();  //fills up cards[] with 5 cards from the deck and drawCard remove cards from deck
        }

        int[] ranks = new int[14];  //14 ranks, index 0-13 - Array for ranks, rank starts at 1 for ace instead of 0, index 0 is empty
        for (int x = 0; x <= 13; x++) {
            ranks[x] = 0;  //zero the contents of the array
        }       
        for (int x = 0; x <= 4; x++) {
            ranks[cards[x].getValue()]++;      //increment rank array at the index of each card's rank
        }
        
        boolean flush = true; //Assume flush is true and straight is false
        for (int x = 0; x < 4; x++) {
            if (cards[x].getSuit() != cards[x + 1].getSuit()) { //if one of the cards have different suit flush ==false
                flush = false;
            }
        }
        
        int topStraightValue=0;   
        boolean straight=false;  //assume no straight  
        for (int x = 1; x <= 9; x++) { //the lowest value of straight is 10
            if (ranks[x] == 1 && ranks[x + 1] == 1 && ranks[x + 2] == 1 && ranks[x + 3] == 1 && ranks[x + 4] == 1) {
                straight = true;
                topStraightValue = x + 4;  //Highest straight value
                break;
            }
        } 
        if (ranks[10] == 1 && ranks[11] == 1 && ranks[12] == 1 && ranks[13] == 1 && ranks[1] == 1) { //10,J,Q,K,A
            straight = true;
            topStraightValue = 14;
        }
        
        int[] orderedRanks = new int[5];  //5 ordered ranks, index 0-4 - Array for the ordered ranks from player's hand
        int index = 0;
        if (ranks[1] == 1) {  //If ace, run this before because ace is the highest card
            orderedRanks[index] = 14;  //Record an ace as 14 instead of one, as its the highest card
            index++;
        }
        for (int x = 13; x >= 2; x--) {
            if (ranks[x] == 1) {
                orderedRanks[index] = x;
                index++;
            }
        }
        
        int sameCards = 1, sameCards2 = 1;
        int largeGroupRank = 0, smallGroupRank = 0;
        for (int x = 13; x >= 1; x--) {  //Evaluate if cards are the same
        	if (ranks[x] > sameCards)
            {
                if (sameCards != 1)
                //if sameCards was not the default value
                {
                    sameCards2 = sameCards;
                    smallGroupRank = largeGroupRank;
                }
                sameCards = ranks[x];
                largeGroupRank = x;
            } else if (ranks[x] > sameCards2)
            {
                sameCards2 = ranks[x];
                smallGroupRank = x;
            }
        }
        
        for (int x = 0; x <= 5; x++) {
            value[x] = 0;
        }
        
        //Start hand evaluation
        if (sameCards == 1) {  //No pair, high card
            value[0] = 1;  //Lowest type of hand, so it gets the lowest value
            value[1] = orderedRanks[0];  //The first determining factor is the highest card
            value[2] = orderedRanks[1];  //Second highest
            value[3] = orderedRanks[2];  //So on...
            value[4] = orderedRanks[3];
            value[5] = orderedRanks[4];
        }
        if (sameCards == 2 && sameCards2 == 1) {  //1 pair
            value[0] = 2;  //Higher value than high card
            value[1] = largeGroupRank;  //Rank of pair
            value[2] = orderedRanks[0];  //Second highest
            value[3] = orderedRanks[1];  //So on
            value[4] = orderedRanks[2];
        }
        if (sameCards == 2 && sameCards2 == 2) {  //2 pairs
            value[0] = 3;
            value[1] = largeGroupRank > smallGroupRank ? largeGroupRank : smallGroupRank;  //Rank of greater pair
            value[2] = largeGroupRank < smallGroupRank ? largeGroupRank : smallGroupRank;  //Rank of smaller pair
            value[3] = orderedRanks[0];  //Extra card
        }
        if (sameCards == 3 && sameCards2 != 2) {  //3 of a kind
            value[0] = 4;
            value[1] = largeGroupRank;
            value[2] = orderedRanks[0];
            value[3] = orderedRanks[1];
        }
        if (straight && !flush) {  //Straight
            value[0] = 5;
            value[1] = topStraightValue;
        }
        if (flush && !straight) { //flush
            value[0] = 6;
            value[1] = orderedRanks[0];
            value[2] = orderedRanks[1];
            value[3] = orderedRanks[2];
            value[4] = orderedRanks[3];
            value[5] = orderedRanks[4];
        }
        if (sameCards == 3 && sameCards2 == 2) { //Full house
            value[0] = 7;
            value[1] = largeGroupRank;
            value[2] = smallGroupRank;
        }
        if (sameCards == 4) { //Four of a kind
            value[0] = 8;
            value[1] = largeGroupRank;
            value[2] = orderedRanks[0];
        }
        if (straight && flush) {  //Straight flush
            value[0] = 9;
            value[1] = topStraightValue;
        }
    }
    

    public int getRank() {
        return value[0];
    }
    

    public String getHandDetails() {  //Return hand details in string form
        String s = "";
        for (int x = 0; x < 5; x++) {
            s += Card.getValue(cards[x].value) + " of " + Card.suits[cards[x].suit] + ", ";
        }
        return s;
    }
    

    void showCards() {  //Print cards
        for (int x = 0; x < 5; x++) {
            System.out.println();
        }
    }

    int compareTo(Hand that) {  //Compare hands
        for (int x = 0; x < 6; x++)  //Cycle through values
        {
            if (this.value[x] > that.value[x]) {
                return 1;  //Win
            }
            if (this.value[x] < that.value[x]) {
                return -1;  //Lose
            }
        }
        return 0;  //If hands are equal
    }

    void putCardsBackInDeck(Deck deck) { //Put cards back in deck after round is over
        for (int x = 0; x < 5; x++) {
            deck.add(cards[x]);
        }
        cards = new Card[5];
    }
    
}
