package citadels;

import java.util.*;
/**
 * Represents the core game logic for Citadels, managing players, roles, and game state.
 */
public class Game{
    /** The list of players participating in the game. */
    private final List<Player> players;

    /** Index of the current player's turn in the players list. */
    private int currentIndex = 0;

    /** Roles that are still available for selection this round. */
    private List<Role> availableRoles;

    /** Mapping of players to the roles they have chosen. */
    private Map<Player, Role> chosenRoles = new HashMap<>();

    /** The player who holds the King role from the previous round. */
    private Player kingPlayer;
      /** The player who is currently active in the game logic. */
    private Player currentPlayer;

    /**
     * Constructs a new Game with the specified number of players.
     * @param numberOfPlayers the number of players to create in this game
     */
    public Game(int numberOfPlayers){
        this.players = new ArrayList<>();
        
        for(int i = 1; i <= numberOfPlayers; i++){
            Player player = new Player(i);
            players.add(player);
        }
        this.kingPlayer = players.get(0);
        initRoles();
    }

    /**
     * Initializes and shuffles the list of available roles for the round.
     */
    private void initRoles(){
        Role[] rolesArray = Role.values();
        availableRoles = new ArrayList<>();
        for(Role role: rolesArray){
            availableRoles.add(role);
        }
        Collections.shuffle(availableRoles);
    }

    /**
     * Retrieves the player whose turn it currently is.
     * @return the current Player
     */
    public Player getCurrentPlayer(){
        return players.get(currentIndex);
    }

     /**
     * Sets the internal reference to the current player.
     * @param player the Player to set as current
     */
    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    /**
     * Advances to and returns the next player in turn order.
     * Wraps around to the first player when reaching the end of the list.
     * @return the next Player in turn order
     */
    public Player getNextPlayer(){
        if (currentIndex < players.size() - 1) {
            currentIndex++;
        }else{
            currentIndex = 0;
        }
        return getCurrentPlayer();
    }

    /**
     * Retrieves a copy of roles currently available for selection.
     * @return a List of available Role values
     */
    public List<Role> getAvailableRoles(){
        List<Role> roleCopy = new ArrayList<>();
        for(Role role: availableRoles){
            roleCopy.add(role);
        }
        return roleCopy;
    }

    /**
     * Replaces the current set of available roles with a new list.
     * @param roles the List of Role values to set as available
     */
    public void setAvailableRoles(List<Role> roles) {
        this.availableRoles = new ArrayList<>(roles); 
    }


    /**
     * Allows the current player to choose a role by name if it is available.
     * @param roleName the name of the role to choose (case-insensitive)
     * @return true if the role was successfully chosen; false otherwise
     */
    public boolean choosenRole(String roleName){
        for(Role role: availableRoles){
            if(role.name().equalsIgnoreCase(roleName)){
                chosenRoles.put(getCurrentPlayer(), role);
                availableRoles.remove(role);
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the role assigned to the specified player for the current round.
     * @param player the Player whose chosen role is requested
     * @return the Role assigned to that player, or null if none selected
     */
    public Role getRoleFor(Player player){
        return chosenRoles.get(player);
    }

    /**
     * Returns a new List of all players in the game.
     * @return a copy of the players list
     */
    public List<Player> getPlayers(){
        return new ArrayList<>(players);
    }

    /**
     * Explicitly sets the internal currentIndex to a provided value.
     * @param i the index to assign as the current turn index
     */
    public void setCurrentIndex(int i) {
        this.currentIndex = i;
    }

    /**
     * Resets the roles for a new round, clearing chosen roles and reinitializing available roles.
     */
    public void resetRoles() {
        initRoles();
        chosenRoles.clear();
    }
    
    /**
     * Retrieves another player by their unique ID.
     * @param id the integer ID of the player to find
     * @return the Player with the matching ID, or null if not found
     */
    public Player getOtherPlayer(int id) {
        for (Player player : players) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    /**
     * Sets the player who holds the King role at the start of a new turn phase.
     * @param player the Player to assign as king
     */
    public void setKing(Player player) {
        this.kingPlayer = player;
    }

    /**
     * Retrieves the current King player.
     * @return the Player assigned as king
     */
    public Player getKingPlayer() {
        return kingPlayer;
    }

    /**
     * Performs a deep copy of another Game instance's state into this one.
     * Copies players, current index, king, available roles, and chosen roles.
     * @param other the Game instance to copy from
     */
    public void gameCopy(Game other) {
        if (this.players == null) return;
        this.players.clear();
        for (Player p : other.getPlayers()) {
            this.players.add(new Player(p));
        }

        this.currentIndex = other.currentIndex;
        this.kingPlayer = other.kingPlayer;

        if (this.availableRoles == null) this.availableRoles = new ArrayList<>();
        else this.availableRoles.clear();
        this.availableRoles.addAll(other.getAvailableRoles());

        if (this.chosenRoles == null) this.chosenRoles = new HashMap<>();
        else this.chosenRoles.clear();
        this.chosenRoles.putAll(other.chosenRoles);
    }


    /**
     * Determines if the game has ended based on any player having 8 or more built district cards.
     * @return true if the game is over; false otherwise
     */
    public boolean gameOver() {
        for (Player p : players) {
            if (p.getBuildDistricCards().size() >= 8) {
                return true;
            }
        }
        return false;
    }

}