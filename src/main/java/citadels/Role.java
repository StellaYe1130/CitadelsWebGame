package citadels;

/**
 * Enum representing the various roles a player can assume in the Citadels game.
 * Each role has a turn order, name, and special ability.
 */
public enum Role {
    /**
     * Assassin: Eliminates a character, causing that player to skip their turn.
     */
    Assassin(1, "Assassin", "Select another character whom you wish to kill. The killed character loses their turn."),

    /**
     * Thief: Steals all gold from a character when they take their turn.
     */
    Thief(2, "Thief", "Select another character whom you wish to rob. When a player reveals that character to take his turn, you immediately take all of his gold. You cannot rob the Assassin or the killed character."),

    /**
     * Magician: Swaps hand with another player or discards and redraws cards.
     */
    Magician(3, "Magician", "Can either exchange their hand with another player's, or discard any number of district cards face down to the bottom of the deck and draw an equal number of cards from the district deck (can only do this once per turn)."),

    /**
     * King: Gains bonus from yellow districts and crowns the next round.
     */
    King(4, "King", "Gains one gold for each yellow (noble) district in their city. They receive the crown token and will be the first to choose characters on the next round."),

    /**
     * Bishop: Gains gold from blue districts and protection from the Warlord.
     */
    Bishop(5, "Bishop", "Gains one gold for each blue (religious) district in their city. Their buildings cannot be destroyed by the Warlord, unless they are killed by the Assassin."),

    /**
     * Merchant: Gains gold from green districts and an extra gold.
     */
    Merchant(6, "Mechant", "Gains one gold for each green (trade) district in their city. Gains one extra gold."),

    /**
     * Architect: Draws 2 extra cards and can build up to 3 districts in a turn.
     */
    Architect(7, "Architect", "Gains two extra district cards. Can build up to 3 districts per turn."),

    /**
     * Warlord: Gains gold from red districts and can destroy buildings.
     */
    Warlord(8, "Warlord", "Gains one gold for each red (military) district in their city.");

    private final int order;
    private final String theName;
    private final String specialAbility;

    /**
     * Constructs a Role enum value with the specified order, name, and ability.
     *
     * @param order           the turn order of the role
     * @param theName         the display name of the role
     * @param specialAbility  the description of the role's power
     */
    Role(int order, String theName, String specialAbility){
        this.order = order;
        this.theName = theName;
        this.specialAbility = specialAbility;
    }

    /**
     * Returns the order in which this role takes its turn.
     *
     * @return the role's order
     */
    public int getOrder(){
        return order;
    }

    /**
     * Returns the display name of this role.
     *
     * @return the role's name
     */
    public String getTheName(){
        return theName;
    }

    /**
     * Returns the special ability description for this role.
     *
     * @return the special ability text
     */
    public String getSpecialAbility(){
        return specialAbility;
    }

    /**
     * Returns a string representation of the role, including name, order, and ability.
     *
     * @return the formatted role description
     */
    @Override
    public String toString() {
        return theName + " (order " + order + "): " + specialAbility;
    }
}
