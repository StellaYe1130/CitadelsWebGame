package citadels;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.io.*;


import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

/**
 * Main application class for running the Citadels game.
 * Handles game setup, user input, command processing, and game phases.
 */
public class App {
	/** File containing card definitions (cards.tsv). */
    private File cardsFile;
    /** Core game logic instance. */
    private Game game;
    /** Deck of cards used in the game. */
    private Deck deck;


    public void setDeck(Deck deck){
        this.deck = deck;
    }

    /** Index of the next player to select a role. */
    private int selectionPlayerIndex = 0;

    /** Roles available during the selection phase. */
    private List<Role> selectionRoles;

     /** Current phase of the game: character selection or turn phase. */
    public enum Phase{
        character_selection,
        turn_phase
    }
    public Phase phase = Phase.character_selection;

    /** Scanner for reading user input. */
    private Scanner scanner = new Scanner(System.in);

    public void setScanner(Scanner scanner){
        this.scanner = scanner;
    }
    
    /** Index within turnOrder for the active turn. */
    private int turnIndex = 0;

    /** Order of roles during the turn phase. */
    private List<Role> turnOrder;

    /** Mapping from Role to the Player who selected it. */
    private Map<Role, Player> roleToPlayer;

     /** Roles that have been assassinated this round. */
    private Set<Role> assasssinatedRoles = new HashSet<>();

     /** The currently active role for processing commands. */
    private Role activeRole;
    public void setActiveRole(Role role) {
        this.activeRole = role;
    }

    public List<Player> getPlayers() {
        return game.getPlayers();
    }

     /** Flag for printing debug information. */
    public boolean debug = false;

    /** Gson instance for JSON (save/load) with pretty printing. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

     /** Roles that have hospital protection (assassinated but still act). */
    private Set<Role> hospital = new HashSet<>();

    public Player getPlayerByRole(Role role) {
        return roleToPlayer.get(role);
    }

     /** Current role pointer used in various methods. */
    private Role currentRole;
    
    public void setCurrentRole(Role role) {
        this.currentRole = role;
    }

    public Player getCurrentPlayer() {
        return game.getCurrentPlayer(); 
    }

    public String Colour(Card card, Player player){
        if(card.getName().equalsIgnoreCase("school of magic") && player.getSchoolOfMagicColour() != null){
            return player.getSchoolOfMagicColour();
        }
        if(card.getName().equalsIgnoreCase("haunted city") && player.getHauntedCity()!= null){
            return player.getHauntedCity();
        }
        return card.getColour().toLowerCase();
    }

    public void gameCopy(App app) {
        Game copiedGame = app.getGame();
        if (copiedGame == null || copiedGame.getPlayers() == null) {
         throw new IllegalStateException("Source app's game or players is null. Cannot copy.");
        }

        this.game = new Game(copiedGame.getPlayers().size());
        this.game.gameCopy(copiedGame);
        this.cardsFile = app.cardsFile;
        this.deck = app.deck;
        this.phase = app.phase;

        this.activeRole = app.activeRole;
        this.selectionPlayerIndex = app.selectionPlayerIndex;
        this.selectionRoles = app.selectionRoles == null ? null : new ArrayList<>(app.selectionRoles);

        this.turnOrder = app.turnOrder == null ? null : new ArrayList<>(app.turnOrder);
        this.turnIndex = app.turnIndex;

        this.debug = app.debug;
    }

     /** Indicates if Bell Tower's end condition is active. */
    public boolean bellTower = false;

    /**
     * Default constructor: starts a 4-player game using System.in.
     */
    public App() {
        this(4);
    }

    /**
     * Constructs a new App with specified number of players.
     * Uses System.in for input.
     * @param numberOfPlayers number of players (4-7)
     */
    public App(int numberOfPlayers) {
        this(numberOfPlayers, System.in);
    }

    /**
     * Constructs a new App with specified number of players and input stream.
     * Initializes game, deck, deals cards, and starts role selection.
     * @param numberOfPlayers number of players (4-7)
     * @param in InputStream for user commands
     */

	public App(int numberOfPlayers, InputStream in) {
		try {
            cardsFile = new File(URLDecoder.decode(this.getClass().getResource("cards.tsv").getPath(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Shuffling deck...");
        this.game = new Game(numberOfPlayers);
        this.scanner = new Scanner(in);
        this.deck = parseCards();
        this.deck.shuffle();
        this.roleToPlayer = new LinkedHashMap<>();
        System.out.println("Dealing cards...");

        for(Player player : game.getPlayers()){
            for(int i = 0; i < 4; i++){
                Card card = this.deck.drawOrThrow();
                player.drawCard(card);

            }
        }
        System.out.println("Welcome to Citadels");
        startSelectionPhase();
        
	}

    public Game getGame(){
        return this.game;
    }

    public void setGame(Game g) {
        this.game = g;
    }

    public List<Role> getSelectionRoles(){
        return new ArrayList<>(selectionRoles);
    }


    /**
     * Reads cards.tsv resource, parses each line, and constructs a Deck.
     * @return Deck populated with cards from cards.tsv
     */
    public Deck parseCards(){
        Deck deck = new Deck();
        InputStream in = getClass().getResourceAsStream("/citadels/cards.tsv");

        try(Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name())){
            //skip the head of cards.tsv
            if(scanner.hasNextLine()){
               scanner.nextLine();
            }
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] parts = line.split("\t");
                String Name = parts[0];
                String colour = parts[2];
                int cost = Integer.parseInt(parts[3]);
                String description = "";
                if(parts.length >= 5){
                    description = parts[4];
                }
                deck.addCard(new Card(Name, cost, colour, description));
            }
        }catch(Exception e){
            throw new RuntimeException("Error parsing cards.tsv: " + e.getMessage());
        }
        return deck;
    }

    /**
     * Main game loop: reads user input, processes commands, and advances phases.
     */
    public void run(){
        
        while(true){
            System.out.print("> ");
            String line;
            try{
                line = scanner.nextLine().trim();
            }catch(NoSuchElementException e){
                break;
            }
            if(line.equalsIgnoreCase("exit")){
                System.out.println("Player Exit");
                break;
            }
            if(line.equalsIgnoreCase("t")){
                handleNext();
                continue;
            }

            if(phase == Phase.character_selection){
                if(line.equalsIgnoreCase("handcards") || line.equalsIgnoreCase("money") || line.equalsIgnoreCase("help")){
                   System.out.println(line.toLowerCase());
                   System.out.println(processCommand(line));
                   continue;
               }
                Role chosenRole = null;
                for(Role role: selectionRoles){
                    if(role.name().equalsIgnoreCase(line)){
                        chosenRole = role;
                        break;
                    }
                }
                if(chosenRole != null){
                   String res = processCommand("select " + line);
                   System.out.println(res);
                   if(selectionPlayerIndex >= game.getPlayers().size()){
                       System.out.println("All players have chosen. Starting turn phase.");
                       phase = Phase.turn_phase;
                       initTurnOrder();
                   } else {
                        Player next = game.getPlayers().get(selectionPlayerIndex);
                        printSelectionPrompt(next);
                   }
                }else{
                    System.out.println("All players have chosen. Starting turn phase.");
                    phase = Phase.turn_phase;
                    initTurnOrder();
                }
                continue;
            }
            System.out.println(processCommand(line));
        }
    }


    /** Initializes the role selection phase, shuffles roles, and prints first prompt. */
    public void startSelectionPhase(){
        phase = Phase.character_selection;
        assasssinatedRoles.clear();
        roleToPlayer.clear();
        hospital.clear();
        selectionRoles = new ArrayList<>();
        for(Role role: Role.values()){
            selectionRoles.add(role);
        }
        Collections.shuffle(selectionRoles);
        Role hidden = selectionRoles.remove(0);
        System.out.println("A mystery character was removed.");

        int numberPlayers = game.getPlayers().size();
        int faceUpCount;
        if(numberPlayers == 4){
            faceUpCount = 2;
        }else if(numberPlayers == 5){
            faceUpCount = 1;
        }else{
            faceUpCount = 0;
        }
        for(int i = 0 ; i < faceUpCount; i++){
            Role faceUp = selectionRoles.remove(0);
            System.out.println(faceUp.name() + " was removed.");
        }

        Player first = game.getPlayers().get(selectionPlayerIndex);
        printSelectionPrompt(first);
    }

    /** Prints prompt showing which roles are available to a player. */
    public void printSelectionPrompt(Player p){
        List<String> names = new ArrayList<>();
        for(Role role: selectionRoles){
            names.add(role.name());
        }
        String options = String.join(", ", names);
        System.out.println("Player " + p.getId() + ", choose a charcter from: " + options);
    }

    /** Initializes turn order based on chosen roles and their order property. */
    public void initTurnOrder(){
        turnOrder = new ArrayList<Role>();
        for(Role role: roleToPlayer.keySet()){
            turnOrder.add(role);
        }
        Collections.sort(turnOrder, new Comparator<Role>() {
            @Override
            public int compare(Role role1, Role role2){
                return role1.getOrder() - role2.getOrder();
            }
        });
        turnIndex = 0;
    }

    /**
     * Advances the game state to the next player or phase.
     * If in character selection phase: Prompts the next player to choose a role. Once all players have chosen, initializes turn order and switches to turn phase.
     * If in turn phase: When all roles have acted, announces the crowned player and resets to character selection. Handles assassination:If a role is assassinated but protected by Hospital, allows the player to act with restrictions.Otherwise, skips and logs the assassination.
     * Sets the current player based on the next role in turn order.Resets that player’s one-time purple card flags (laboratory, smithy, armory).</li>
     * Calls {@link #processTurn()} to perform the turn actions and {@link #end(Player)} for end-of-turn effects.
     * @see #startSelectionPhase()
     * @see #processTurn()
     * @see #end(Player)
     */
    public void handleNext(){
        if(phase == Phase.character_selection){
            if(selectionPlayerIndex < game.getPlayers().size()){
                Player next = game.getPlayers().get(selectionPlayerIndex);
                printSelectionPrompt(next);
            }else{
                initTurnOrder();
                phase = Phase.turn_phase;
                turnIndex = 0;
                handleNext();
            }
            return;
        }

        if (turnIndex >= turnOrder.size()) {
            Player first = game.getKingPlayer();
            if (first != null) {
                System.out.println(first.getId() + " is the crowned player and goes first.");
                selectionPlayerIndex = game.getPlayers().indexOf(first);
            } else {
                selectionPlayerIndex = 0;
            }
            phase = Phase.character_selection;
            startSelectionPhase();
            return;
        }
        
        Role next = turnOrder.get(turnIndex);
        Player nextPlayer = roleToPlayer.get(next);
        
        boolean hasHospital = false;
        for(Card build: nextPlayer.getBuildDistricCards()){
            if("hospital".equalsIgnoreCase(build.getName())){
                hasHospital = true;
                break;
            }
        }
        if(assasssinatedRoles.contains(next) && hasHospital && !hospital.contains(next)){
            System.out.println("Hospital: you are assassinated, you may take an action during your turn (but you may not build a district card or use your character's power)");
            hospital.add(next);
        }else{
            while(turnIndex < turnOrder.size() && assasssinatedRoles.contains(turnOrder.get(turnIndex))){
                    Role dead = turnOrder.get(turnIndex);
                    System.out.println();
                    System.out.println("==========================");
                    System.out.println(dead.name() + " was assassinated. SKIP.");
                    turnIndex++;
            }
        }

        if(turnIndex >= turnOrder.size()){
            Player first = game.getKingPlayer();
            if (first != null) {
                System.out.println(first.getId() + " is the crowned player and goes first.");
            }
            if(first != null){
                selectionPlayerIndex = game.getPlayers().indexOf(first);
            }else{
                selectionPlayerIndex = 0;
            }
            phase = Phase.character_selection;
            startSelectionPhase();
            
           return;
        }
        
        Role currentRole = turnOrder.get(turnIndex);
            
        Player currentPlayer = roleToPlayer.get(currentRole);
        int index = game.getPlayers().indexOf(currentPlayer);
        game.setCurrentIndex(index);
        currentPlayer.laboratory = false;
        currentPlayer.smithy = false;
        currentPlayer.armory = false;
        processTurn();
        end(currentPlayer);
        turnIndex++;
        
    }

    /**
     * Executes a full turn for the active role, covering:
     * Reset to selection phase if turnIndex exceeds role list
     * Assignment of activeRole and updating current player index
     * King and Bishop district-based income bonuses
     * Prompting for 'action income' or 'action draw', including library effects
     * Architect ability: draw two extra cards
     * Magician ability: handle 'swap' and 'redraw' subcommands with validation
     * Building phase: enforce build limits and process 'build' commands
     * Merchant and Warlord end-of-turn bonuses for green and red districts
     * Advance to next role or reset selection phase at round end
     * @see #processCommand(String)
     * @see #handleNext()
     */
    
    public void processTurn(){
        // Determine role and player
        Role currentRole;
        Player currentPlayer;
        if(turnOrder != null && turnIndex < turnOrder.size()){
            currentRole = turnOrder.get(turnIndex);
            this.activeRole = currentRole;
            currentPlayer = roleToPlayer.get(currentRole);
            if(currentPlayer != null){
                int index = game.getPlayers().indexOf(currentPlayer);
                game.setCurrentIndex(index);
            } else {
                currentPlayer = game.getCurrentPlayer();
            }
        } else if(turnOrder != null && turnIndex >= turnOrder.size()){
            phase = Phase.character_selection;
            startSelectionPhase();
            return;
        } else {
            // Direct call without turnOrder (e.g. from tests): use field-set role
            currentRole = this.currentRole != null ? this.currentRole : this.activeRole;
            this.activeRole = currentRole;
            currentPlayer = game.getCurrentPlayer();
        }

        if(currentRole == Role.King){
            int count = 0;
            for(Card card: currentPlayer.getBuildDistricCards()){
                String colour = Colour(card, currentPlayer);
                if(colour.equals("yellow")){
                    count++;
                }
            }
            currentPlayer.addMoney(count);
            System.out.println("King bonus: +" + count + " gold.");

            game.setKing(currentPlayer);
            System.out.println("Player " + currentPlayer.getId() + " is the king.");

            for(Player player: game.getPlayers()){
                for(Card built : player.getBuildDistricCards()){
                    if("throne room".equalsIgnoreCase(built.getName())){
                        player.addMoney(1);
                        System.out.println("Throne Room: Player " + player.getId() + " gains 1 gold as Crown switched.");
                        break;
                    }
                }
            }
        }

        if(currentRole == Role.Bishop){
            int count = 0;
            for(Card card: currentPlayer.getBuildDistricCards()){
                if(Colour(card, currentPlayer).equalsIgnoreCase("blue")){
                    count++;
                }
            }
            currentPlayer.addMoney(count);
            System.out.println("Bishop bonus: +" + count + " gold for building blue district.");
        }
        
        if(currentRole != null){
            System.out.println();
            System.out.println("===" + currentRole.getTheName() + "'s turn ===");
        }
        System.out.println("Player " + currentPlayer.getId() + "'s turn");

        // Action phase: only run when called via handleNext (turnOrder is set)
        if(turnOrder != null){
            System.out.println("Please enter 'action income' or 'action draw': ");
            while(true){
                System.out.print("> ");
                String choice;
                try{
                    choice = scanner.nextLine().trim().toLowerCase();
                }catch(NoSuchElementException e){
                    break;
                }
                if(choice.equals("exit")){
                    System.out.println("Player Exit");
                    System.exit(0);
                }
                if(choice.equals("action income") || choice.equals("action draw")){
                    System.out.println(processCommand(choice));
                    break;
                }else if(choice.equals("handcards") || choice.equals("money")){
                    System.out.println(processCommand(choice));
                }else{
                    System.out.println("Invaild input, please enter 'action income' or 'action draw");
                }
            }
            if(currentRole == Role.Architect){
                System.out.println("Architect ability: drawing 2 cards...");
                for(int i = 0; i < 2; i++){
                    Card extra = deck.drawOrThrow();
                    currentPlayer.drawCard(extra);
                    System.out.println(" Draw: " + extra.getName());
                }
            }
        }

        if(turnOrder != null && currentRole == Role.Magician){
            System.out.println("Magician ability: 'action swap <playerID>' action redraw <i1,i2,...>' or 'skip'");
            while(true){
                System.out.print("> ");
                String line;
                try{
                    line = scanner.nextLine().trim();
                }catch(NoSuchElementException e){
                    break;
                }
                 if(line.equalsIgnoreCase("skip")){
                    break;
                }
                String[] args = line.split("\\s+", 3);

                if(args.length < 3 || !args[0].equalsIgnoreCase("action")){
                    System.out.println("Invalid input.");
                    continue;
                }
                
                String subcommand = args[1];
                String target = args[2];
                Player magician = game.getCurrentPlayer();
               
                if(subcommand.equalsIgnoreCase("swap")){
                    try{
                        int targetId = Integer.parseInt(target);
                        Player other = game.getOtherPlayer(targetId);
                        if(other == null){
                            System.out.println("No such player: " + args[1]);
                        }else{
                            List<Card> magicianCards = new ArrayList<>(magician.getHandCards());
                            List<Card> otherCards = new ArrayList<>(other.getHandCards());
                            magician.setHandCards(otherCards);
                            other.setHandCards(magicianCards);
                            System.out.println("Swap hand cards with Player " + targetId);
                            break;
                        }
                    }catch(NumberFormatException e){
                        System.out.println("INvalid player id: " + target);
                    }
                }else if(subcommand.equalsIgnoreCase("redraw")){
                    String[] position = target.split(",");
                    List<Card> magicianHandCards = magician.getHandCards();
                    List<Integer> redraw = new ArrayList<>();
                    boolean error = false;
                    for(String s: position){
                        try{
                            int i = Integer.parseInt(s.trim()) - 1;
                            if(i < 0 || i >= magicianHandCards.size()){
                                System.out.println("Invalid index: " + s);
                                error = true;
                                break;
                            }
                            redraw.add(i);
                        }catch(NumberFormatException e){
                            System.out.println("Invalid number: " + s);
                            error = true;
                            break;
                        }
                    }
                    if(!error && !redraw.isEmpty()){
                        Collections.sort(redraw, Collections.reverseOrder());
                        List<Card> oldCards = new ArrayList<>();
                        for(int idx: redraw){
                            oldCards.add(magicianHandCards.get(idx));
                        }

                       for(Card old: oldCards){
                        deck.addBottom(old);
                       }

                       List<Card> newcCards = new ArrayList<>();
                       for(int i = 0; i < redraw.size(); i++){
                        newcCards.add(deck.drawOrThrow());
                       }

                       for(int j = 0; j < redraw.size(); j++){
                            int idx = redraw.get(j);
                            magicianHandCards.remove(idx);
                            Card newCard = newcCards.get(j);
                            magicianHandCards.add(idx, newCard);
                            System.out.println("Draw: " + newCard.getName());
                       }
                        System.out.println("Redraw: " + redraw.size() + " cards.");
                        break;
                    }
                }else{
                    System.out.println("Invalid action");
                }
            }
        }

        System.out.println("Please enter: ");
        System.out.println("build <n>");
        System.out.println("action <special>");
        System.out.println("end");

        int buildCount = 0;
        int maxBuilds;
        if(currentRole == Role.Architect){
            maxBuilds = 3;
        }else{
            maxBuilds = 1;
        }
        
        
        while(true){
            System.out.print("> ");
            String command;
            try{
                command = scanner.nextLine().trim();
            }catch(NoSuchElementException e){
                System.out.println("The turn ends");
                break;
            }
            if(command.equalsIgnoreCase("exit")){
                System.out.println("Player Exit");
                System.exit(0);
            }
            if(command.equalsIgnoreCase("end")){
                System.out.println("The turn ends");
                break;
            }
            String[] parts = command.split("\\s+");
            if(parts[0].equalsIgnoreCase("build")){
                if(buildCount >= maxBuilds){
                    System.out.println("You have used all your build this turn.");
                    continue;
                }
                String result = processCommand(command);
                System.out.println(result);
                if(result.startsWith("Built: ")){
                    buildCount++;
                }
                continue;
            }
            System.out.println(processCommand(command));
        }
        if(currentRole == Role.Merchant){
            int count = 0;
            for(Card card: currentPlayer.getBuildDistricCards()){
                if(Colour(card, currentPlayer).equalsIgnoreCase("green")){
                    count++;
                }
            }
            currentPlayer.addMoney(1);
            System.out.println("Merchant bonus: +1 gold.");
        }
    }

    /**
     * Parses and executes a player command, handling all supported cases:
     * handcards / hand: lists the current player’s hand cards
     * money / gold: displays the current player’s gold amount
     * build: attempts to build the nth card in hand, enforcing
     *       duplicate, quarry, and factory rules
     * info: shows details for a built district or purple card
     * role: lists roles still available for selection
     * select: allows the player to pick a role during the selection phase
     * action income / action draw: grants gold or draws cards, with library special rules if owned
     * destroy / assassinate / steal: executes Warlord, Assassin, and Thief actions
     * special kill|steal: internal handling of special role interactions
     * help: prints the list of all commands
     * all: displays a summary of each player’s state
     * city / list / citadels: lists built districts for the given player
     * debug: toggles debug mode (reveals other players’ hands)
     * save load : persists or restores game state to/from JSON
     * use: activates the ability of a built purple district
     * @param line the raw input command
     * @return a message indicating the result or an error description
     */

    public String processCommand(String line){
        if (line != null && line.trim().equalsIgnoreCase("t")) {
            return "Please enter 'action income' or 'action draw': ";
        }
        String[] parts = line.split("\\s+");
        String command = parts[0].toLowerCase();
        switch(command){
            case "handcards":
            case "hand":{
                List<Card> handCards = game.getCurrentPlayer().getHandCards();
                StringBuilder sb = new StringBuilder();
                sb.append("Handcards: ");
                
                for (int i = 0; i < handCards.size(); i++) {
                    if (i > 0) sb.append(", ");
                        sb.append(handCards.get(i).getName());
                }
                return sb.toString();
            }

            case "money":
            case "gold": {
                int money = game.getCurrentPlayer().getMoney();
                StringBuilder sb = new StringBuilder();
                sb.append("Money: ");
                sb.append(money);
                return sb.toString();
            }    

            case "build":
                Role currentRole = activeRole;
                try{
                    int cardIndex = Integer.parseInt(parts[1]) - 1;
                    Player player = game.getCurrentPlayer();
                    List<Card> hand = player.getHandCards();
                    if(cardIndex < 0 || cardIndex >= hand.size()){
                        return "Invalid handIndex: " + parts[1];
                    }
                    Card c = hand.get(cardIndex);
                    List<Card> builtList = player.getBuildDistricCards();
                    
                    int exist = 0;
                    for(Card buildCard: builtList){
                        if(buildCard.getName().equals(c.getName())){
                            exist++;
                        }
                    }
                    if(exist > 0){
                        boolean quarry = false;
                        for(Card built : builtList){
                            if("quarry".equalsIgnoreCase(built.getName())){
                                quarry = true;
                                break;
                            }
                        }
                        if(!quarry){
                            return "Cannot build duplicate district: " + c.getName();
                        }

                        if(exist >= 2){
                            return "Cannot build more than one duplicate of: " + c.getName();
                        }
                        System.out.println("Quarry: you may only have one such duplicate district in your city at any one time");
                    }
                    int original = c.getCost();
                    int effective = original;
                    if(!c.getName().equalsIgnoreCase("factory") && c.getColour().equalsIgnoreCase("purple")){
                        boolean factory = false;
                        for(Card built : player.getBuildDistricCards()){
                            if("factory".equalsIgnoreCase(built.getName())){
                                factory = true;
                                break;
                            }
                        }
                        if(factory){
                            effective = Math.max(0, original - 1);
                            System.out.println("\"Factory works: " + original + " turns to " + effective);
                        }
                    }
                    
                    if(!player.spendMoney(c.getCost())){
                        return "Insufficient money to build the district: " + c.getName();
                    }
                    player.buildDistricCards(c);
                    
                    if(c.getColour().equalsIgnoreCase("purple")){
                        purpleCardsOne(player, c);
                    }
                    checkOver();
                    return "Built: " + c.getName();
        
                }catch(NumberFormatException e){
                    return "Invalid number: " + parts[1];
                }

            case "info":
                if(parts.length < 2){
                    return "Invalid built position: ";
                }
                Player gamer = game.getCurrentPlayer();
                String arg = parts[1];

                try{
                    int cardPosition = Integer.parseInt(parts[1]) - 1;
                    Player player = game.getCurrentPlayer();
                    List<Card> built = player.getBuildDistricCards();
                    if(cardPosition < 0 || cardPosition >= built.size()){
                        return "Invalid built position: " + parts[1];
                    }
                    Card c = built.get(cardPosition);
                    return String.format(
                    "Card[%d]: %s (cost = %d, colour = %s) - %s",
                    cardPosition + 1, c.getName(), c.getCost(), c.getColour(), c.getDescription());
                }catch(NumberFormatException e){
                    for(Card c : gamer.getHandCards()){
                        if(c.getName().equalsIgnoreCase(arg) && c.getColour().equalsIgnoreCase("purple")){
                            return String.format("%s [ purple%d ] - %s", c.getName(),c.getCost(),c.getDescription());
                        }
                    }
                    return "Invalid built position: " + parts[1];
                }

            case "role":
                List<Role> available = game.getAvailableRoles();
                StringBuilder names = new StringBuilder();
                for(int i = 0; i < available.size(); i++){
                    if(i > 0){
                        names.append(", ");
                    }
                    names.append(available.get(i).name());
                }
                return "Available roles: " + names.toString();
                

            case "select":
                String choice = parts[1];
                Role chosenRole = null;
                for(Role role: Role.values()){
                    if(role.name().equalsIgnoreCase(choice)){
                        chosenRole = role;
                        break;
                    }
                }
                if(chosenRole == null){
                    return "Unknown role: " + choice;
                }

                if(! selectionRoles.contains(chosenRole)){
                    return "The role has already been chosen " + chosenRole.name();
                }
                Player p = game.getPlayers().get(selectionPlayerIndex);
                roleToPlayer.put(chosenRole, p);
                selectionRoles.remove(chosenRole);

                String result = "Chosen " + choice;
                selectionPlayerIndex++;
                if(selectionPlayerIndex < game.getPlayers().size()){
                    Player next = game.getPlayers().get(selectionPlayerIndex);
                    result += " . Next player: " + next.getId();
                        
                }
                return result;
                
            case "action":
                
                String action = parts[1].toLowerCase();
                Player current = game.getCurrentPlayer();
                switch (action) {
                    case "income":
                        current.addMoney(2);
                        return "Income: add 2 gold";

                    case "draw":
                        Player currentPlayer = game.getCurrentPlayer();
                        boolean library = false;
                        for(Card built: current.getBuildDistricCards()){
                            if("library".equalsIgnoreCase(built.getName())){
                                library = true;
                                break;
                            }
                        }

                        if(library){
                            System.out.println(":obrary: If you choose to draw cards you you take an action, you keep both of the cards you have drawn.");
                            Card one = deck.drawOrThrow();
                            Card two = deck.drawOrThrow();
                            current.drawCard(one);
                            current.drawCard(two);
                            System.out.println("Drawn: 1/ " + one.getName() + ", Drawn: 2/ " + two.getName());
                            return "Library: you can keep both cards";
                        }else{
                            List<Card> choose = new ArrayList<>();
                            Card first  = deck.drawOrThrow();
                            Card second = deck.drawOrThrow();
                            choose.add(first);
                            choose.add(second);

                            System.out.println("Draw: you drew two cards, choose one to keep:");
                            System.out.println("  1: " + first.getName());
                            System.out.println("  2: " + second.getName());
                            System.out.print("Choose one to keep (1 or 2): ");

                            int option = 1;
                            try {
                                option = Integer.parseInt(scanner.nextLine().trim());
                            } catch (Exception e) {

                            }
                            if (option != 2) {
                                option = 1;
                            }

                            Card keep = choose.get(option - 1);
                            Card drop = choose.get(2 - option);
                            current.drawCard(keep);
                            deck.addBottom(drop);

                            System.out.println("Draw card: " + keep.getName());
                            return "Draw card: " + keep.getName();
                        }
                        
                    case "steal":
                        return processCommand("special steal " + parts[2]);

                    case "assassinate":
                        return processCommand("special kill " + parts[2]);
                        
                    case "destroy":
                        if(parts.length != 4){
                            return "You should enter destroy <player's ID> <district Index>.";
                        }
                        if(activeRole != Role.Warlord){
                            return "Only Warlord can destroy district";
                        }

                        try{
                            int targetId = Integer.parseInt(parts[2]);
                            int destroyIndex = Integer.parseInt(parts[3]) - 1;

                            Player target = game.getOtherPlayer(targetId);
                            Player warlord = game.getCurrentPlayer();

                            List<Card> districts = target.getBuildDistricCards();
                        
                            Card toDestroy = districts.get(destroyIndex);
                        
                            if(toDestroy.getName().equalsIgnoreCase("keep")){
                                return "The Keep cannot be destroyed by the Warlord";
                            }

                            int cost = toDestroy.getCost() - 1;

                            boolean greatWall = false;
                            for(Card built : target.getBuildDistricCards()){
                                if(built.getName().equalsIgnoreCase("great wall")){
                                    greatWall = true;
                                    break;
                                }
                            }
                            if(greatWall){
                                cost += 1;
                                System.out.println("The cost for the Warlord to destory any of your other districts is increased by one gold. Now is turned to " + cost + "gold" );
                            }

                            if(warlord.getMoney() < cost){
                                return "Insufficient money to destroy the target";
                            }
                            warlord.spendMoney(cost);
                            districts.remove(destroyIndex);
                                
                            for(Card built : target.getBuildDistricCards()){
                                if(built.getName().equalsIgnoreCase("graveyard")){
                                    System.out.println("Player " + target.getId() +", Graveyard lets you pay 1 gold to take" + toDestroy.getName() + " into your hand (yes or no)? ");
                                    String answer = scanner.nextLine().trim().toLowerCase();
                                    if("yes".equals(answer) && target.getMoney() >= 1){
                                        target.spendMoney(1);
                                        target.drawCard(toDestroy);
                                        System.out.println("Graveyard: you paid 1 gold and took " + toDestroy.getName() + " into your hand.");
                                    }else{
                                        System.out.println("Fail");
                                    }
                                    break;
                                }
                            }

                            if("bell tower".equalsIgnoreCase(toDestroy.getName())){
                                bellTower = false;
                                System.out.println("Bell Tower was destroyed, the ability won't be activated");
                            }

                            return "Destroyed " + toDestroy.getName() + " from Player " + targetId + " 's district (cost" + cost + " money)";

                        }catch(NumberFormatException e){
                            return "Invalid ";
                        }
            
                        default:
                            return "Unknown action: " + action;
                    }

                case "steal":
                    if(parts.length < 3){
                        return "Invalid arguments";
                    }
                    return processCommand("special steal " + parts[2]);

                case "assassinate":
                    if(parts.length < 3){
                        return "Invalid arguments";
                    }
                    return processCommand("special kill " + parts[2]);

                case "destroy":
                    if(activeRole != Role.Warlord){
                        return "Only Warlord can destroy district";
                    }
                    return processCommand("action " + line);

                case "help":
                    StringBuilder sb = new StringBuilder();
                    sb.append("Available commands:\n");
                    sb.append("  info : show information about a character or building\n");
                    sb.append("  t : processes turns\n");
                    sb.append("  all : shows all current game info\n");
                    sb.append("  citadel/list/city : shows districts built by a player\n");
                    sb.append("  hand : show all cards in hand\n");
                    sb.append("  gold [p] : shows gold of a player\n");
                    sb.append("  build <n> : Builds a building into your city\n");
                    sb.append("  action : Gives info about your special action and how to perform it\n");
                    sb.append("  end : Ends your turn\n");
                    return sb.toString();

                case "special":
                    
                    String Action = parts[1].toLowerCase();
                    String target = parts[2];
                    Role targetRole = null;
                    for(Role role: Role.values()){
                        if(role.name().equalsIgnoreCase(target)){
                            targetRole = role;
                            break;
                        }
                    }
                    if(targetRole == null){
                        return "Unknown role: " + target;
                    }
                    if(Action.equals("kill")){
                        assasssinatedRoles.add(targetRole);
                        return targetRole.name() + " has been assassinated.";
                    }else if(Action.equals("steal")){
                        Player thief = game.getCurrentPlayer();
                        Player victim = roleToPlayer.get(targetRole);
                        if(targetRole == Role.Assassin || assasssinatedRoles.contains(targetRole)){
                            return "You cannot steal from " + targetRole.name();
                        }
                        if(victim == null){
                            return targetRole.name() + " is not this round.";
                        }
                        int amount = Math.min(2, victim.getMoney());
                        victim.spendMoney(amount);
                        thief.addMoney(amount);
                        return "Stole " + amount + " money from " + targetRole.name() + " .";
                    }else{
                        return "Unknow special action: " + Action;
                    }

                case "all":{
                    StringBuilder allResult = new StringBuilder();
                    for (Player player : game.getPlayers()) {
                        StringBuilder SB = new StringBuilder();
                        SB.append("Player ").append(player.getId());
                        if(player == game.getCurrentPlayer()){
                            SB.append(" (you) ");
                        }
                        SB.append(": ");

                        int hand = player.getHandCardsCopy().size();
                        SB.append("cards=").append(hand).append(" ");
                        int gold = player.getMoney();
                        SB.append("gold=").append(gold).append(" ");
                        List<Card> build = player.getBuildDistricCards();
                        List<String> name = new ArrayList<>();
                        for(Card card: build){
                            name.add(card.getName());
                        }
                        SB.append("build=").append(String.join(",", name)).append(" ");

                        for(Card card: build){
                            SB.append("[ ");
                            SB.append(card.getColour().toLowerCase());
                            SB.append(card.getCost());
                            SB.append(" ]");
                        }
                        if(debug && player != game.getCurrentPlayer()){
                            List<Card> hide = player.getHandCardsCopy();
                            StringBuilder hideSB = new StringBuilder();
                            hideSB.append("[DEBUG] handcards: ");
                            if(!hide.isEmpty()){
                                for(int j = 0; j < hide.size(); j++){
                                    if(j > 0){
                                        hideSB.append(", ");
                                    }
                                    hideSB.append(hide.get(j).getName());
                                }
                                System.out.println(hideSB.toString());
                            }
                        }
                        System.out.println(SB.toString());
                        allResult.append(SB.toString()).append("\n");
                    }
                    return allResult.length() > 0 ? allResult.substring(0, allResult.length() - 1) : "";
                }

                case "city":
                case "list":
                case "citadels":{
                    if(parts.length < 2){
                        return "Invaild arguments";
                    }
                    int playerID;
                    try{
                        playerID = Integer.parseInt(parts[1]);
                    }catch(NumberFormatException e){
                        return "Invalid player ID";
                    }

                    Player otherPlayer = game.getOtherPlayer(playerID);
                    List<Card> district = otherPlayer.getBuildDistricCards();
                    if(district.isEmpty()){
                        return "Player " + playerID + " has no built districts";
                    }
                    List<String> name = new ArrayList<>();
                    for (Card c : district) {
                        name.add(c.getName());
                    }
                    return "Player " + playerID + " has built: " + String.join(",", name);
                }

                case "debug":{
                    debug = !debug;
                    if(debug){
                        return "Debug mode ON - other players'hands will be shown";
                    }else{
                        return "Debug mode OFF.";
                    }
                }

                case "save":{
                    if(parts.length < 2){
                        return "Invalid input";
                    }
                    String filename = parts[1];
                    try(FileWriter writer = new FileWriter(filename)){
                        GSON.toJson(this.game, writer);
                        return "Save file " + filename;
                    }catch(IOException e){
                        return "Save failed: " + e.getMessage();
                    }
                }

                case "load":{
                    if(parts.length < 2){
                        return "Invalid";
                    }
                    String filename = parts[1];
                    try(FileReader reader = new FileReader(filename)){
                        Game loadedGame = GSON.fromJson(reader, Game.class);
                        if(loadedGame == null){
                            return "Load failed: invalid file";
                        }
                        this.game = loadedGame;
                        return "Load file " + filename;
                    }catch(IOException e){
                        return "Load failed: " + e.getMessage();
                    }
                }

                case "use":
                    return use(parts);

                default:
                    return "Unknown command: " + command;
            }
        }


     /**
     * Executes a command string by processing it and printing the result.
     * @param line the raw command line to process
     */
    public void realizeCommand(String line){
        System.out.println(processCommand(line));
    }

    /**
     * Handles the activation of a single purple district card's effect during build time.
     * Prompts the player for additional input as needed based on the card name.
     * @param player the Player who built the purple district
     * @param c the Card instance for the purple district
     */
    public void purpleCardsOne(Player player, Card c){
        switch(c.getName().toLowerCase()){
            case "observatory":
                List<Card> three = new ArrayList<>();
                for(int i = 0; i < 3; i++){
                    three.add(deck.drawOrThrow());

                }
                System.out.println("Top 3 cards: " + three);
                for(int i = 0; i < three.size(); i++){
                    System.out.println((i + 1) + ": " + three.get(i).getName());
                }
                System.out.print("Choose one from the top three cards: ");
                int choose;
                try{
                    choose = Integer.parseInt(scanner.nextLine().trim()) - 1;
                }catch(NumberFormatException | NoSuchElementException e){
                    choose = 0;
                }
                
                Card save = three.get(choose);
                player.drawCard(save);
                for(int i = 0; i < three.size(); i++){
                    if(i != choose){
                        deck.addBottom(three.get(i));
                    }
                }
                System.out.println("Observatory: keep " + save.getName() + " in handcards.");
                break;
                            
            case "lighthouse":
                Card top = deck.drawOrThrow();
                player.drawCard((top));
                deck.shuffle();
                System.out.println("Lighthouse: you can pick a card " + top.getName());
                break;
                            
            case "belltower":
                System.out.println("Bell Tower: when built, the game will end after the round in which a player first places his 7th district.(yes or no) ");
                try {
                    String line = scanner.nextLine().trim().toLowerCase();
                    if("yes".equals(line)){
                        bellTower = true;
                        System.out.println("the game will end after the round in which a player first places his 7th district.  ");
                    }else{
                        System.out.println("The BellTower ability won't activate");
                    }
                } catch(NoSuchElementException e){
                    System.out.println("The BellTower ability won't activate");
                }
                break;
                            
            case "hospital":
                System.out.println("Hospital: ven if you are assassinated, you may take an action during your turn  ");
                break;
                            
            case "museum":
                System.out.println("Museum: on your turn, you may place on district card from your hand face down under the Museum. ");
                if(player.getHandCards().isEmpty()){
                    System.out.println(" No cards invalid");
                    break;
                 }
                System.out.println("Your handcards: ");
                for(int i = 0; i< player.getHandCards().size(); i++){
                    System.out.println(" " + (i + 1) + ": " + player.getHandCards().get(i).getName());
                }
                System.out.print("Choose one to place under Museum (1-" + player.getHandCards().size() + "), or enter 0 to skip: ");
                try{
                    choose = Integer.parseInt(scanner.nextLine().trim());
                }catch(NumberFormatException | NoSuchElementException e){
                    choose = 0;
                }
                if(choose > 1 && choose <= player.getHandCards().size()){
                    Card choosed = player.getHandCards().remove(choose - 1);
                    System.out.println(choosed.getName() + " is placed under museum");
                }else{
                    System.out.println("SKIP");
                }
                break;
                            
            case "quarry":
                System.out.println("Quarry: when building, you may play a district already found in your city.  You may only have one such duplicate district in your city at any one time");
                break;
                            
            case "school of magic":
                System.out.println("School of Magic: for the purposes of income, the School Of Magic is considered to be the color of your choice.");
                String colour = "blue";
                try {
                    while(true){
                        colour = scanner.nextLine().trim().toLowerCase();
                        if(colour.equals("yellow") || colour.equals("blue") || colour.equals("green") || colour.equals("red")){
                            break;
                        }
                        System.out.println("Invalid colour.Enter one of: yellow, blue, green, red. ");
                    }
                } catch(NoSuchElementException e){
                    // keep default colour
                }
                player.setSchoolOfMagicColour(colour);
                System.out.println("School Of Magic will count as " + colour + " districts for income.");
                break;
                            
            case "library":
                System.out.println("Library: if you choose to draw cards when you take an action, you keep both of the cards you have drawn.");
                break;

            case "haunted city":
                System.out.println("Haunted city: For the purposes of victory points, the Haunted City is considered to be of the color of your choice.  You cannot use this ability if you built it during the last round of the game ");
                String hauntedCity = "blue";
                try {
                    while(true){
                        hauntedCity = scanner.nextLine().trim().toLowerCase();
                        if(hauntedCity.equals("yellow") || hauntedCity.equals("red") || hauntedCity.equals("blue") || hauntedCity.equals("green") ){
                            break;
                        }
                        System.out.println("Enter one of : yellow, blue, red, green");
                    }
                } catch(NoSuchElementException e){
                    // keep default colour
                }
                player.setHauntedCity(hauntedCity);
                System.out.println("Haunted City will count as " + hauntedCity + " for scores");
                break;

            case "throne room":
                System.out.println("Throne Room : Every time the Crown switches players, you receive one gold from the bank.");
                break;

            default:
                 break;
        }
                            
    }

    /**
     * Facilitates using a built purple district by delegating to activate().
     * @param parts command tokens where parts[1] is the district name
     * @return result message of the activation or error
     */
    public String use(String[] parts){
        Player player = game.getCurrentPlayer();
        String name = parts[1].toLowerCase();
        Card built = null;
        for(Card card: player.getBuildDistricCards()){
            if(card.getName().equalsIgnoreCase(name) && card.getColour().equalsIgnoreCase("purple")){
                built = card;
                break;
            }
        }
        if(built == null){
            return "You have not built a purple district named '" + parts[1] + "'.";
        }
        return activate(player, built, parts);
    }

    /**
     * Activates a specific purple district's ability during the turn phase.
     * @param player the Player invoking the ability
     * @param card the purple district Card to activate
     * @param parts command tokens containing usage parameters
     * @return result message or error description
     */
    public String activate(Player player, Card card, String[] parts){
        switch(card.getName().toLowerCase()){
            case "laboratory":
                if(player.laboratory){
                    return "Laboratory ability has been already used";
                }
                int index;
                try{
                    index = Integer.parseInt(parts[2]) - 1;
                }catch(NumberFormatException e){
                    return "Invalid hand index";
                }
                List<Card> handcards = player.getHandCards();
                if(index < 0 || index >= handcards.size()){
                    return "Invalid";
                }
                Card discard = handcards.remove(index);
                player.addMoney(1);
                player.laboratory = true;
                return "Discard " + discard.getName() + " and receive 1 gold";
            
            case "smithy":
                if(player.smithy){
                    return "Smithy ability has been already use";
                }

                if(player.getMoney() < 2){
                    return "Insufficient gold to use Simthy";
                }

                player.spendMoney(2);
                for(int i = 0; i < 3; i++){
                    player.drawCard(deck.drawOrThrow());
                }
                player.smithy = true;
                return "Paid 2 gold and drew 3 cards: " + player.getMoney();

            case "armory":
                if(player.armory){
                    return "Armory ability has been already use";
                }
                if(parts.length != 4){
                    return "Invalid";
                }
                int targetID;
                int destroyIndex;
                try{
                    targetID = Integer.parseInt(parts[2]);
                    destroyIndex = Integer.parseInt(parts[3]) - 1;

                }catch(NumberFormatException e){
                    return "Invalid";
                }
                Player target = game.getOtherPlayer(targetID);
                if(target == null){
                    return "No such player";
                }
                List<Card> built = target.getBuildDistricCards();
                if(destroyIndex < 0 || destroyIndex >= built.size()){
                    return " Invalid";
                }
                List<Card> Armory = player.getBuildDistricCards();
                Armory.remove(card);
                Card remove = built.remove(destroyIndex);
                player.armory = true;
                return "Armory self-destructed. Destroyed Player " + targetID +" 's " + remove.getName();

            default:
                return "Invalid";
        }
    }

    /**
     * Performs end-of-turn effects for built districts (e.g., Poor House, Park) and checks for game end.
     * @param player the Player whose turn is ending
     */
    public void end(Player player){
        for(Card card: player.getBuildDistricCards()){
            if(card.getName().equalsIgnoreCase("poor house")){
                if(player.getMoney() == 0){
                    player.addMoney(1);
                    System.out.println("Poor house: If you have no gold at the end of your turn, receive one gold from the bank.");
                }
                break;
            }
        }
        for(Card card: player.getBuildDistricCards()){
            if(card.getName().equalsIgnoreCase("park")){
                if(player.getHandCards().isEmpty()){
                    System.out.println("Park: If you have no cards in your hand at the end of your turn, you may draw 2 cards form the District Deck.");
                    for(int i = 0; i < 2; i++){
                        player.drawCard(deck.drawOrThrow());
                    }
                }
                break;
            }
        } 
        checkOver();  
    }

     /**
     * Calculates and displays final scores for all players, applying special purple district scoring.
     */
    public void gameOver(){
        for(Player player: game.getPlayers()){
            System.out.println("Calculating score for Player "+ player.getId());
            int score = 0;
            System.out.println("Base scores: ");
            for(Card card: player.getBuildDistricCards()){
                String name = card.getName().toLowerCase();
                int value;
                if(name.equals("dragon gate") || name.equals("university")){
                    value = 8;
                    System.out.println(card.getName() + ": value 8 scores at the end");
                }else{
                    value = card.getCost();
                    System.out.println(card.getName() + ": cost " + card.getCost());
                }
                score += value;
            }
            boolean mapRoom = false;
            for(Card c : player.getBuildDistricCards()){
                if("map room".equalsIgnoreCase(c.getName())){
                    mapRoom = true;
                    break;
                }
            }
            if(mapRoom){
                int handcards = player.getHandCards().size();
                score += handcards;
                System.out.println(" Map room bonus : " + handcards +" scores (1 score per card).");
            } else {
                System.out.println("  No Map Room");
            }

            boolean imperialTreasury = false;
            for(Card card: player.getBuildDistricCards()){
                if("Imperial Treasury".equalsIgnoreCase(card.getName())){
                    imperialTreasury = true;
                    break;
                }
            }
            if(imperialTreasury){
                int remain = player.getMoney();
                score += remain;
                System.out.println("Imperial Treasury bonus: " + remain + " points (1 point per gold).");
            } else {
                System.out.println("  No Imperial Treasury.");
            }

            boolean wishingWell = false;
            for(Card card: player.getBuildDistricCards()){
                if("wishing well".equalsIgnoreCase(card.getName())){
                    wishingWell = true;
                    break;
                }
            }
            if(wishingWell){
                int purple = 0;
                for(Card card: player.getBuildDistricCards()){
                    if(!card.getName().equalsIgnoreCase("wishing well") && card.getColour().equalsIgnoreCase("purple")){
                        purple++;
                    }
                }
                score += purple;
                System.out.println("Wishing well : Other purple districts in your city: " + purple + " points (1 per other purple).");
            }else{
                System.out.println("No wishing well");
            }
            
            boolean museum = false;
            for(Card c: player.getBuildDistricCards()){
                if("museum".equalsIgnoreCase(c.getName())){
                    museum = true;
                    break;
                }
            }

            if(museum){
                List<Card> under = player.getMuseum();
                int underPoints = under.size();
                score += underPoints;
                System.out.println("Cards under Museum: add" + underPoints + " points.");
            }else{
                System.out.println("No museum");
            }

            System.out.println("Player " + player.getId() + " total score: " + score);
            System.out.println();
        }
    }
    

    /**
     * Checks for Bell Tower or standard game-over conditions and exits if met.
     */
    public void checkOver(){
        if(bellTower){
            for(Player player: game.getPlayers()){
                if(player.getBuildDistricCards().size() >= 7){
                    System.out.println("Bell Tower condition activated by Player " + player.getId() + ". Game over!");
                    gameOver();
                    System.exit(0);
                }
            }
        }
        for(Player player: game.getPlayers()){
            if(player.getBuildDistricCards().size() >= 8){
                System.out.println("Player " + player.getId() + " has built 8 districts. Game over!");
                gameOver();
                System.exit(0);
            }
        }
    }

    /**
     * Program entry point: prompts for player count and starts game loop.
     */
    
     public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int numberOfPlayers = 4;
        try{
            do{
                System.out.print("Enter number of players (4-7): ");
                numberOfPlayers = scanner.nextInt();
            }while(numberOfPlayers < 4 || numberOfPlayers > 7);
        }catch(Exception e){
            return;
        }

        App app = new App(numberOfPlayers);
        app.run();

    }
    


    


}
