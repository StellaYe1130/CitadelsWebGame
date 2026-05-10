package citadels.web;

import citadels.*;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class WebGameService {

    public enum Phase {
        INIT,
        SELECTION,
        HUMAN_SELECTION,
        TURN,
        HUMAN_TURN_ACTION,
        HUMAN_TURN_BUILD,
        GAME_OVER
    }

    private final int totalPlayers;
    private final Set<Integer> humanPlayerIds;

    private Game game;
    private Deck deck;
    private Phase phase = Phase.INIT;
    private int selectionPlayerIndex = 0;
    private List<Role> selectionRoles;
    private Map<Role, Player> roleToPlayer;
    private List<Role> turnOrder;
    private int turnIndex = 0;
    private Set<Role> assassinatedRoles;
    private Role activeRole;
    private Set<Role> hospitalSet;
    private boolean bellTower = false;
    private boolean humanActionDone = false;
    private int humanBuildCount = 0;
    private int humanMaxBuilds = 1;
    private List<String> eventLog = new ArrayList<>();
    private Player firstCompleted = null;
    private Set<Player> completedPlayers = new HashSet<>();
    private Map<Player, Role> lastRoundRoles = new LinkedHashMap<>();
    private List<Card> pendingDrawCards = null;

    public int currentActingPlayerId = 0;

    public WebGameService(int totalPlayers, Set<Integer> humanPlayerIds) {
        this.totalPlayers = totalPlayers;
        this.humanPlayerIds = new HashSet<>(humanPlayerIds);
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    public void newGame() {
        game = new Game(totalPlayers);
        deck = parseCards();
        deck.shuffle();
        roleToPlayer = new LinkedHashMap<>();
        assassinatedRoles = new HashSet<>();
        hospitalSet = new HashSet<>();
        completedPlayers = new HashSet<>();
        lastRoundRoles = new LinkedHashMap<>();
        bellTower = false;
        firstCompleted = null;
        eventLog = new ArrayList<>();
        pendingDrawCards = null;
        activeRole = null;
        turnOrder = null;
        turnIndex = 0;
        selectionPlayerIndex = 0;
        currentActingPlayerId = 0;

        for (Player p : game.getPlayers()) {
            for (int i = 0; i < 4; i++) p.drawCard(deck.drawOrThrow());
        }

        log("Welcome to Citadels! " + totalPlayers + " players.");
        if (humanPlayerIds.size() == 1 && humanPlayerIds.contains(1)) {
            log("You are Player 1.");
        } else {
            log("Human players: " + humanPlayerIds.stream().sorted().collect(Collectors.toList()));
        }
        startSelectionPhase();
    }

    public GameStateDTO humanAction(int playerId, String input) {
        if (currentActingPlayerId != playerId) return getState();
        switch (phase) {
            case HUMAN_SELECTION:   handleHumanRoleSelection(input); break;
            case HUMAN_TURN_ACTION: handleHumanTurnAction(input); break;
            case HUMAN_TURN_BUILD:  handleHumanBuildPhase(input); break;
            default: break;
        }
        return getState();
    }

    // Runs AI steps until a human must act, game over, or stuck.
    public void autoAdvance() {
        int safety = 2000;
        while (safety-- > 0
                && phase != Phase.GAME_OVER
                && phase != Phase.HUMAN_SELECTION
                && phase != Phase.HUMAN_TURN_ACTION
                && phase != Phase.HUMAN_TURN_BUILD) {
            if (phase == Phase.SELECTION) doAIRoleSelection();
            else if (phase == Phase.TURN)  doAITurn();
            else break;
        }
    }

    public GameStateDTO getState() {
        GameStateDTO dto = new GameStateDTO();
        dto.phase = phase.name();
        dto.gameOver = (phase == Phase.GAME_OVER);
        dto.log = new ArrayList<>(eventLog);
        dto.activeRole = activeRole != null ? activeRole.name() : null;
        dto.currentActingPlayerId = currentActingPlayerId;

        if (game != null) {
            // Build reverse map once (fixes N+1 over roleToPlayer per player)
            Map<Player, Role> playerToRole = new HashMap<>();
            if (roleToPlayer != null) {
                for (Map.Entry<Role, Player> e : roleToPlayer.entrySet()) {
                    playerToRole.put(e.getValue(), e.getKey());
                }
            }

            dto.players = game.getPlayers().stream().map(p -> {
                GameStateDTO.PlayerDTO pd = new GameStateDTO.PlayerDTO();
                pd.id = p.getId();
                pd.isHuman = humanPlayerIds.contains(p.getId());
                pd.gold = p.getMoney();
                pd.handSize = p.getHandCards().size();
                pd.city = p.getBuildDistricCards().stream()
                        .map(c -> new GameStateDTO.CardDTO(c.getName(), c.getColour(), c.getCost(), c.getDescription()))
                        .collect(Collectors.toList());
                pd.isCurrentTurn = (activeRole != null && roleToPlayer != null
                        && p.equals(roleToPlayer.get(activeRole)));
                Role r = playerToRole.get(p);
                if (r != null) pd.role = r.name();
                if (pd.isHuman) {
                    pd.hand = p.getHandCards().stream()
                            .map(c -> new GameStateDTO.CardDTO(c.getName(), c.getColour(), c.getCost(), c.getDescription()))
                            .collect(Collectors.toList());
                }
                return pd;
            }).collect(Collectors.toList());
        }

        if (phase == Phase.HUMAN_SELECTION && selectionRoles != null) {
            dto.availableRoles = selectionRoles.stream().map(Role::name).collect(Collectors.toList());
        }

        if (pendingDrawCards != null) {
            dto.pendingCards = pendingDrawCards.stream()
                    .map(c -> new GameStateDTO.CardDTO(c.getName(), c.getColour(), c.getCost(), c.getDescription()))
                    .collect(Collectors.toList());
        }

        dto.prompt = buildPrompt();
        return dto;
    }

    // ─── Selection Phase ─────────────────────────────────────────────────────

    private void startSelectionPhase() {
        phase = Phase.SELECTION;
        assassinatedRoles.clear();
        roleToPlayer.clear();
        hospitalSet.clear();
        lastRoundRoles.clear();

        selectionRoles = new ArrayList<>(Arrays.asList(Role.values()));
        Collections.shuffle(selectionRoles);

        selectionRoles.remove(0);
        log("A mystery character was removed.");

        int numPlayers = game.getPlayers().size();
        int faceUpCount = numPlayers == 4 ? 2 : numPlayers == 5 ? 1 : 0;
        for (int i = 0; i < faceUpCount; ) {
            int idx = 0;
            while (idx < selectionRoles.size() && selectionRoles.get(idx) == Role.King) idx++;
            if (idx < selectionRoles.size()) {
                log(selectionRoles.remove(idx).name() + " was removed face-up.");
                i++;
            } else break;
        }

        advanceSelection();
    }

    private void advanceSelection() {
        List<Player> players = game.getPlayers();
        if (selectionPlayerIndex >= players.size()) {
            startTurnPhase();
            return;
        }
        Player current = players.get(selectionPlayerIndex);
        if (humanPlayerIds.contains(current.getId())) {
            phase = Phase.HUMAN_SELECTION;
            currentActingPlayerId = current.getId();
            log("Player " + current.getId() + ": choose your character.");
        } else {
            phase = Phase.SELECTION;
            currentActingPlayerId = 0;
        }
    }

    private void doAIRoleSelection() {
        List<Player> players = game.getPlayers();
        if (selectionPlayerIndex >= players.size()) return;
        Player current = players.get(selectionPlayerIndex);
        if (humanPlayerIds.contains(current.getId())) return;

        Role chosen = aiChooseRole(current, selectionRoles);
        roleToPlayer.put(chosen, current);
        lastRoundRoles.put(current, chosen);
        selectionRoles.remove(chosen);
        log("Player " + current.getId() + " chose a character.");
        selectionPlayerIndex++;
        advanceSelection();
    }

    private void handleHumanRoleSelection(String roleName) {
        Player player = getPlayer(currentActingPlayerId);
        Role chosen = findRole(roleName);
        if (chosen == null || !selectionRoles.contains(chosen)) {
            log("Invalid role. Choose from: " + roleNames(selectionRoles));
            return;
        }
        roleToPlayer.put(chosen, player);
        lastRoundRoles.put(player, chosen);
        selectionRoles.remove(chosen);
        log("Player " + currentActingPlayerId + " chose " + chosen.name() + ".");
        selectionPlayerIndex++;
        advanceSelection();
    }

    private Role aiChooseRole(Player player, List<Role> available) {
        int gold = player.getMoney();
        int handSize = player.getHandCards().size();
        long yellow = countColour(player, "yellow");
        long blue   = countColour(player, "blue");
        long green  = countColour(player, "green");
        long red    = countColour(player, "red");

        return available.stream().max(Comparator.comparingInt(r -> {
            switch (r) {
                case King:      return (int)(5 + yellow * 2);
                case Bishop:    return (int)(4 + blue * 2);
                case Merchant:  return (int)(3 + green * 2 + (gold < 3 ? 2 : 0));
                case Architect: return handSize < 3 ? 8 : (gold > 5 ? 6 : 3);
                case Magician:  return handSize < 2 ? 7 : 2;
                case Warlord:   return (int)(3 + red * 2);
                case Thief:     return gold < 2 ? 5 : 2;
                case Assassin:  return 3;
                default:        return 0;
            }
        })).orElse(available.get(0));
    }

    // ─── Turn Phase ───────────────────────────────────────────────────────────

    private void startTurnPhase() {
        turnOrder = new ArrayList<>(roleToPlayer.keySet());
        turnOrder.sort(Comparator.comparingInt(Role::getOrder));
        turnIndex = 0;
        log("================================");
        log("TURN PHASE");
        log("================================");
        advanceTurn();
    }

    private void advanceTurn() {
        while (turnIndex < turnOrder.size()) {
            Role r = turnOrder.get(turnIndex);
            Player p = roleToPlayer.get(r);
            if (assassinatedRoles.contains(r)) {
                boolean hasHospital = p.getBuildDistricCards().stream()
                        .anyMatch(c -> "hospital".equalsIgnoreCase(c.getName()));
                if (hasHospital && !hospitalSet.contains(r)) {
                    hospitalSet.add(r);
                    log(r.name() + " was assassinated but Hospital allows a limited action.");
                    break;
                } else {
                    log(r.name() + " was assassinated. Skipping.");
                    turnIndex++;
                }
            } else break;
        }

        if (turnIndex >= turnOrder.size()) { endRound(); return; }

        Role role = turnOrder.get(turnIndex);
        Player player = roleToPlayer.get(role);
        activeRole = role;
        game.setCurrentIndex(game.getPlayers().indexOf(player));

        applyRoleStartBonuses(player, role);

        if (humanPlayerIds.contains(player.getId())) {
            humanActionDone = false;
            humanBuildCount = 0;
            humanMaxBuilds = (role == Role.Architect) ? 3 : 1;
            phase = Phase.HUMAN_TURN_ACTION;
            currentActingPlayerId = player.getId();
            log("=== Player " + player.getId() + "'s turn as " + role.getTheName() + " ===");
            log("Gold: " + player.getMoney() + " | Cards in hand: " + player.getHandCards().size());
        } else {
            phase = Phase.TURN;
            currentActingPlayerId = 0;
        }
    }

    private void doAITurn() {
        if (turnIndex >= turnOrder.size()) return;
        Role role = turnOrder.get(turnIndex);
        Player player = roleToPlayer.get(role);
        boolean assassinated = assassinatedRoles.contains(role);
        boolean hospitalized = hospitalSet.contains(role);

        log("--- " + role.name() + ": Player " + player.getId() + " ---");

        if (assassinated && !hospitalized) {
            log("Player " + player.getId() + " loses their turn (assassinated).");
            finishAITurn();
            return;
        }

        if (!assassinated || hospitalized) {
            if (role == Role.Assassin) aiDoAssassin(player);
            else if (role == Role.Thief) aiDoThief(player);
        }

        if (role == Role.Architect && !assassinated) {
            for (int i = 0; i < 2; i++) {
                try { player.drawCard(deck.drawOrThrow()); } catch (NoSuchElementException e) { break; }
            }
            log("Player " + player.getId() + " drew 2 extra cards (Architect).");
        }

        if (!assassinated || hospitalized) {
            if (player.getMoney() < 4 || player.getHandCards().isEmpty()) {
                player.addMoney(2);
                log("Player " + player.getId() + " collected 2 gold. (total: " + player.getMoney() + ")");
            } else {
                try {
                    Card c1 = deck.drawOrThrow(), c2 = deck.drawOrThrow();
                    Card keep = c1.getCost() >= c2.getCost() ? c1 : c2;
                    player.drawCard(keep);
                    deck.addBottom(keep == c1 ? c2 : c1);
                    log("Player " + player.getId() + " drew a card.");
                } catch (NoSuchElementException e) {
                    player.addMoney(2);
                    log("Player " + player.getId() + " collected 2 gold (deck empty).");
                }
            }

            if (role == Role.Magician) aiDoMagician(player);

            int maxBuilds = (role == Role.Architect) ? 3 : 1;
            for (int b = 0; b < maxBuilds; b++) {
                Card toBuild = aiBestBuild(player);
                if (toBuild == null) break;
                player.spendMoney(toBuild.getCost());
                player.buildDistricCards(toBuild);
                log("Player " + player.getId() + " built " + fmt(toBuild) + ".");
                if (checkGameOver()) return;
            }

            if (role == Role.Warlord) aiDoWarlord(player);

            applyEndBonuses(player, role);
        }

        finishAITurn();
    }

    private void finishAITurn() {
        turnIndex++;
        if (phase != Phase.GAME_OVER) advanceTurn();
    }

    private void endRound() {
        Player king = game.getKingPlayer();
        if (king != null) {
            selectionPlayerIndex = game.getPlayers().indexOf(king);
            log(king.getId() + " is the crowned player and goes first next round.");
        } else {
            selectionPlayerIndex = 0;
        }
        log("================================");
        log("New round!");
        log("================================");
        startSelectionPhase();
    }

    // ─── Human Turn ───────────────────────────────────────────────────────────

    private void handleHumanTurnAction(String input) {
        Player human = getPlayer(currentActingPlayerId);
        input = input.toLowerCase().trim();

        if (input.startsWith("assassinate ") && activeRole == Role.Assassin) {
            Role target = findRole(input.substring(12).trim());
            if (target == null || target == Role.Assassin) { log("Invalid target."); return; }
            assassinatedRoles.add(target);
            log("Player " + currentActingPlayerId + " assassinated the " + target.name() + "!");
            return;
        }

        if (input.startsWith("steal ") && activeRole == Role.Thief) {
            Role target = findRole(input.substring(6).trim());
            if (target == null || target == Role.Assassin || assassinatedRoles.contains(target)) {
                log("Cannot steal from that role.");
                return;
            }
            Player victim = roleToPlayer.get(target);
            if (victim == null) { log(target.name() + " is not in this round."); return; }
            int stolen = victim.getMoney();
            victim.spendMoney(stolen);
            human.addMoney(stolen);
            log("Player " + currentActingPlayerId + " stole " + stolen + " gold from the " + target.name() + "!");
            return;
        }

        if (input.equals("income")) {
            human.addMoney(2);
            log("Player " + currentActingPlayerId + " received 2 gold. Total: " + human.getMoney());
            afterHumanAction(human);
        } else if (input.equals("draw")) {
            try {
                Card c1 = deck.drawOrThrow(), c2 = deck.drawOrThrow();
                pendingDrawCards = Arrays.asList(c1, c2);
                log("Drew 2 cards — choose one to keep.");
            } catch (NoSuchElementException e) {
                human.addMoney(2);
                log("Deck is empty, took 2 gold instead.");
                afterHumanAction(human);
            }
        } else if (input.equals("draw 1") || input.equals("draw 2")) {
            if (pendingDrawCards != null) {
                int idx = input.endsWith("1") ? 0 : 1;
                Card keep = pendingDrawCards.get(idx);
                Card drop = pendingDrawCards.get(1 - idx);
                human.drawCard(keep);
                deck.addBottom(drop);
                log("Player " + currentActingPlayerId + " kept: " + fmt(keep));
                pendingDrawCards = null;
                afterHumanAction(human);
            }
        } else {
            log("Choose: 'income' or 'draw'"
                    + (activeRole == Role.Assassin ? " | 'assassinate <role>'" : "")
                    + (activeRole == Role.Thief ? " | 'steal <role>'" : ""));
        }
    }

    private void afterHumanAction(Player human) {
        if (activeRole == Role.Architect) {
            for (int i = 0; i < 2; i++) {
                try { human.drawCard(deck.drawOrThrow()); } catch (NoSuchElementException e) { break; }
            }
            log("Architect: drew 2 extra cards.");
        }
        phase = Phase.HUMAN_TURN_BUILD;
        log("Build a district ('build <n>'), or type 'end' to finish your turn.");
        if (activeRole == Role.Magician)
            log("Magician: 'swap <playerID>' to swap hands, or 'redraw <1,2,...>' to redraw cards.");
        if (activeRole == Role.Warlord)
            log("Warlord: 'destroy <playerID> <distPos>' to destroy a district.");
    }

    private void handleHumanBuildPhase(String input) {
        Player human = getPlayer(currentActingPlayerId);
        input = input.toLowerCase().trim();

        if (input.equals("end")) {
            applyEndBonuses(human, activeRole);
            if (checkGameOver()) return;
            log("Player " + currentActingPlayerId + " ended their turn.");
            turnIndex++;
            advanceTurn();
            return;
        }

        if (input.startsWith("build ")) {
            tryBuild(human, input.substring(6).trim());
            return;
        }

        if (input.startsWith("swap ") && activeRole == Role.Magician) {
            try {
                int tid = Integer.parseInt(input.substring(5).trim());
                Player other = game.getOtherPlayer(tid);
                if (other == null || other == human) { log("Invalid player."); return; }
                swapHands(human, other);
                log("Player " + currentActingPlayerId + " swapped hands with Player " + tid + ".");
            } catch (NumberFormatException e) { log("Usage: swap <playerID>"); }
            return;
        }

        if (input.startsWith("redraw ") && activeRole == Role.Magician) {
            String[] parts = input.substring(7).split(",");
            List<Integer> indices = new ArrayList<>();
            boolean ok = true;
            for (String s : parts) {
                try {
                    int i = Integer.parseInt(s.trim()) - 1;
                    if (i < 0 || i >= human.getHandCards().size()) { ok = false; break; }
                    indices.add(i);
                } catch (NumberFormatException e) { ok = false; break; }
            }
            if (ok && !indices.isEmpty()) {
                indices.sort(Collections.reverseOrder());
                List<Card> removed = new ArrayList<>();
                for (int i : indices) removed.add(human.getHandCards().remove(i));
                for (Card c : removed) deck.addBottom(c);
                for (int i = 0; i < removed.size(); i++) {
                    try { human.drawCard(deck.drawOrThrow()); } catch (NoSuchElementException e) { break; }
                }
                log("Player " + currentActingPlayerId + " redrawn " + removed.size() + " card(s).");
            } else { log("Usage: redraw <1,2,...>"); }
            return;
        }

        if (input.startsWith("destroy ") && activeRole == Role.Warlord) {
            String[] parts = input.split("\\s+");
            if (parts.length == 3) {
                try {
                    int tid = Integer.parseInt(parts[1]);
                    int di  = Integer.parseInt(parts[2]) - 1;
                    Player target = game.getOtherPlayer(tid);
                    if (target == null) { log("No such player."); return; }
                    for (Map.Entry<Role, Player> e : roleToPlayer.entrySet()) {
                        if (e.getValue() == target && e.getKey() == Role.Bishop
                                && !assassinatedRoles.contains(Role.Bishop)) {
                            log("Cannot destroy Bishop's districts."); return;
                        }
                    }
                    List<Card> districts = target.getBuildDistricCards();
                    if (di < 0 || di >= districts.size()) { log("Invalid position."); return; }
                    Card toDestroy = districts.get(di);
                    if ("keep".equalsIgnoreCase(toDestroy.getName())) { log("The Keep cannot be destroyed."); return; }
                    int cost = toDestroy.getCost() - 1;
                    if (!human.spendMoney(cost)) { log("Not enough gold (costs " + cost + ")."); return; }
                    districts.remove(di);
                    log("Player " + currentActingPlayerId + " destroyed " + toDestroy.getName()
                            + " from Player " + tid + " (cost " + cost + ").");
                } catch (NumberFormatException e) { log("Usage: destroy <playerID> <distPos>"); }
            } else { log("Usage: destroy <playerID> <distPos>"); }
            return;
        }

        log("Commands: build <n> | end"
                + (activeRole == Role.Magician ? " | swap <id> | redraw <1,2,...>" : "")
                + (activeRole == Role.Warlord ? " | destroy <playerID> <distPos>" : ""));
    }

    private void tryBuild(Player player, String indexStr) {
        try {
            int idx = Integer.parseInt(indexStr) - 1;
            List<Card> hand = player.getHandCards();
            if (idx < 0 || idx >= hand.size()) { log("Invalid card position."); return; }
            if (humanBuildCount >= humanMaxBuilds) { log("No more builds this turn."); return; }
            Card c = hand.get(idx);
            long dupCount = player.getBuildDistricCards().stream()
                    .filter(b -> b.getName().equalsIgnoreCase(c.getName())).count();
            if (dupCount > 0) {
                boolean hasQuarry = player.getBuildDistricCards().stream()
                        .anyMatch(b -> "quarry".equalsIgnoreCase(b.getName()));
                if (!hasQuarry || dupCount >= 2) { log("Cannot build duplicate: " + c.getName()); return; }
            }
            if (!player.spendMoney(c.getCost())) {
                log("Not enough gold (costs " + c.getCost() + ", have " + player.getMoney() + ")."); return;
            }
            player.buildDistricCards(c);
            log("Player " + currentActingPlayerId + " built: " + fmt(c));
            humanBuildCount++;
            checkGameOver();
        } catch (NumberFormatException e) { log("Usage: build <position>"); }
    }

    // ─── AI Special Abilities ────────────────────────────────────────────────

    private void aiDoAssassin(Player assassin) {
        Role targetRole = null;
        int maxCity = -1;
        for (Map.Entry<Role, Player> e : roleToPlayer.entrySet()) {
            if (e.getKey() == Role.Assassin || e.getValue() == assassin) continue;
            int sz = e.getValue().getBuildDistricCards().size();
            if (sz > maxCity) { maxCity = sz; targetRole = e.getKey(); }
        }
        if (targetRole != null) {
            assassinatedRoles.add(targetRole);
            log("Player " + assassin.getId() + " (Assassin) killed the " + targetRole.name() + ".");
        }
    }

    private void aiDoThief(Player thief) {
        Role targetRole = null;
        int maxGold = 0;
        for (Map.Entry<Role, Player> e : roleToPlayer.entrySet()) {
            Role r = e.getKey(); Player p = e.getValue();
            if (r == Role.Assassin || r == Role.Thief || p == thief) continue;
            if (assassinatedRoles.contains(r)) continue;
            if (p.getMoney() > maxGold) { maxGold = p.getMoney(); targetRole = r; }
        }
        if (targetRole != null && maxGold > 0) {
            Player victim = roleToPlayer.get(targetRole);
            int stolen = victim.getMoney();
            victim.spendMoney(stolen);
            thief.addMoney(stolen);
            log("Player " + thief.getId() + " (Thief) stole " + stolen + " gold from " + targetRole.name() + ".");
        }
    }

    private void aiDoMagician(Player magician) {
        Player richest = null;
        int maxCards = magician.getHandCards().size();
        for (Player p : game.getPlayers()) {
            if (p == magician) continue;
            if (p.getHandCards().size() > maxCards) { maxCards = p.getHandCards().size(); richest = p; }
        }
        if (richest != null) {
            swapHands(magician, richest);
            log("Player " + magician.getId() + " (Magician) swapped hands with Player " + richest.getId() + ".");
        }
    }

    private void aiDoWarlord(Player warlord) {
        Player bestTarget = null; int bestCity = -1; int bestIdx = -1;
        for (Player p : game.getPlayers()) {
            if (p == warlord) continue;
            if (p.getBuildDistricCards().size() >= 8) continue;
            Role pr = null;
            for (Map.Entry<Role, Player> e : roleToPlayer.entrySet())
                if (e.getValue() == p) { pr = e.getKey(); break; }
            if (pr == Role.Bishop && !assassinatedRoles.contains(Role.Bishop)) continue;
            List<Card> ds = p.getBuildDistricCards();
            for (int i = 0; i < ds.size(); i++) {
                Card c = ds.get(i);
                if ("keep".equalsIgnoreCase(c.getName())) continue;
                int cost = c.getCost() - 1;
                if (warlord.getMoney() >= cost && ds.size() > bestCity) {
                    bestCity = ds.size(); bestTarget = p; bestIdx = i; break;
                }
            }
        }
        if (bestTarget != null && bestIdx >= 0) {
            Card destroyed = bestTarget.getBuildDistricCards().get(bestIdx);
            warlord.spendMoney(destroyed.getCost() - 1);
            bestTarget.getBuildDistricCards().remove(bestIdx);
            log("Player " + warlord.getId() + " (Warlord) destroyed " + destroyed.getName()
                    + " from Player " + bestTarget.getId() + ".");
        }
    }

    // ─── Bonuses & End ────────────────────────────────────────────────────────

    private void applyRoleStartBonuses(Player player, Role role) {
        if (role == Role.King) {
            long count = countColour(player, "yellow");
            if (count > 0) { player.addMoney((int) count); log("King bonus: +" + count + " gold."); }
            game.setKing(player);
            log("Player " + player.getId() + " receives the crown.");
            for (Player p : game.getPlayers()) {
                if (p.getBuildDistricCards().stream().anyMatch(c -> "throne room".equalsIgnoreCase(c.getName()))) {
                    p.addMoney(1);
                    log("Throne Room: Player " + p.getId() + " gains 1 gold.");
                    break;
                }
            }
        } else if (role == Role.Bishop) {
            long count = countColour(player, "blue");
            if (count > 0) { player.addMoney((int) count); log("Bishop bonus: +" + count + " gold."); }
        }
    }

    private void applyEndBonuses(Player player, Role role) {
        if (role == Role.Merchant) {
            long count = countColour(player, "green");
            player.addMoney((int) count + 1);
            log("Merchant bonus: +" + (count + 1) + " gold.");
        } else if (role == Role.Warlord) {
            long count = countColour(player, "red");
            if (count > 0) { player.addMoney((int) count); log("Warlord bonus: +" + count + " gold."); }
        }
        if (player.getMoney() == 0 && player.getBuildDistricCards().stream()
                .anyMatch(c -> "poor house".equalsIgnoreCase(c.getName()))) {
            player.addMoney(1);
            log("Poor House: Player " + player.getId() + " gains 1 gold.");
        }
        if (player.getHandCards().isEmpty() && player.getBuildDistricCards().stream()
                .anyMatch(c -> "park".equalsIgnoreCase(c.getName()))) {
            for (int i = 0; i < 2; i++) {
                try { player.drawCard(deck.drawOrThrow()); } catch (NoSuchElementException e) { break; }
            }
            log("Park: Player " + player.getId() + " drew 2 cards.");
        }
    }

    // ─── Game Over ────────────────────────────────────────────────────────────

    private boolean checkGameOver() {
        boolean triggered = false;
        for (Player p : game.getPlayers()) {
            int sz = p.getBuildDistricCards().size();
            if ((bellTower && sz >= 7) || sz >= 8) {
                if (firstCompleted == null) firstCompleted = p;
                completedPlayers.add(p);
                triggered = true;
            }
        }
        if (triggered) {
            phase = Phase.GAME_OVER;
            currentActingPlayerId = 0;
            log("================================");
            log("GAME OVER!");
            log("================================");
            calculateScores();
        }
        return triggered;
    }

    private void calculateScores() {
        Map<Player, Integer> scores = new LinkedHashMap<>();
        for (Player p : game.getPlayers()) {
            int score = 0;
            for (Card c : p.getBuildDistricCards()) {
                String n = c.getName().toLowerCase();
                score += (n.equals("dragon gate") || n.equals("university")) ? 8 : c.getCost();
            }
            Set<String> colors = p.getBuildDistricCards().stream()
                    .map(c -> colour(c, p)).collect(Collectors.toSet());
            if (colors.containsAll(Arrays.asList("yellow", "blue", "green", "red", "purple"))) {
                score += 3; log("Player " + p.getId() + " +3 (all 5 colors).");
            }
            if (p.equals(firstCompleted))          { score += 4; log("Player " + p.getId() + " +4 (first complete)."); }
            else if (completedPlayers.contains(p)) { score += 2; log("Player " + p.getId() + " +2 (completed city)."); }
            if (p.getBuildDistricCards().stream().anyMatch(c -> "map room".equalsIgnoreCase(c.getName()))) {
                int bonus = p.getHandCards().size();
                score += bonus; log("Player " + p.getId() + " +" + bonus + " (Map Room).");
            }
            if (p.getBuildDistricCards().stream().anyMatch(c -> "imperial treasury".equalsIgnoreCase(c.getName()))) {
                score += p.getMoney(); log("Player " + p.getId() + " +" + p.getMoney() + " (Imperial Treasury).");
            }
            if (p.getBuildDistricCards().stream().anyMatch(c -> "wishing well".equalsIgnoreCase(c.getName()))) {
                long purple = p.getBuildDistricCards().stream()
                        .filter(c -> "purple".equalsIgnoreCase(c.getColour()) && !"wishing well".equalsIgnoreCase(c.getName())).count();
                score += purple; log("Player " + p.getId() + " +" + purple + " (Wishing Well).");
            }
            log("Player " + p.getId() + " TOTAL: " + score + " points.");
            scores.put(p, score);
        }
        int max = scores.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        List<Player> tied = scores.entrySet().stream().filter(e -> e.getValue() == max)
                .map(Map.Entry::getKey).collect(Collectors.toList());
        Player winner = tied.size() == 1 ? tied.get(0)
                : tied.stream().max(Comparator.comparingInt(p -> {
                    Role r = lastRoundRoles.get(p); return r != null ? r.getOrder() : 0;
                })).orElse(tied.get(0));
        log("Player " + winner.getId() + " wins with " + max + " points!");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void swapHands(Player a, Player b) {
        List<Card> tmp = new ArrayList<>(a.getHandCards());
        a.setHandCards(new ArrayList<>(b.getHandCards()));
        b.setHandCards(tmp);
    }

    private Card aiBestBuild(Player player) {
        return player.getHandCards().stream()
                .filter(c -> c.getCost() <= player.getMoney())
                .filter(c -> player.getBuildDistricCards().stream()
                        .noneMatch(b -> b.getName().equalsIgnoreCase(c.getName())))
                .max(Comparator.comparingInt(Card::getCost))
                .orElse(null);
    }

    private long countColour(Player p, String colour) {
        return p.getBuildDistricCards().stream().filter(c -> colour(c, p).equals(colour)).count();
    }

    private String colour(Card c, Player p) {
        if ("school of magic".equalsIgnoreCase(c.getName()) && p.getSchoolOfMagicColour() != null)
            return p.getSchoolOfMagicColour();
        if ("haunted city".equalsIgnoreCase(c.getName()) && p.getHauntedCity() != null)
            return p.getHauntedCity();
        return c.getColour().toLowerCase();
    }

    private String fmt(Card c) {
        return c.getName() + " [" + c.getColour().toLowerCase() + c.getCost() + "]";
    }

    private Role findRole(String name) {
        for (Role r : Role.values()) if (r.name().equalsIgnoreCase(name)) return r;
        return null;
    }

    private Player getPlayer(int id) {
        return game.getPlayers().stream().filter(p -> p.getId() == id).findFirst()
                .orElse(game.getPlayers().get(0));
    }

    private String roleNames(List<Role> roles) {
        return roles.stream().map(Role::name).collect(Collectors.joining(", "));
    }

    private String buildPrompt() {
        if (phase == null) return "";
        switch (phase) {
            case INIT:            return "Waiting for players to join...";
            case SELECTION:       return "AI player is choosing a character...";
            case HUMAN_SELECTION: return "Player " + currentActingPlayerId + ": choose your character.";
            case TURN:            return "AI player is taking their turn...";
            case HUMAN_TURN_ACTION:
                if (pendingDrawCards != null) return "Player " + currentActingPlayerId + ": choose a card to keep.";
                return "Player " + currentActingPlayerId + ": take income (2 gold) or draw 2 cards"
                        + (activeRole == Role.Assassin ? " | assassinate <role>" : "")
                        + (activeRole == Role.Thief    ? " | steal <role>" : "");
            case HUMAN_TURN_BUILD:
                return "Player " + currentActingPlayerId + ": build a district or end turn"
                        + (activeRole == Role.Magician ? " | swap <id> | redraw <1,2,...>" : "")
                        + (activeRole == Role.Warlord  ? " | destroy <playerID> <distPos>" : "");
            case GAME_OVER: return "Game Over! Check the scores.";
            default:        return "";
        }
    }

    private void log(String msg) {
        eventLog.add(msg);
        if (eventLog.size() > 200) {
            eventLog.subList(0, 50).clear();
        }
    }

    private Deck parseCards() {
        Deck d = new Deck();
        InputStream in = getClass().getResourceAsStream("/citadels/cards.tsv");
        try (Scanner sc = new Scanner(in, "UTF-8")) {
            if (sc.hasNextLine()) sc.nextLine();
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\t");
                if (parts.length < 4) continue;
                String name = parts[0].trim();
                int qty     = Integer.parseInt(parts[1].trim());
                String col  = parts[2].trim();
                int cost    = Integer.parseInt(parts[3].trim());
                String desc = parts.length >= 5 ? parts[4].trim() : "";
                for (int i = 0; i < qty; i++) d.addCard(new Card(name, cost, col, desc));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing cards.tsv: " + e.getMessage());
        }
        return d;
    }
}
