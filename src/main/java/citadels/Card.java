package citadels;

/**
 * Represents a district card in the Citadels game.
 * Each card has a name, cost, color, and optional description.
 */
public class Card {
    private final String Name;
    private final int cost;
    private final String colour;
    private final String description;

    /**
     * Constructs a new Card with the specified properties.
     *
     * @param Name the name of the card
     * @param cost the cost required to build the district
     * @param colour the color of the card (e.g. red, blue, green, yellow, purple)
     * @param description a short description of the card's special ability or information
     */
    public Card(String Name, int cost, String colour, String description){
        this.Name = Name;
        this.cost = cost;
        this.colour = colour;
        this.description = description;
    }

    /**
     * Returns the name of the card.
     *
     * @return the card name
     */
    public String getName(){
        return Name;
    }

    /**
     * Returns the cost of the card.
     *
     * @return the cost in gold
     */
    public int getCost(){
        return cost;
    }

    /**
     * Returns the color of the card.
     *
     * @return the color as a string
     */
    public String getColour(){
        return colour;
    }

    /**
     * Returns the description of the card.
     *
     * @return a string describing any special ability or rule
     */
    public String getDescription(){
        return description;
    }

    /**
     * Returns the string representation of the card, which is its name.
     *
     * @return the name of the card
     */
    @Override
    public String toString(){
        return Name;
    }
}
