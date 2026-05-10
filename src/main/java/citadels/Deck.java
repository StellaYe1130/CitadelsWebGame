package citadels;

import java.util.*;

/**
 * The {@code Deck} class represents a stack of cards used in the Citadels game.
 * It allows adding, drawing, shuffling, and accessing cards, providing standard deck operations.
 * Internally, it maintains a list of cards where the top of the deck is at the end of the list.
 */
public class Deck {

    /** Internal list to store the cards in the deck. */
    private final List<Card> cards = new ArrayList<>();

    /**
     * Adds a card to the top of the deck.
     *
     * @param card the {@code Card} to be added to the top
     */
    public void addCard(Card card) {
        cards.add(card);
    }

    /**
     * Returns a copy of the current list of cards in the deck.
     * This method returns a new list to preserve encapsulation.
     *
     * @return a new {@code List} containing all cards in the deck
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Returns the number of cards currently in the deck.
     *
     * @return the number of cards in the deck
     */
    public int size() {
        return cards.size();
    }

    /**
     * Randomly shuffles the order of cards in the deck.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Draws and removes the top card from the deck.
     * The top of the deck is considered the end of the list.
     *
     * @return the drawn {@code Card}
     * @throws NoSuchElementException if the deck is empty
     */
    public Card drawOrThrow() {
        if (cards.isEmpty()) {
            throw new NoSuchElementException("No more cards in deck");
        }
        return cards.remove(cards.size() - 1);
    }

    /**
     * Adds a card to the bottom of the deck.
     * The bottom of the deck is considered the beginning of the list.
     *
     * @param card the {@code Card} to be added to the bottom
     */
    public void addBottom(Card card) {
        cards.add(0, card);
    }
}
