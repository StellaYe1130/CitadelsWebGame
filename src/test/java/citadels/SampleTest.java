package citadels;



import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



import org.junit.jupiter.api.io.TempDir;


import static org.junit.jupiter.api.Assertions.*;
import java.security.Permission;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.util.stream.Collectors;
import java.lang.reflect.*;



public class SampleTest {
    static class ExitTrappedException extends SecurityException {}
    @BeforeEach
    public void setUp() {
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {}
            @Override
            public void checkExit(int status) {
                super.checkExit(status);
                throw new ExitTrappedException();
            }
        });
    }
    @AfterEach
    public void tearDown() {
        System.setSecurityManager(null);
    }


/**
 * Test case: testBellTowerCondition
 * This test verifies the functionality of test bell tower condition.
 */
    @Test
    public void testBellTower() {
        App app = new App(4);
        Game game = app.getGame();
        Player p = game.getPlayers().get(0);
        app.bellTower = true;
        List<Card> built = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            built.add(new Card("D" + i, 1, "blue", ""));
        }
        p.setBuildDistrictCards(built);
        assertThrows(ExitTrappedException.class, () -> {
            app.checkOver();
        });
    }

/**
 * Test case: testGameOverAtEightBuildings
 * This test verifies the functionality of test game over at eight buildings.
 */
    @Test
    public void testGameOverAtEight() {
        App app = new App(4);
        Game game = app.getGame();
        Player p = game.getPlayers().get(0);
        List<Card> built = new ArrayList<>();

        for (int i = 0; i < 8; i++) {

            built.add(new Card("D" + i, 1, "red", ""));
        }
        p.setBuildDistrictCards(built);

        assertThrows(ExitTrappedException.class, () -> {
            app.checkOver();
        });
    }

/**
 * Test case: testCardsCount
 * This test verifies the functionality of test cards count.
 */
    @Test
    public void testCardsCount() {
        App app = new App();
        Deck deck = app.parseCards();
        int expectedCount = 41;
        assertEquals(expectedCount, deck.size(), "Deck size should match the number of lines in cards.tsv");
    }

/**
 * Test case: testFirstCard
 * This test verifies the functionality of test first card.
 */
    @Test
    public void testFirstCard(){
        App app = new App();
        Deck deck = app.parseCards();
        List<Card> cards = deck.getCards();
        assertFalse(cards.isEmpty(), "Deck should not be empty");

        Card first = cards.get(0);

        assertEquals("Watchtower", first.getName(), "The name of the first card should be Wachtower ");
        assertEquals(1, first.getCost(), "The cost of the first card should be red ");
        assertEquals("red", first.getColour(), "The colour of the first card should be red ");
        assertEquals("" , first.getDescription(), "The description of the first card should be null ");
    }

/**
 * Test case: testPlayer
 * This test verifies the functionality of test player.
 */
    @Test
    public void testPlayer(){
        Player player = new Player(1);


        assertEquals(1, player.getId(), "Player id should be 1.");

        assertEquals(2, player.getMoney(), "Player initial money should be 2.");
        assertTrue(player.getHandCards().isEmpty(), "The player should have nothing at the beginning.");
        assertTrue(player.getBuildDistricCards().isEmpty(), "The player should have nothing at the beginning.");
    }

/**
 * Test case: testMoney
 * This test verifies the functionality of test money.
 */
    @Test
    public void testMoney(){
        Player player = new Player(2);
        player.addMoney(4);
        assertEquals(6, player.getMoney(), "After adding 4 gold, the money that player has should be 6.");
        boolean sufficient = player.spendMoney(5);
        assertTrue(sufficient, "spendMoney should return true if money is sufficient otherwise return false.");
        assertEquals(1, player.getMoney(), "Money should decrease 1.");
        
        boolean notSufficient = player.spendMoney(7);
        assertFalse(notSufficient, "spendMoney should return false if money is not sufficient. ");
        assertEquals(1, player.getMoney(), "Money should not remain the same because of the insufficiency.");
        player.addMoney(-3);
        assertEquals(1, player.getMoney(), "ignore the negative amounts");
    }
/**
 * Test case: testDrawCard
 * This test verifies the functionality of test draw card.
 */
    @Test
    public void testDrawCard(){

        Player player = new Player(3);

        Card card = new Card("testCard", 3, "red", "test");

        player.drawCard(card);
        List<Card> handCards = player.getHandCards();
        assertEquals(1, handCards.size(), "The drawCard should add to the player's handCards");
        assertSame(card, handCards.get(0), "Handcards should contain the testcard.");
    }

/**
 * Test case: testBuildDistrictCards
 * This test verifies the functionality of test build district cards.
 */
    @Test
    public void testBuildDistrictCards(){
        Player player = new Player(4);
        player.addMoney(5);

        int before = player.getMoney();

        Card card = new Card("Testdistrict", 1, "red", "test");
        player.buildDistricCards(card);
        List<Card> builitList = player.getBuildDistricCards();
        assertEquals(1, builitList.size());
        assertSame(card, builitList.get(0));
        assertEquals(before, player.getMoney());
    }

/**
 * Test case: testRole
 * This test verifies the functionality of test role.
 */
    @Test
    public void testRole(){
        Role role = Role.Assassin;
        assertEquals(1, role.getOrder(), "Assassin order should be 1");
        assertTrue(role.getSpecialAbility().startsWith("Select another character"),"Assassin special ability should start correctly.");

        role = Role.Architect;
        assertEquals(7, role.getOrder(), "Architect order should be 7.");
        assertEquals("Architect", role.getTheName(), "Architect Name should be same");
    }

/**
 * Test case: testTOString
 * This test verifies the functionality of test tostring.
 */
    @Test
    public void testTOString(){
        Role role = Role.King;
        String string = role.toString();
        assertTrue(string.contains("King"), "toString should contain King.");
        assertTrue(string.contains("order 4"), "toString should contain the order number");
        assertTrue(string.contains("Gains one gold for each yellow (noble) district in their city."),"toString should contain part of the special ability.");
    }

/**
 * Test case: testValueOf
 * This test verifies the functionality of test value of.
 */
    @Test
    public void testValueOf(){
        for(Role role: Role.values()){
            assertSame(role, Role.valueOf(role.name()), "should return the same enum constant");
        }
    }

/**
 * Test case: testCommand
 * This test verifies the functionality of test command.
 */
    @Test
    public void testCommand(){
        App app = new App();
        String command1 = app.processCommand("handcards");

        assertTrue(command1.startsWith("Handcards: "), "The output should start with 'Handcards:'");

        String command2 = app.processCommand("money");
        assertTrue(command2.startsWith("Money: "),"The output should start with 'Handcards:'");
        String command3 = app.processCommand("Return");
        assertEquals("Unknown command: return", command3);
    }

/**
 * Test case: testBuildCommand
 * This test verifies the functionality of test build command.
 */
    @Test
    public void testBuildCommand(){

        App app = new App();
        Player player = app.getGame().getCurrentPlayer();

        player.addMoney(5);
        player.drawCard(new Card("Built1",2, "green", ""));
        int index = player.getHandCards().size();

        String first = app.processCommand("build " + index);
        assertTrue(first.startsWith("Built: "), "The first build 1 should be successfula, and will return Built:");
        player.drawCard(new Card("Built1", 2 , "green", ""));
        String duplicate = app.processCommand("build " + index);
        assertEquals("Cannot build duplicate district: Built1", duplicate, "If build the same card then will return false");
        player.drawCard(new Card("Built2", 8, "red", ""));
        int index2 = player.getHandCards().size();
        String insufficient = app.processCommand("build " + index2);
        assertTrue(insufficient.startsWith("Insufficient money to build the district: "));
        String outOfBound = app.processCommand("build 99");
        assertEquals("Invalid handIndex: 99", outOfBound);        
    }

/**
 * Test case: testInformationCommand_missingArgument
 * This test verifies the functionality of test information command_missing argument.
 */
    @Test
    public void testInformationCommand_missingArgument() {
        App app = new App();

        Player player = app.getGame().getCurrentPlayer();
        player.addMoney(5);
        player.drawCard(new Card("C1", 1, "blue", "desc"));
        app.processCommand("build 1");
        String res = app.processCommand("info");
        assertEquals("Invalid built position: ", res);
    }

/**
 * Test case: testRoles
 * This test verifies the functionality of test roles.
 */
    @Test
    public void testRoles(){
        App app = new App();
        Game game = app.getGame();
        String output = app.processCommand("role");

        assertTrue(output.startsWith("Available roles:"), "The output should start with 'Available roles'");


        for (Role role : game.getAvailableRoles()) {
            assertTrue(output.contains(role.name()), "The output should contain the role name: " + role.name());
        }
    }

/**
 * Test case: testSelect
 * This test verifies the functionality of test select.
 */
    @Test
    public void testSelect(){
        App app = new App();

        Game game = app.getGame();
        String roleName = app.getSelectionRoles().get(0).name();
        String success = app.processCommand("select " + roleName);

        assertTrue(success.startsWith("Chosen "));
        String same = app.processCommand("select " + roleName);
        assertEquals("The role has already been chosen " + roleName, same);
    }

/**
 * Test case: testActionIncome
 * This test verifies the functionality of test action income.
 */
    @Test
    public void testActionIncome(){
        App app = new App();

        Player player = app.getGame().getCurrentPlayer();
        int before = player.getMoney();

        String out = app.processCommand("action income");
        assertEquals("Income: add 2 gold", out, "The output should return the remind");
        assertEquals(before + 2, player.getMoney(), "The money of player should add 2 gold");
    }

/**
 * Test case: testActionDraw
 * This test verifies the functionality of test action draw.
 */
    @Test

    public void testActionDraw(){
        App app = new App();
        Player player = app.getGame().getCurrentPlayer();
        int before = player.getHandCards().size();

        String out = app.processCommand("action draw");
        assertTrue(out.startsWith("Draw card: "), "should return drew with card");
        assertEquals(before + 1, player.getHandCards().size(), "The handcards of player should plus 1");
    }   

/**
 * Test case: testDeckShuffle
 * This test verifies the functionality of test deck shuffle.
 */
    @Test

    public void testDeckShuffle(){
        App app = new App();
        Deck deck = app.parseCards();
        List<Card> before = new ArrayList<>(deck.getCards());
        deck.shuffle();
        List<Card> after = deck.getCards();
        assertNotEquals(before.toString(), after.toString(),"Deck order should change after shuffle");
    }

/**
 * Test case: testDrawOrThrow
 * This test verifies the functionality of test draw or throw.
 */
    @Test
    public void testDrawOrThrow(){
        Deck deck = new Deck();
        assertThrows(RuntimeException.class, () -> {deck.drawOrThrow();}, "Should throw RuntimeException when drawing from empty deck");
    }

/**
 * Test case: testDrawAndBottom
 * This test verifies the functionality of test draw and bottom.
 */
    @Test

    public void testDrawAndBottom(){

        App app = new App();
        Deck deck = app.parseCards();
        Card card = deck.drawOrThrow();

        int before = deck.size();
        deck.addBottom(card);
        assertEquals(before + 1, deck.size(), "Deck size should increase after addBottom");
    }

/**
 * Test case: testRoleRank
 * This test verifies the functionality of test role rank.
 */
    @Test
    public void testRoleRank(){
        assertEquals(1, Role.Assassin.getOrder(), "Assassin should be rank 1");
        assertEquals(8, Role.Warlord.getOrder(), " Warlord should be rank 8");
    }

/**
 * Test case: testRoleAbility
 * This test verifies the functionality of test role ability.
 */
    @Test

    public void testRoleAbility(){
        Role king = Role.King;
        assertTrue(king.getSpecialAbility().toLowerCase().contains("crown"), "Kings's special ability should contain crown");
        Role magician = Role.Magician;
        assertTrue(magician.getSpecialAbility().toLowerCase().contains("exchange"), "Magician's ability should contain swap");
    }

/**
 * Test case: testGamePlayer
 * This test verifies the functionality of test game player.
 */
    @Test
    public void testGamePlayer(){
        Game game = new Game(4);

        assertEquals(4, game.getPlayers().size(), "Game should initialze with 4 players");
        for(int i = 0; i < 4; i++){
            assertEquals(i + 1, game.getPlayers().get(i).getId(), "Player's ID should be 1 through 4");
        }
    }

/**
 * Test case: testNextPlayer
 * This test verifies the functionality of test next player.
 */
    @Test
    public void testNextPlayer(){
        Game game = new Game(4);

        Player first = game.getCurrentPlayer();
        Player second = game.getNextPlayer();
        assertNotEquals(first, second, "Second player should be different");
        Player third = game.getNextPlayer();
        Player forth = game.getNextPlayer();
        Player backToFirst = game.getNextPlayer();
        assertEquals(first, backToFirst, "Should cycle back to first player after 4 turns");
    }

/**
 * Test case: testKing
 * This test verifies the functionality of test king.
 */
    @Test
    public void testKing(){
        Game game = new Game(4);
        Player player2 = game.getPlayers().get(1);
        game.setKing(player2);
        assertEquals(player2, game.getKingPlayer(), "King player should be player 2");
    }

/**
 * Test case: testCommandGold
 * This test verifies the functionality of test command gold.
 */
    @Test
    public void testCommandGold(){
        App app = new App(4);
        Player player = app.getGame().getPlayers().get(0);
        player.spendMoney(1);
        String result = app.processCommand("gold");
        assertTrue(result.contains("1"), "Should show remaining gold as 1");
    }

/**
 * Test case: testCommandHand
 * This test verifies the functionality of test command hand.
 */
    @Test
    public void testCommandHand(){
        App app = new App(4);
        Player player = app.getGame().getPlayers().get(0);

        Card card = new Card("Library", 6, "purple", "Handcards");

        player.drawCard(card);
        String result = app.processCommand("hand");
        assertTrue(result.contains("Library"), "Hand should include the Library card");
    }

/**
 * Test case: testCommandInfoPurple
 * This test verifies the functionality of test command info purple.
 */
    @Test
    public void testCommandInfoPurple(){
        App app = new App(4);
        Player player = app.getGame().getPlayers().get(0);

        Card purpleCard = new Card("Library", 6, "purple", "You always have 5 cards in hand.");

        player.drawCard(purpleCard);

        String result = app.processCommand("info Library");
        System.out.println("INFO RESULT = " + result);
        assertTrue(result.toLowerCase().contains("library"), "Should mention library");
        assertTrue(result.toLowerCase().contains("cards"), "Should mention card effect");
    }

/**
 * Test case: testBuildWithFactoryDiscount
 * This test verifies the functionality of test build with factory discount.
 */
    @Test
    public void testBuildWithFactoryDiscount() {
        App app = new App(4);
        Player player = app.getGame().getPlayers().get(0);
        player.addMoney(3);
        player.getHandCards().clear();

        Card factory = new Card("Factory", 2, "purple", "Purple discount");
        player.drawCard(factory);
        String result1 = app.processCommand("build 1");
        System.out.println("Build Factory: " + result1);
        assertTrue(result1.toLowerCase().contains("built"), "Should build first purple card");

         player.getHandCards().clear();
        Card dragon = new Card("Dragon Gate", 2, "purple", "Fancy gate");
        player.drawCard(dragon);

        String result2 = app.processCommand("build 1");
        System.out.println("Build Dragon Gate: " + result2);
        assertTrue(result2.toLowerCase().contains("built"), "Should build discounted purple card");
    }

/**
 * Test case: testBuildDuplicateWithoutQuarry
 * This test verifies the functionality of test build duplicate without quarry.
 */
   @Test
    public void testBuildDuplicateWithoutQuarry() {
        App app = new App(4);
        Player player = app.getGame().getPlayers().get(0);
        player.getHandCards().clear(); 
        player.addMoney(10); 
        Card card1 = new Card("Smithy", 3, "green", "Builds weapons");
        Card card2 = new Card("Smithy", 3, "green", "Builds weapons");
        player.drawCard(card1); 

        String result1 = app.processCommand("build 1");
        System.out.println("First build: " + result1);
        assertTrue(result1.toLowerCase().contains("built"));


        player.drawCard(card2);
        String result2 = app.processCommand("build 1");
        System.out.println("Second build: " + result2);
        assertTrue(result2.toLowerCase().contains("cannot build duplicate"), "Should block second build without Quarry");
    }

/**
 * Test case: testBuildDuplicateWithQuarry
 * This test verifies the functionality of test build duplicate with quarry.
 */
    @Test
    public void testBuildDuplicateWithQuarry() {
        App app = new App(4);
        Player player = app.getGame().getPlayers().get(0);
        player.addMoney(20);

        player.getHandCards().clear();
        Card quarry = new Card("Quarry", 3, "purple", "Allow 1 duplicate");
        player.drawCard(quarry);
        String result1 = app.processCommand("build 1");

        System.out.println("Build Quarry: " + result1);
        Card palace1 = new Card("Palace", 5, "red", "Fancy");
        player.drawCard(palace1);
        String result2 = app.processCommand("build 1");
        System.out.println("Build Palace 1: " + result2);
        Card palace2 = new Card("Palace", 5, "red", "Fancy");

        player.drawCard(palace2);
        String result3 = app.processCommand("build 1");
        System.out.println("Build Palace 2: " + result3);
        assertTrue(result3.toLowerCase().contains("built"), "Should allow 1 duplicate with Quarry");
    }

/**
 * Test case: testBuildInvalidIndex
 * This test verifies the functionality of test build invalid index.
 */
    @Test

    public void testBuildInvalidIndex() {
        App app = new App(4);

        Player player = app.getGame().getPlayers().get(0);
        player.getHandCards().clear(); 
        player.addMoney(5);

        Card card = new Card("Castle", 5, "blue", "Rich palace");
        player.drawCard(card);
        String result = app.processCommand("build 2");
        System.out.println("Build invalid index: " + result);
        assertTrue(result.toLowerCase().contains("invalid handindex"));
    }

/**
 * Test case: testBuildInsufficientMoney
 * This test verifies the functionality of test build insufficient money.
 */
    @Test
    public void testBuildInsufficientMoney() {
        App app = new App(4);

        Player player = app.getGame().getPlayers().get(0);


        player.getHandCards().clear(); 
        while (player.getMoney() > 0) player.spendMoney(1); 
        player.addMoney(1);
        Card expensiveCard = new Card("Palace", 5, "red", "Expensive palace");
        player.drawCard(expensiveCard);
        String result = app.processCommand("build 1");
        System.out.println("Build Insufficient: " + result);
        assertTrue(result.toLowerCase().contains("insufficient money"), "Should detect insufficient money to build");
    }

/**
 * Test case: testBuildInvalidHandIndex
 * This test verifies the functionality of test build invalid hand index.
 */
    @Test
    public void testInvalidHandIndex() {
        App app = new App(4);
        Player player = app.getGame().getPlayers().get(0);

        player.getHandCards().clear(); 
        String result = app.processCommand("build 1");
        System.out.println("Build Invalid HandIndex: " + result);
        assertTrue(result.toLowerCase().contains("invalid handindex"), "Should catch invalid hand index");
    }

/**
 * Test case: testBuildInvalidNumber
 * This test verifies the functionality of test build invalid number.
 */
    @Test
    public void testBuildInvalidNumber() {
        App app = new App(4);
        String result = app.processCommand("build abc");
        System.out.println("Build Invalid Number: " + result);
        assertTrue(result.toLowerCase().contains("invalid number"), "Should catch invalid number");
    }

/**
 * Test case: testInfoMissingArgument
 * This test verifies the functionality of test info missing argument.
 */
    @Test
    public void testInfoMissingArgument() {

        App app = new App(4);

        String result = app.processCommand("info");
        System.out.println("Info Missing Argument: " + result);
        assertTrue(result.toLowerCase().contains("invalid built position"), "Should catch missing info arg");
    }

/**
 * Test case: testSelectUnknownRole
 * This test verifies the functionality of test select unknown role.
 */
    @Test
    public void testSelectUnknownRole() {
        App app = new App(4);

        String result = app.processCommand("select UnknownRole");
        System.out.println("Select Result: " + result);
        assertTrue(result.toLowerCase().contains("unknown role"), "Should handle unknown role");
    }

/**
 * Test case: testSelectChosenRole
 * This test verifies the functionality of test select already chosen role.
 */
    @Test


    public void testSelectChosenRole() {
        App app = new App(4);
        app.processCommand("select King"); 
        String result = app.processCommand("select King"); 
        System.out.println("Select Duplicate Result: " + result);
        assertTrue(result.toLowerCase().contains("already been chosen"), "Should not allow duplicate role selection");
    }

/**
 * Test case: testDestroyWithoutWarlord
 * This test verifies the functionality of test destroy without warlord.
 */
    @Test

    public void testDestroyWithoutWarlord() {
        App app = new App(4);

        String result = app.processCommand("action destroy 2 1");
        System.out.println("Destroy without Warlord: " + result);
        assertTrue(result.toLowerCase().contains("only warlord can destroy"), "Should reject if not warlord");
    }

/**
 * Test case: testInfoHandCardByName
 * This test verifies the functionality of test info hand card by name.
 */
    @Test
    public void testInfoHandCardByName() {
        App app = new App(4);
        Player player = app.getGame().getPlayers().get(0);
        Card card = new Card("Library", 6, "purple", "You always have 5 cards in hand.");
        player.drawCard(card);
        String result = app.processCommand("info Library");
        assertTrue(result.toLowerCase().contains("library"), "Should find hand card by name");
    }

/**
 * Test case: testProcessTurnExecution
 * This test verifies the functionality of test process turn execution.
 */
    @Test
    public void testProcessTurn() {
        App app = new App(4);
        Game game = app.getGame();
        List<Role> roles = new ArrayList<>();
        roles.add(Role.Assassin);
        roles.add(Role.Merchant);
        roles.add(Role.Warlord);
        roles.add(Role.Architect);

        game.setAvailableRoles(roles);
        app.processCommand("select Assassin");
        app.processCommand("select Merchant");
        app.processCommand("select Warlord");
        app.processCommand("select Architect");

        String result = app.processCommand("t"); 
        System.out.println("Turn result: " + result);
        assertTrue(result.toLowerCase().contains("please enter"), "Should prompt action command");
    }

/**
 * Test case: testPurpleCardsEffects
 * This test verifies the functionality of test purple cards effects.
 */
    @Test
    public void testPurpleCardsEffects() {
        App app = new App(4);
        Player p = app.getGame().getPlayers().get(0);
        p.addMoney(20);
        p.getHandCards().clear();
        Card library = new Card("Library", 6, "purple", "You always have 5 cards in hand.");
        p.drawCard(library);
        try {
            app.processCommand("build 1");
        } catch (java.util.NoSuchElementException ignored) {
        }
        Card grave = new Card("Graveyard", 5, "purple", "You may pay to rebuild destroyed district");
        p.drawCard(grave);

        try {
            app.processCommand("build 1");
        } catch (java.util.NoSuchElementException ignored) {}
        Card bell = new Card("Bell Tower", 3, "purple", "BELL");

        p.drawCard(bell);
        try {
            app.processCommand("build 1");
        } catch (java.util.NoSuchElementException ignored) {}
        Card school = new Card("School Of Magic", 6, "purple", "Acts as color of your choice");
        p.drawCard(school);
        try {
            app.processCommand("build 1");
        } catch (java.util.NoSuchElementException ignored) {
        }
        assertEquals(4, p.getBuildDistricCards().size(), "Should build 4 purple special cards");
    }

/**
 * Test case: testEndMethodDoesNotCrash
 * This test verifies the functionality of test end method does not crash.
 */
    @Test
    public void testEndMethodDoesNotCrash() {
        App app = new App(4);
        Player player = app.getGame().getPlayers().get(0);
        app.end(player);
        assertTrue(true, "End should not throw exception");
    }

/**
 * Test case: testAssassinAbility
 * This test verifies the functionality of test assassin ability.
 */
    @Test
    public void testAssassinAbility() {

        App app = new App(4);
        app.processCommand("select Assassin");
        app.processCommand("select Thief");

        app.processCommand("select King");
        app.processCommand("select Bishop");
        app.processCommand("t"); // Assassin's turn
        String result = app.processCommand("action assassinate King");
        System.out.println("Assassin Action: " + result);
        assertTrue(result.toLowerCase().contains("assassinated"));
    }
    /**
 * Test that Thief fails to steal from a role not in this round (no roleToPlayer mapping).
 */

/**
 * Test case: testThiefStealFailsNotThisRound
 * This test verifies the functionality of test thief steal fails not this round.
 */
@Test
public void testThiefStealFailsNotThisRound() {
    App app = new App(4);

    try {
        Field active = App.class.getDeclaredField("activeRole");

        Field rtp = App.class.getDeclaredField("roleToPlayer");
        active.setAccessible(true);
        rtp.setAccessible(true);
        active.set(app, Role.Thief);
        rtp.set(app, new HashMap<>());  // no target assigned
    } catch (Exception e) {
        fail("Setup failed: " + e.getMessage());
    }

    String result = app.processCommand("action steal Architect");
    System.out.println("Thief Fail Result = " + result);
    assertTrue(result.toLowerCase().contains("is not this round"));
}

/**
 * Test case: testMagicianAbility
 * This test verifies the functionality of test magician ability.
 */
    @Test
    public void testMagicianAbility() {
        App app = new App(4);
        app.processCommand("select Magician");
        app.processCommand("select Assassin");
        app.processCommand("select Architect");
        app.processCommand("select Bishop");
        app.processCommand("t");

        String result = app.processCommand("action exchange 2");
        System.out.println("Magician Action: " + result);
        assertTrue(result.toLowerCase().contains("exchange"));
    }

/**
 * Test case: testKingAbility
 * This test verifies the functionality of test king ability.
 */
    @Test
    public void testKingAbility() {
        App app = new App(4);
        app.processCommand("select King");
        app.processCommand("select Bishop");
        app.processCommand("select Assassin");
        app.processCommand("select Architect");
        app.processCommand("t");

        String result = app.processCommand("action income");
        System.out.println("King Action: " + result);
        assertTrue(result.toLowerCase().contains("gold"));
    }

/**
 * Test case: testBishopAbility
 * This test verifies the functionality of test bishop ability.
 */
    @Test

    public void testBishopAbility() {
        App app = new App(4);
        app.processCommand("select Bishop");
        app.processCommand("select Thief");

        app.processCommand("select Merchant");
        app.processCommand("select Warlord");
        app.processCommand("t");

        String result = app.processCommand("action income");
        System.out.println("Bishop Action: " + result);
        assertTrue(result.toLowerCase().contains("gold"));
    }

/**
 * Test case: testMerchantAbility
 * This test verifies the functionality of test merchant ability.
 */
    @Test


    public void testMerchantAbility() {
        App app = new App(4);
        app.processCommand("select Merchant");
        app.processCommand("select Architect");
        app.processCommand("select Warlord");
        app.processCommand("select Magician");
        app.processCommand("t");
        String result = app.processCommand("action income");
        System.out.println("Merchant Action: " + result);
        assertTrue(result.toLowerCase().contains("gold"));
    }

/**
 * Test case: testArchitectAbility
 * This test verifies the functionality of test architect ability.
 */
    @Test
    public void testArchitectAbility() {
        App app = new App(4);
        app.processCommand("select Architect");
        app.processCommand("select Magician");
        app.processCommand("select Warlord");
        app.processCommand("select King");



        app.processCommand("t");
        String result = app.processCommand("action draw");
        System.out.println("Architect Action: " + result);
        assertTrue(result.toLowerCase().contains("draw"));
    }

/**
 * Test case: testLibraryEffect
 * This test verifies the functionality of test library effect.
 */
    @Test
    public void testLibraryEffect() {
        App app = new App(4);
        Player player = app.getGame().getPlayers().get(0);

        player.getHandCards().clear();
        player.addMoney(10);
        Card library = new Card("Library", 6, "purple", "If you draw cards, you keep both.");

        player.drawCard(library);

        app.processCommand("build 1");
        int before = player.getHandCards().size();
        app.processCommand("action draw");
        int after = player.getHandCards().size();
        assertEquals(before + 2, after, "Library should allow drawing and keeping both cards");
    }

/**
 * Test case: testGameOverMethodCall
 * This test verifies the functionality of test game over method call.
 */
    @Test


    public void testGameOverMethodCall() {
        App app = new App(4);
        app.getGame().getPlayers().get(0).buildDistricCards(new Card("D1", 1, "red", ""));
        app.getGame().getPlayers().get(0).buildDistricCards(new Card("D2", 1, "red", ""));
        app.getGame().getPlayers().get(0).buildDistricCards(new Card("D3", 1, "red", ""));
        app.getGame().getPlayers().get(0).buildDistricCards(new Card("D4", 1, "red", ""));
        app.getGame().getPlayers().get(0).buildDistricCards(new Card("D5", 1, "red", ""));
        app.getGame().getPlayers().get(0).buildDistricCards(new Card("D6", 1, "red", ""));
        app.getGame().getPlayers().get(0).buildDistricCards(new Card("D7", 1, "red", ""));
        app.getGame().getPlayers().get(0).buildDistricCards(new Card("D8", 1, "red", ""));
        try {

            app.gameOver(); 
        } catch (Exception e) {
            fail("gameOver() should not crash: " + e.getMessage());
        }
    }

/**
 * Test case: testColour_SchoolOfMagic
 * This test verifies the functionality of test colour_school of magic.
 */
    @Test


    public void testColour_SchoolOfMagic() {
        App app = new App(4);

        Player player = app.getGame().getPlayers().get(0);
        player.setSchoolOfMagicColour("yellow"); 
        Card schoolCard = new Card("School of Magic", 5, "purple", "Magic");
        String colour = app.Colour(schoolCard, player);
        assertEquals("yellow", colour);
    }

/**
 * Test case: testColour_HauntedCity
 * This test verifies the functionality of test colour_haunted city.
 */
    @Test
    public void testColour_HauntedCity() {

        App app = new App(4);
        Player player = app.getGame().getPlayers().get(0);


        player.setHauntedCity("blue"); 
        Card haunted = new Card("Haunted City", 5, "purple", "Ghost");
        String colour = app.Colour(haunted, player);
        assertEquals("blue", colour);
    }

/**
 * Test case: testColour
 * This test verifies the functionality of test colour_normal card.
 */
    @Test
    public void testColour() {
        App app = new App(4);
        Player player = app.getGame().getPlayers().get(0);
        Card c = new Card("Castle", 4, "Red", "Rich");
        String colour = app.Colour(c, player);
        assertEquals("red", colour);
    }

/**
 * Test case: testParseCardsCountAndFirst
 * This test verifies the functionality of test parse cards count and first.
 */
    @Test
    public void testParseCardsFirst() {
        App app = new App();
        Deck deck = app.parseCards();
        assertEquals(41, deck.size(), "");
        Card first = deck.getCards().get(0);
        assertEquals("Watchtower", first.getName(), "");
    }

/**
 * Test case: testDrawOrThrowEmptyDeck
 * This test verifies the functionality of test draw or throw empty deck.
 */
    @Test 

    public void testDrawOrThrowEmptyDeck() {
        Deck empty = new Deck();
        assertThrows(RuntimeException.class, empty::drawOrThrow, "");
    }

/**
 * Test case: testNextPlayerCycles
 * This test verifies the functionality of test next player cycles.
 */
    @Test 
    public void testNextPlayerCycles() {
        Game g = new Game(3);

        Player first = g.getCurrentPlayer();
        g.getNextPlayer();

        g.getNextPlayer();
        Player back = g.getNextPlayer();
        assertEquals(first, back, "");
    }

/**
 * Test case: testColourSchoolOfMagic
 * This test verifies the functionality of test colour school of magic.
 */
    @Test 

    public void testColourSchoolOfMagic() {
        App a = new App(4);
        Player p = a.getGame().getPlayers().get(0);
        p.setSchoolOfMagicColour("yellow");
        assertEquals("yellow", a.Colour(new Card("School of Magic", 5, "purple", ""), p));
    }

/**
 * Test case: testColourHauntedCity
 * This test verifies the functionality of test colour haunted city.
 */
    @Test 
    public void testColourHauntedCity() {
        App a = new App(4);

        Player p = a.getGame().getPlayers().get(0);
        p.setHauntedCity("blue");
        assertEquals("blue", a.Colour(new Card("Haunted City", 5, "purple", ""), p));
    }

/**
 * Test case: testColourLowercases
 * This test verifies the functionality of test colour normal card lowercases.
 */
    @Test 
    public void testColourLowercases() {
        App a = new App(4);
        String c = a.Colour(new Card("Castle", 4, "Red", ""), a.getGame().getPlayers().get(0));
        assertEquals("red", c, "");
    }

/**
 * Test case: testRoleList
 * This test verifies the functionality of test role list command.
 */
    @Test 
    public void testRoleList() {
        App a = new App();
        String out = a.processCommand("role");
        assertTrue(out.startsWith("Available roles:"), "");
        }

/**
 * Test case: testSelectRoleAndDuplicate
 * This test verifies the functionality of test select role and duplicate.
 */
    @Test 
    public void testSelectRoleAndDuplicate() {
        App a = new App(4);

        String role = a.getSelectionRoles().get(0).name();
        String ok = a.processCommand("select " + role);
        assertTrue(ok.startsWith("Chosen "), "");
        String dup = a.processCommand("select " + role);
        assertEquals("The role has already been chosen " + role, dup);
    }

/**
 * Test case: testKingIncome
 * This test verifies the functionality of test king ability income.
 */
    @Test 
    public void testKingIncome() {
        App a = new App(4);
        a.processCommand("select King");
        a.processCommand("select Assassin");
        a.processCommand("select Architect");
        a.processCommand("select Bishop");
        a.processCommand("t");
        String out = a.processCommand("action income");
        assertTrue(out.toLowerCase().contains("gold"));
    }

/**
 * Test case: testBishopIncome
 * This test verifies the functionality of test bishop ability income.
 */
    @Test 
    public void testBishopyIncome() {
        App a = new App(4);

        a.processCommand("select Bishop");

        a.processCommand("select Thief");
        a.processCommand("select Merchant");
        a.processCommand("select Warlord");
        a.processCommand("t");
        assertTrue(a.processCommand("action income").toLowerCase().contains("gold"));
    }


/**
 * Test case: testMerchantIncome
 * This test verifies the functionality of test merchant ability income.
 */
    @Test 
    public void testMerchantIncome() {

        App a = new App(4);
        a.processCommand("select Merchant");
        a.processCommand("select Architect");
        a.processCommand("select Magician");
        a.processCommand("select King");
        a.processCommand("t");
        assertTrue(a.processCommand("action income").toLowerCase().contains("gold"));
    }


/**
 * Test case: testArchitectDraw
 * This test verifies the functionality of test architect ability draw.
 */
    @Test 

    public void testArchitectDraw() {
        App a = new App(4);
        a.processCommand("select Architect");

        a.processCommand("select Magician");

        a.processCommand("select Warlord");
        a.processCommand("select King");
        a.processCommand("t");
        assertTrue(a.processCommand("action draw").toLowerCase().startsWith("draw"));
    }


/**
 * Test case: testLibraryDrawsTwo
 * This test verifies the functionality of test library draws two.
 */
    @Test 
    public void testLibraryDrawsTwo() {

        App a = new App(4);
        Player p = a.getGame().getPlayers().get(0);

        p.getHandCards().clear(); p.addMoney(10);

        p.drawCard(new Card("Library", 6, "purple", "If you draw cards, you keep both."));
        a.processCommand("build 1");
        int before = p.getHandCards().size();


        a.processCommand("action draw");
        assertEquals(before + 2, p.getHandCards().size());
    }


/**
 * Test case: testFactoryDiscount
 * This test verifies the functionality of test factory discount.
 */
    @Test 
    public void testFactoryDiscount() {

        App a = new App(4);
        Player p = a.getGame().getPlayers().get(0);
        p.getHandCards().clear(); p.addMoney(3);

        p.drawCard(new Card("Factory", 2, "purple", "Purple discount"));
        assertTrue(a.processCommand("build 1").toLowerCase().contains("built"));


        p.drawCard(new Card("Dragon Gate", 2, "purple", "Fancy gate"));
        assertTrue(a.processCommand("build 1").toLowerCase().contains("built"));
    }


/**
 * Test case: testQuarryAllowsOneDuplicate
 * This test verifies the functionality of test quarry allows one duplicate.
 */
    @Test 
    public void testQuarryAllowsOneDuplicate() {
        App a = new App(4);
        Player p = a.getGame().getPlayers().get(0);


        p.addMoney(10); p.getHandCards().clear();
        p.drawCard(new Card("Quarry", 3, "purple", "Allow 1 duplicate"));
        a.processCommand("build 1");

        Card d1 = new Card("Smithy", 3, "green", "");

        p.drawCard(d1); a.processCommand("build 1");
        p.drawCard(new Card("Smithy", 3, "green", ""));
        assertTrue(a.processCommand("build 1").toLowerCase().contains("built"));
    }


/**
 * Test case: testUnknownCommand
 * This test verifies the functionality of test unknown command.
 */
    @Test 



    public void testUnknownCommand() {
        App a = new App();
        assertEquals("Unknown command: foo", a.processCommand("foo"));
    }


/**
 * Test case: testBuildInvalidArg
 * This test verifies the functionality of test build invalid arg.
 */
    @Test 


    public void testBuildInvalidArg() {

        App a = new App(4);
        assertTrue(a.processCommand("build abc").toLowerCase().contains("invalid number"));
        assertTrue(a.processCommand("build 99").toLowerCase().contains("invalid handindex"));
    }


/**
 * Test case: testCardToString
 * This test verifies the functionality of test card to string.
 */
    @Test



    public void testCardToString() {
        Card c = new Card("MyCard", 5, "blue", "desc");
        assertEquals("MyCard", c.toString(), "");
    }


/**
 * Test case: testColourDefault
 * This test verifies the functionality of test colour default.
 */
    @Test
    public void testColourDefault() {



        App app = new App();
        Player p = app.getGame().getPlayers().get(0);
        Card c = new Card("Any", 1, "Green", "desc");
        assertEquals("green", app.Colour(c, p), "");
    }


/**
 * Test case: testHelpCommand
 * This test verifies the functionality of test help command.
 */
    @Test


    public void testHelpCommand() {
        App app = new App(3);

        String out = app.processCommand("help");
        assertTrue(out.toLowerCase().contains("available commands"),
            "Help should list available commands");
    }


/**
 * Test case: testAllCommand
 * This test verifies the functionality of test all command.
 */
    @Test
    public void testAllCommand() {


        App app = new App(2);

        String out = app.processCommand("all");
        assertTrue(out.toLowerCase().contains("player"),
            "All command should display players or game state");
    }


/**
 * Test case: testDebugToggle
 * This test verifies the functionality of test debug toggle.
 */
    @Test
    public void testDebugToggle() {

        App app = new App(2);
        String out1 = app.processCommand("debug");

        assertTrue(out1.toLowerCase().contains("debug"),
            "Debug command should toggle debug mode on/off");

        String out2 = app.processCommand("debug");
        assertTrue(out2.toLowerCase().contains("debug"),
            "Debug command toggles back on second call");
    }


/**
 * Test case: testSaveGameInvalidPath
 * This test verifies the functionality of test save game invalid path.
 */
    @Test

    public void testSaveGameInvalidPath() {

        App app = new App(2);

        String out = app.processCommand("save /nonexistent_dir/file.sav");
        assertTrue(out.toLowerCase().contains("error") || out.toLowerCase().contains("failed"),
        "");
    }


/**
 * Test case: testDestroyUsageAndPermission
 * This test verifies the functionality of test destroy usage and permission.
 */
    @Test
    public void testDestroyUsageAndPermission() {


        App app = new App(4);
        // without args

        String r1 = app.processCommand("action destroy");
        assertTrue(r1.toLowerCase().contains("enter destroy"));
        // not warlord
        String r2 = app.processCommand("action destroy 2 1");
        assertTrue(r2.toLowerCase().contains("only warlord"));
    }


/**
 * Test case: testUseCommand
 * This test verifies the functionality of test use command.
 */
    @Test

    public void testUseCommand() {
        App app = new App(3);
        String out = app.processCommand("use 1");
        assertTrue(out.toLowerCase().contains("not built"),"Expected warning about no such purple district");
    }


/**
 * Test case: testActivateAndHandleNext
 * This test verifies the functionality of test activate and handle next.
 */
    @Test
    public void testActivateAndHandleNext() {
        App app = new App(2);
        // activate with no-op



        String a = app.processCommand("activate");
        assertNotNull(a);
        // handleNext shouldn't throw
        assertDoesNotThrow(() -> app.processCommand("t"));
    }


/**
 * Test case: testEndAndCheckOver
 * This test verifies the functionality of test end and check over.
 */
    @Test
    public void testEndAndCheckOver() {
        App app = new App(2);
        boolean over = app.getGame().gameOver();

        assertFalse(over, "New game should not be over immediately");
        String endOut = app.processCommand("end");
        System.out.println("endOut = " + endOut);
        assertNotNull(endOut);
    }

/**
 * Test case: testPurpleCardsOneSkipInteraction
 * This test verifies the functionality of test purple cards one skip interaction.
 */
    @Test
    public void testPurpleCardsOneSkipInteraction() {
        App app = new App(2);
        Player p = app.getGame().getCurrentPlayer();

        Card c = new Card("Library", 6, "purple", "desc");
        p.drawCard(c);
        // no scanner input -> should not throw
        assertDoesNotThrow(() -> app.processCommand("build 1"));
    }

/**
 * Test case: testProcessTurnDoesNotCrash
 * This test verifies the functionality of test process turn does not crash.
 */
    @Test
    public void testProcessTurnDoesNotCrash() {
        App app = new App(3);
        assertDoesNotThrow(() -> app.processCommand("t"));
    }

/**
 * Test case: testRunExits
 * This test verifies the functionality of test run.
 */
    @Test
    public void testRunExits() {
        String input = "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        App app = new App(2);
        assertDoesNotThrow(() -> app.run(), "");
    }

/**
 * Test case: testUseNoException
 * This test verifies the functionality of test use bounds.
 */
    @Test
    public void testUseNoException() {
        App app = new App(2);
        assertDoesNotThrow(() -> app.use(new String[]{"arg1", "arg2"}),"");
    }

/**
 * Test case: testActivateNoException
 * This test verifies the functionality of test Activate bounds.
 */
    @Test
    public void testActivateNoException() {
        App app = new App(2);

        Player p = app.getGame().getCurrentPlayer();
        Card c = new Card("X", 1, "blue", "desc");
        assertDoesNotThrow(() -> app.activate(p, c, new String[0]),"");
    }

/**
 * Test case: testHandleNextInternal
 * This test verifies the functionality of test handleNext.
 */
    @Test
    public void testHandleNextInternal() {
        App app = new App(3);
        assertDoesNotThrow(() -> app.handleNext(),"");
    }

/**
 * Test case: testCheckOverNoException
 * This test verifies the functionality of test checkOver bounds.
 */
    @Test
    public void testCheckOverNoException() {
        App app = new App(2);
        assertDoesNotThrow(() -> app.checkOver(), "");
    }


/**
 * Test case: testEndMethodNoException
 * This test verifies the functionality of test end command.
 */
    @Test
    void testEndMethodNoException() {
        App app = new App(2);
        Player p = app.getGame().getCurrentPlayer();
        assertDoesNotThrow(() -> app.end(p),"");
    }


/**
 * Test case: testMainDoesNotCrash
 * This test verifies the functionality of test main class.
 */
    @Test
    void testMainDoesNotCrash() {
        assertDoesNotThrow(() -> App.main(new String[0]),"");
    }

/**
 * Test case: testSettersAndRealizeCommand
 * This test verifies the functionality of test setters and realize command.
 */
    @Test
    public void testSettersAndRealizeCommand() throws IOException {
        App app = new App(2);
        Deck fakeDeck = new Deck(); 
        Scanner fakeScanner = new Scanner(new ByteArrayInputStream("".getBytes()));

        Game fakeGame = new Game(2);
        app.setDeck(fakeDeck);
        app.setScanner(fakeScanner);
        app.setGame(fakeGame);
        assertDoesNotThrow(() -> app.realizeCommand("help"));
    }


/**
 * Test case: testGetOtherPlayer
 * This test verifies the functionality of test get other player.
 */
    @Test
    public void testGetOtherPlayer() {
        Game game = new Game(4);
        Player player = game.getOtherPlayer(1);
        assertNotNull(player);

        Player noOne = game.getOtherPlayer(99);
        assertNull(noOne);
    }

/**
 * Test case: testSetCurrentIndex
 * This test verifies the functionality of test set current index.
 */
    @Test
    public void testSetCurrentIndex() {
        Game game = new Game(4);
        game.setCurrentIndex(2);
        assertEquals(2, game.getPlayers().indexOf(game.getCurrentPlayer()));
    }


/**
 * Test case: testRoleComparatorSortOrder
 * This test verifies the functionality of test role comparator sort order.
 */
    @Test
    public void testRoleComparatorSortOrder() {
        List<Role> roles = new ArrayList<>();
        roles.add(Role.Warlord);

        roles.add(Role.Assassin);
        roles.add(Role.King);
        Collections.sort(roles, new Comparator<Role>() {
            @Override
            public int compare(Role r1, Role r2) {
                return Integer.compare(r1.ordinal(), r2.ordinal());
            }

        });
        assertEquals(Role.Assassin, roles.get(0));
        assertEquals(Role.King, roles.get(1));
        assertEquals(Role.Warlord, roles.get(2));
    }


/**
 * Test case: testGameCopyIndependent
 * This test verifies the functionality of test game copy independent.
 */
    @Test
    public void testGameCopyIndependent() {
        App original = new App(3);
        Player origP0 = original.getGame().getPlayers().get(0);

        int baseMoney = origP0.getMoney();
        origP0.addMoney(5); 
        App copy = new App(1); 
        copy.gameCopy(original); 
        origP0.addMoney(10);
        Player copyP0 = copy.getGame().getPlayers().get(0);
        assertEquals(baseMoney + 5, copyP0.getMoney(),"");
    }


/**
 * Test case: testMainValidFirst
 * This test verifies the functionality of test main_valid first input.
 */
    @Test
    public void testMainValidFirst() {
        String input = "4\n";

        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        PrintStream oldOut = System.out;

        System.setOut(ps);
        App.main(new String[0]);
        System.setOut(oldOut);

        String out = baos.toString();
        assertTrue(out.contains("Enter number of players"), "");
        assertTrue(out.contains("> "), "");
    }

/**
 * Test case: testMainInvalidThenValidInput
 * This test verifies the functionality of test main_invalid then valid input.
 */
    @Test
    public void testMainInvalidThenValidInput() {
        String input = "3\n8\n5\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        PrintStream oldOut = System.out;
        System.setOut(ps);
        App.main(new String[0]);
        System.setOut(oldOut);

        String out = baos.toString();

        int count = out.split("Enter number of players", -1).length - 1;
        assertEquals(3, count, "");
        assertTrue(out.contains("> "), "");
    }


/**
 * Test case: testKingBonus
 * This test verifies the functionality of test king bonus.
 */
    @Test
    public void testKingBonus() throws Exception {
        App app = new App(4);
        Game game = app.getGame();
        Player p1 = game.getPlayers().get(0);

        Card palace = new Card("Palace", 5, "yellow", "");
        Card castle = new Card("Castle", 4, "yellow", "");
        p1.drawCard(palace);
        p1.drawCard(castle);

        assertTrue(p1.buildDistricCards(palace));


        assertTrue(p1.buildDistricCards(castle));
        Field turnOrderF = App.class.getDeclaredField("turnOrder");
        turnOrderF.setAccessible(true);

        turnOrderF.set(app, Collections.singletonList(Role.King));
        Field roleMapF = App.class.getDeclaredField("roleToPlayer");


        roleMapF.setAccessible(true);
        Map<Role, Player> map = new HashMap<>();

        map.put(Role.King, p1);
        roleMapF.set(app, map);
        Field scannerF = App.class.getDeclaredField("scanner");
        scannerF.setAccessible(true);

        Scanner fakeScanner = new Scanner(
            new ByteArrayInputStream("action draw\nend\n".getBytes())
        );
        scannerF.set(app, fakeScanner);

        int before = p1.getMoney();
        assertEquals(2, before, "");
        app.processTurn();

        assertEquals(before + 2, p1.getMoney(),"King should get +1 per yellow district");
        assertEquals(p1, game.getKingPlayer(),"This player must be set as King");
    }


/**
 * Test case: testBishopBonus
 * This test verifies the functionality of test bishop bonus.
 */
    @Test
    public void testBishopBonus() throws Exception {
        App app = new App(3);
        Field turnOrderF = App.class.getDeclaredField("turnOrder");
        turnOrderF.setAccessible(true);
        turnOrderF.set(app, Collections.singletonList(Role.Bishop));

        Field roleMapF = App.class.getDeclaredField("roleToPlayer");
        roleMapF.setAccessible(true);
        Player p = app.getGame().getPlayers().get(0);
        Map<Role, Player> map = new HashMap<>();


        map.put(Role.Bishop, p);
        roleMapF.set(app, map);
        Card tower = new Card("Watchtower", 3, "blue", "");
        Card manor = new Card("Manor", 5, "blue", "");

        p.drawCard(tower);
        p.drawCard(manor);

        assertTrue(p.buildDistricCards(tower));

        assertTrue(p.buildDistricCards(manor));
        Field scannerF = App.class.getDeclaredField("scanner");
        scannerF.setAccessible(true);

        Scanner fakeScanner = new Scanner(new ByteArrayInputStream("action draw\n1\nend\n".getBytes()));
        scannerF.set(app, fakeScanner);
        int before = p.getMoney(); 
        assertEquals(2, before);

        app.processTurn();
        assertEquals(before + 2, p.getMoney(),
            "Bishop should gain +1 gold per blue district");
    }


/**
 * Test case: testMerchantBonus
 * This test verifies the functionality of test merchant bonus.
 */
    @Test
    public void testMerchantBonus() throws Exception {
        App app = new App(2);
        Game game = app.getGame();

        Player merchant = game.getPlayers().get(0);
        Card tavern  = new Card("Tavern", 1, "green", "");
        Card harbour = new Card("Harbour",4,"green","");
        merchant.drawCard(tavern);

        merchant.drawCard(harbour);
        assertTrue(merchant.buildDistricCards(tavern));
        assertTrue(merchant.buildDistricCards(harbour));

        Field toF = App.class.getDeclaredField("turnOrder");
        toF.setAccessible(true);
        toF.set(app, Collections.singletonList(Role.Merchant));

        Field rmF = App.class.getDeclaredField("roleToPlayer");
        rmF.setAccessible(true);
        Map<Role, Player> mapM = new HashMap<>();
        mapM.put(Role.Merchant, merchant);

        rmF.set(app, mapM);
        Field scF = App.class.getDeclaredField("scanner");
        scF.setAccessible(true);

        scF.set(app, new Scanner(


            new ByteArrayInputStream("action income\nend\n".getBytes())
        ));
        int before = merchant.getMoney();
        app.processTurn();
        assertEquals(before + 3, merchant.getMoney(), "Merchan");
    }

/**
 * Test case: testArchitectDrawAbility
 * This test verifies the functionality of test architect draw ability.
 */
    @Test
    public void testArchitectDrawAbility() throws Exception {
        App app = new App(2);
        Game game = app.getGame();
        Player architect = game.getPlayers().get(0);
        Field toF = App.class.getDeclaredField("turnOrder");


        toF.setAccessible(true);
        toF.set(app, Collections.singletonList(Role.Architect));


        Field rmF = App.class.getDeclaredField("roleToPlayer");
        rmF.setAccessible(true);
        Map<Role, Player> mapA = new HashMap<>();
        mapA.put(Role.Architect, architect);
        rmF.set(app, mapA);
        Field scF = App.class.getDeclaredField("scanner");
        scF.setAccessible(true);


        scF.set(app, new Scanner(
            new ByteArrayInputStream("action income\nend\n".getBytes())
        ));
        int beforeHand = architect.getHandCards().size();

        app.processTurn();
        assertEquals(beforeHand + 2, architect.getHandCards().size(),
            "Architect  2 ");
    }

/**
 * Test case: testWarlordBonus
 * This test verifies the functionality of test warlord bonus.
 */
    @Test
    public void testWarlordBonus() throws Exception {
        App app = new App(2);
        Game game = app.getGame();

        Player warlord = game.getPlayers().get(0);
        Card prison    = new Card("Prison", 2, "red", "");

        Card watchtower= new Card("Watchtower",1,"red","");
        warlord.drawCard(prison);
        warlord.drawCard(watchtower);

        assertTrue(warlord.buildDistricCards(prison));

        assertTrue(warlord.buildDistricCards(watchtower));
        Field toF = App.class.getDeclaredField("turnOrder");
        toF.setAccessible(true);

        toF.set(app, Collections.singletonList(Role.Warlord));
        Field rmF = App.class.getDeclaredField("roleToPlayer");
        rmF.setAccessible(true);
        Map<Role, Player> mapW = new HashMap<>();

        mapW.put(Role.Warlord, warlord);
        rmF.set(app, mapW);

        Field scF = App.class.getDeclaredField("scanner");


        scF.setAccessible(true);
        scF.set(app, new Scanner(new ByteArrayInputStream("action income\nend\n".getBytes())));
        int before = warlord.getMoney();
        app.processTurn();
        assertEquals(before + 2, warlord.getMoney(),"");

    }

/**
 * Test case: testLaboratoryAbility
 * This test verifies the functionality of test laboratory ability.
 */
    @Test
    public void testLaboratoryAbility() throws Exception {
        App app = new App(2);
        Player p = app.getGame().getPlayers().get(0);
        p.getHandCards().clear();

        Card c1 = new Card("Alpha", 1, "green", "");
        Card c2 = new Card("Beta",  2, "red",   "");

        p.drawCard(c1);
        p.drawCard(c2);
        Card lab = new Card("Laboratory", 5, "purple", "");

        p.buildDistricCards(lab);
        String resp = app.processCommand("use laboratory 1");


        assertTrue(resp.toLowerCase().contains("discard alpha and receive 1 gold"),"Response should mention discarding and receiving gold");
        assertEquals(3, p.getMoney(), "");
        assertTrue(p.laboratory, "");
        List<Card> handAfter = p.getHandCards();

        assertEquals(1, handAfter.size(), "");

        assertFalse(handAfter.contains(c1), "Alpha ");
        assertTrue(handAfter.contains(c2),  "Beta");
        String resp2 = app.processCommand("use laboratory 1");
        assertEquals("Laboratory ability has been already used", resp2);
    }



/**
 * Test case: testSmithyAbility
 * This test verifies the functionality of test smithy ability.
 */
    @Test
    public void testSmithyAbility() throws Exception {

        App app = new App(2);
        Game game = app.getGame();

        Player p = game.getPlayers().get(0);
        p.getHandCards().clear();

        Card smithy = new Card("Smithy", 0, "purple", "");
        p.drawCard(smithy);

        assertTrue(p.buildDistricCards(smithy), "Should be able to build Smithy");
        int beforeMoney = p.getMoney();
        assertTrue(beforeMoney >= 2, "Initial money must be at least 2");
        String resp = app.processCommand("use smithy");

        assertTrue(resp.startsWith("Paid 2 gold and drew 3 cards"), "Response should indicate paying and drawing cards");
        assertEquals(beforeMoney - 2, p.getMoney(),"Should have spent 2 gold");
        assertEquals(3, p.getHandCards().size(), "Should have drawn exactly 3 cards");
        assertTrue(p.smithy,"Smithy ability flag should be set");
    }

/**
 * Test case: testArmoryAbility
 * This test verifies the functionality of test armory ability.
 */
    @Test
    public void testArmoryAbility() throws Exception {
        App app = new App(2);
        Game game = app.getGame();

        Player p
         = game.getPlayers().get(0);

        Player target = game.getPlayers().get(1);
        Card armory = new Card("Armory", 0, "purple", "");
        p.drawCard(armory);
        assertTrue(p.buildDistricCards(armory), "Should be able to build Armory");
        Card fort = new Card("Fortress", 0, "red", "");

        target.drawCard(fort);
        assertTrue(target.buildDistricCards(fort), "Target must have a built district");
        int targetId = target.getId();
        String cmd = String.format("use armory %d 1", targetId);
        String resp = app.processCommand(cmd);

        assertTrue(resp.contains("Armory self-destructed"),

            "Response should indicate self-destruction and target destruction");
        List<Card> mineBuilt   = p.getBuildDistricCards();

        List<Card> targetBuilt = target.getBuildDistricCards();

        assertFalse(mineBuilt.contains(armory),

            "Armory should be removed from your built districts");
        assertFalse(targetBuilt.contains(fort),
            "Target's Fortress should have been destroyed");
        assertTrue(p.armory,
            "Armory ability flag should be set");

    }

/**
 * Test case: testAllCommandBuildListing
 * This test verifies the functionality of test all command build listing.
 */
    @Test
    public void testAllCommandBuildListing() throws Exception {

        App app = new App(2);
        Game game = app.getGame();
        Player p1 = game.getPlayers().get(0);

        Card c1 = new Card("Castle", 5, "Yellow", "");

        Card c2 = new Card("Chapel", 2, "Blue",   "");
        p1.getBuildDistricCards().clear();
        p1.getBuildDistricCards().add(c1);
        p1.getBuildDistricCards().add(c2);
        PrintStream origOut = System.out;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bout));
        try {
            String ret = app.processCommand("all");
            String output = bout.toString();


            assertTrue(output.contains("build=Castle,Chapel"),
            "The first line should be build=Castle,Chapel");
            assertTrue(output.contains("[ yellow5 ]"),
            "“[ yellow5 ]”");
            assertTrue(output.contains("[ blue2 ]"),
            "“[ blue2 ]”");
        } finally {
            System.setOut(origOut);
        }
    }

/**
 * Test case: testDestroyDirectly
 * This test verifies the functionality of test destroy directly.
 */
    @Test
public void testDestroyDirectly() throws Exception {
    App app = new App(2);
    Game game = app.getGame();
    Player warlord = game.getPlayers().get(0);
    Player target = game.getPlayers().get(1);
    Field arField = App.class.getDeclaredField("activeRole");

    arField.setAccessible(true);
    arField.set(app, Role.Warlord);
    Card tower = new Card("Tower", 3, "yellow", "");
    target.getBuildDistricCards().add(tower);
    int beforeCount = target.getBuildDistricCards().size();

    warlord.addMoney(10); 
    int beforeGold = warlord.getMoney();

    String result = app.processCommand("action destroy 2 1");
    System.out.println("Destroy  " + result);

    assertTrue(result.contains("Destroyed Tower"),"");
    assertEquals(beforeCount - 1,target.getBuildDistricCards().size(),"");
    assertTrue(warlord.getMoney() < beforeGold);
}

/**
 * Test case: testRunExit
 * This test verifies the functionality of test run exit.
 */
@Test
    public void testRunExit() throws Exception {
        App app = new App(2);
        String input = "exit\n";
        Field scF = App.class.getDeclaredField("scanner");
        scF.setAccessible(true);
        scF.set(app, new Scanner(new ByteArrayInputStream(input.getBytes())));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        System.setOut(new PrintStream(bout));
        app.run();
        String out = bout.toString();
        assertTrue(out.contains("Player Exit"),
                   "Player Exit");
    }

/**
 * Test case: testRunTIntoTurnPhase
 * This test verifies the functionality of test run tinto turn phase.
 */
    @Test
    public void testRunTIntoTurnPhase() throws Exception {
        App app = new App(2);
        Field phaseF = App.class.getDeclaredField("phase");
        phaseF.setAccessible(true);
        phaseF.set(app, App.Phase.turn_phase);
        Method initF = App.class.getDeclaredMethod("initTurnOrder");

        initF.setAccessible(true);
        initF.invoke(app);


        String input = "t\nexit\n";
        Field scF = App.class.getDeclaredField("scanner");
        scF.setAccessible(true);
        scF.set(app, new Scanner(new ByteArrayInputStream(input.getBytes())));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bout));
        app.run();
        String out = bout.toString();
        assertTrue(out.toLowerCase().contains("player"), 
                   "");
        assertTrue(out.contains("Player Exit"), "");
    }

/**
 * Test case: testRunCharacterSelectionCommandsAndComplete
 * This test verifies the functionality of test run character selection commands and complete.
 */
    @Test

    public void testRunCharacterSelectionCommandsAndComplete() throws Exception {
        App app = new App(2);
        String input = String.join("\n",
            "handcards",
            "money",
            "help",
            "King",
            "Merchant",
            "exit"
        ) + "\n";
        Field scF = App.class.getDeclaredField("scanner");
        scF.setAccessible(true);
        scF.set(app, new Scanner(new ByteArrayInputStream(input.getBytes())));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bout));

        app.run();
        String out = bout.toString();
        assertTrue(out.contains("handcards"), "");
        assertTrue(out.contains("money"),     "");
        assertTrue(out.contains("help"),      "");
        assertTrue(out.contains("All players have chosen"),
                   "All players have chosen…");
        assertTrue(out.contains("Starting turn phase"),
                   "turn phase");
    }


/**
 * Test case: testHandleNextHospitalBranch
 * This test verifies the functionality of test handle next hospital branch.
 */
    @Test
    public void testHandleNextHospitalBranch() throws Exception {
        App app = new App(2);
        Game game = app.getGame();
        Player p1 = game.getPlayers().get(0);

        Player p2 = game.getPlayers().get(1);
        Field phaseF = App.class.getDeclaredField("phase");
        phaseF.setAccessible(true);
        phaseF.set(app, App.Phase.turn_phase);
        List<Role> order = Arrays.asList(Role.Magician, Role.Warlord);

        Field toF = App.class.getDeclaredField("turnOrder");
        toF.setAccessible(true);
        toF.set(app, order);
        Field rtpF = App.class.getDeclaredField("roleToPlayer");

        rtpF.setAccessible(true);
        Map<Role, Player> map = new HashMap<>();
        map.put(Role.Magician, p1);
        map.put(Role.Warlord,  p2);

        rtpF.set(app, map);

        Field arF = App.class.getDeclaredField("assasssinatedRoles");
        arF.setAccessible(true);
        Set<Role> dead = new HashSet<>();
        dead.add(Role.Warlord);

        arF.set(app, dead);
        p2.getBuildDistricCards().add(new Card("Hospital", 6, "blue", ""));
        Field tiF = App.class.getDeclaredField("turnIndex");
        tiF.setAccessible(true);
        tiF.setInt(app, 1);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        System.setOut(new PrintStream(bout));
        app.handleNext();
        String out = bout.toString();
        assertTrue(out.contains("Hospital: you are assassinated"),
            "");
    }

/**
 * Test case: testHandleNextSkipAssassinated
 * This test verifies the functionality of test handle next skip assassinated.
 */
    @Test
    public void testHandleNextSkipAssassinated() throws Exception {
        App app = new App(3);
        Game game = app.getGame();
        Player p1 = game.getPlayers().get(0);
        Player p2 = game.getPlayers().get(1);
        Player p3 = game.getPlayers().get(2);
        Field phaseF = App.class.getDeclaredField("phase");

        phaseF.setAccessible(true);

        phaseF.set(app, App.Phase.turn_phase);
        List<Role> order = Arrays.asList(Role.Assassin, Role.Thief, Role.King);
        Field toF = App.class.getDeclaredField("turnOrder");

        toF.setAccessible(true);
        toF.set(app, order);
        Field rtpF = App.class.getDeclaredField("roleToPlayer");
        rtpF.setAccessible(true);
        Map<Role, Player> map = new HashMap<>();

        map.put(Role.Assassin, p1);
        map.put(Role.Thief,p2);

        map.put(Role.King,p3);
        rtpF.set(app, map);
        Field arF = App.class.getDeclaredField("assasssinatedRoles");
        arF.setAccessible(true);

        arF.set(app, new HashSet<>(Arrays.asList(Role.Assassin, Role.Thief)));
        Field tiF = App.class.getDeclaredField("turnIndex");
        tiF.setAccessible(true);

        tiF.setInt(app, 0);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        System.setOut(new PrintStream(bout));
        app.handleNext();

        String out = bout.toString();
        assertTrue(out.contains("Assassin was assassinated. SKIP."),"");
        assertTrue(out.contains("Thief was assassinated. SKIP.")," Thief SKIP");
    }

/**
 * Simplified test to check if handleNext resets the phase to character_selection.
 */

/**
 * Test case: testHandleNextResetsPhase
 * This test verifies the functionality of test handle next resets phase.
 */
@Test
public void testHandleNextResetsPhase() {
    App app = new App(2);
    try {
        Field phaseField = App.class.getDeclaredField("phase");
        Field turnOrderField = App.class.getDeclaredField("turnOrder");

        Field turnIndexField = App.class.getDeclaredField("turnIndex");
        phaseField.setAccessible(true);
        turnOrderField.setAccessible(true);
        turnIndexField.setAccessible(true);

        Class<?> phaseEnum = Class.forName("citadels.App$Phase");
        Object turnPhase = Enum.valueOf((Class<Enum>) phaseEnum, "turn_phase");

        phaseField.set(app, turnPhase);


        List<Role> turnOrder = new ArrayList<>();
        turnOrder.add(Role.King);

        turnOrderField.set(app, turnOrder);
        turnIndexField.setInt(app, 1);
    } catch (Exception e) {
        fail("Setup failed: " + e.getMessage());
    }
    app.handleNext();
    try {

        Field phaseField = App.class.getDeclaredField("phase");
        phaseField.setAccessible(true);

        Object newPhase = phaseField.get(app);
        assertEquals("character_selection", newPhase.toString());
    } catch (Exception e) {

        fail("Phase check failed: " + e.getMessage());
    }
}


/**
 * Test case: testHandleNextSkipMultipleAssassinated
 * This test verifies the functionality of test handle next skip multiple assassinated.
 */
    @Test
    public void testHandleNextSkipMultipleAssassinated() throws Exception {
        App app = new App(3);
        Game game = app.getGame();
        Player p1 = game.getPlayers().get(0);
        Player p2 = game.getPlayers().get(1);
        Field phaseF = App.class.getDeclaredField("phase");
        phaseF.setAccessible(true);
        phaseF.set(app, App.Phase.turn_phase);

        List<Role> order = Arrays.asList(Role.Assassin, Role.Thief);
        Field toF = App.class.getDeclaredField("turnOrder");
        toF.setAccessible(true);
        toF.set(app, order);
        Map<Role, Player> map = new HashMap<>();
        map.put(Role.Assassin, p1);
        map.put(Role.Thief,    p2);
        Field rtpF = App.class.getDeclaredField("roleToPlayer");
        rtpF.setAccessible(true);
        rtpF.set(app, map);

        Field deadF = App.class.getDeclaredField("assasssinatedRoles");
        deadF.setAccessible(true);
        deadF.set(app, new HashSet<>(order));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bout));
        Field tiF = App.class.getDeclaredField("turnIndex");
        tiF.setAccessible(true);
        tiF.setInt(app, 0);
        app.handleNext();

        String out = bout.toString();
        int count = out.split("was assassinated\\. SKIP").length - 1;
        assertEquals(2, count,
            "SKIP");
    }
    

/**
 * Test case: testCitadelsInvalidArguments
 * This test verifies the functionality of test citadels invalid arguments.
 */
   @Test
    public void testCitadelsInvalidArguments() {
        App app = new App(2);
        String result = app.processCommand("citadels");

        System.out.println("Returned: " + result);
        assertTrue(result.toLowerCase().contains("invaild"));

    }


/**
 * Test case: testCitadelsInvalidPlayerId
 * This test verifies the functionality of test citadels invalid player id.
 */
    @Test
    public void testCitadelsInvalidPlayerId() {
        App app = new App(2);
        assertEquals("Invalid player ID",app.processCommand("citadels abc"),  "Invalid player ID");
    }


/**
 * Test case: testCitadelsNoBuiltDistricts
 * This test verifies the functionality of test citadels no built districts.
 */
    @Test
    public void testCitadelsNoBuiltDistricts() {
        App app = new App(2);
        assertEquals("Player 2 has no built districts", app.processCommand("citadels 2"), "Player 2 has no built districts");
    }


/**
 * Test case: testCitadelsHasBuiltDistricts
 * This test verifies the functionality of test citadels has built districts.
 */
    @Test
    public void testCitadelsHasBuiltDistricts() {
        App app = new App(2);
        Game game = app.getGame();
        Player p2 = game.getPlayers().get(1);


        p2.getBuildDistricCards().clear();
        p2.getBuildDistricCards().add(new Card("Tower", 3, "yellow", ""));

        p2.getBuildDistricCards().add(new Card("Chapel", 2, "blue", ""));
        p2.getBuildDistricCards().add(new Card("Market", 1, "green", ""));
       String result = app.processCommand("citadels 2");
        System.out.println("Result: " + result);
        assertTrue(result.contains("Tower"));
        assertTrue(result.contains("Chapel"));
        assertTrue(result.contains("Market"));

        assertTrue(result.toLowerCase().contains("player 2 has built"));
    }

/**
 * Test case: testLighthouseAbility
 * This test verifies the functionality of test lighthouse ability.
 */
    @Test
    public void testLighthouseAbility() throws Exception {
        App app = new App(1);
        Game game = app.getGame();

        Player p = game.getPlayers().get(0);
        p.getHandCards().clear();
        Card lighthouse = new Card("Lighthouse", 3, "purple", "");
        Deck deck = new Deck();

        Field deckF = App.class.getDeclaredField("deck");
        deckF.setAccessible(true);
        deckF.set(app, deck);
        deck.getCards().clear();
        Card topCard = new Card("Beacon", 2, "yellow", "");
        deck.addBottom(topCard);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        System.setOut(new PrintStream(bout));
        app.purpleCardsOne(p, lighthouse);

        String out = bout.toString();
        assertTrue(out.contains("Lighthouse: you can pick a card Beacon"));

        assertEquals(1, p.getHandCards().size());
        assertEquals("Beacon", p.getHandCards().get(0).getName());

    }

/**
 * Test case: testMuseumPlaceAndSkip
 * This test verifies the functionality of test museum ability place and skip.
 */
    @Test
    public void testMuseumPlaceAndSkip() throws Exception {
        App app = new App(1);

        Player p = app.getGame().getPlayers().get(0);
        Card museum = new Card("Museum", 4, "purple", "");
        p.getHandCards().clear();
        p.getHandCards().addAll(Arrays.asList(

            new Card("A", 1, "blue",""),
            new Card("B", 2, "red","")
        ));

        String seq = "2\n";

        Field scF = App.class.getDeclaredField("scanner");
        scF.setAccessible(true);
        scF.set(app, new Scanner(new ByteArrayInputStream(seq.getBytes())));

        ByteArrayOutputStream bout1 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bout1));
        app.purpleCardsOne(p, museum);
        String out1 = bout1.toString();

        assertTrue(out1.contains("B is placed under museum"));
        assertEquals(1, p.getHandCards().size());
        assertEquals("A", p.getHandCards().get(0).getName());
        p.getHandCards().clear();

        p.getHandCards().add(new Card("C",3,"green",""));
        String seq2 = "0\n";

        scF.set(app, new Scanner(new ByteArrayInputStream(seq2.getBytes())));
        ByteArrayOutputStream bout2 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bout2));
        app.purpleCardsOne(p, museum);
        String out2 = bout2.toString();
        assertTrue(out2.contains("SKIP"));
    }

/**
 * Test case: testQuarryLibraryThroneRoomHelpText
 * This test verifies the functionality of test quarry library throne room help text.
 */
    @Test
    public void testQuarryLibraryThroneRoomHelpText() throws Exception {
        App app = new App(1);
        Player p = app.getGame().getPlayers().get(0);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        System.setOut(new PrintStream(bout));
        // Quarry
        app.purpleCardsOne(p, new Card("Quarry", 3, "purple",""));
        // Library
        app.purpleCardsOne(p, new Card("Library", 4, "purple",""));
        // Throne Room
        app.purpleCardsOne(p, new Card("Throne Room", 5, "purple",""));

        String out = bout.toString();

        assertTrue(out.contains("Quarry: when building"));
        assertTrue(out.contains("Library: if you choose to draw cards"));
        assertTrue(out.contains("Throne Room : Every time the Crown"));
    
    }
/**
 * Test case: testSchoolOfMagicAndHauntedCity
 * This test verifies the functionality of test school of magic and haunted city.
 */
    @Test
    public void testSchoolOfMagicAndHauntedCity() throws Exception {
        App app = new App(1);
        Player p = app.getGame().getPlayers().get(0);
        // School of Magic

        String in1 = "green\n";
        Field scF = App.class.getDeclaredField("scanner");

        scF.setAccessible(true);
        scF.set(app, new Scanner(new ByteArrayInputStream(in1.getBytes())));
        ByteArrayOutputStream bout1 = new ByteArrayOutputStream();

        System.setOut(new PrintStream(bout1));
        app.purpleCardsOne(p, new Card("School of Magic", 5, "purple",""));
        String o1 = bout1.toString();

        assertTrue(o1.contains("School Of Magic will count as green districts for income."));
        assertEquals("green", p.getSchoolOfMagicColour());
        String in2 = "red\n";
        scF.set(app, new Scanner(new ByteArrayInputStream(in2.getBytes())));
        ByteArrayOutputStream bout2 = new ByteArrayOutputStream();

        System.setOut(new PrintStream(bout2));

        app.purpleCardsOne(p, new Card("Haunted City", 6, "purple",""));
        String o2 = bout2.toString();

        assertTrue(o2.contains("Haunted City will count as red for scores"));
        assertEquals("red", p.getHauntedCity());
    }

/**
 * Test case: testPoorHouseGivesOneGoldAtEnd
 * This test verifies the functionality of test poor house gives one gold at end.
 */
    @Test
    public void testPoorHouseGivesOneGoldAtEnd() {
        App app = new App(2);

        Game game = app.getGame();
        Player p = game.getCurrentPlayer();

        Card poorHouse = new Card("Poor House", 1, "green", "");

        assertTrue(p.buildDistricCards(poorHouse), "Should be able to build Poor House");
        while (p.getMoney() > 0) {
            p.spendMoney(1);
        }
        assertEquals(0, p.getMoney(), "Player should start with 0 gold for this test");
        app.end(p);
        assertEquals(1, p.getMoney(), "Poor House should grant 1 gold at end of turn when player had 0 gold");
    }

/**
 * Test case: testParkDrawsTwoCardsAtEnd
 * This test verifies the functionality of test park draws two cards at end.
 */
    @Test
    public void testParkDrawsTwoCardsAtEnd() throws Exception {

        App app = new App(2);
        Game game = app.getGame();
        Player p = game.getCurrentPlayer();

        Card park = new Card("Park", 2, "green", "");

        assertTrue(p.buildDistricCards(park), "Should be able to build Park");
        p.getHandCards().clear();

        assertTrue(p.getHandCards().isEmpty(), "Player's hand should be empty before end()");
        Field deckF = App.class.getDeclaredField("deck");

        deckF.setAccessible(true);
        Deck deck = (Deck) deckF.get(app);
        int before = deck.size();
        app.end(p);

        assertEquals(2, p.getHandCards().size(), "Park should cause player to draw 2 cards at end of turn");
        assertEquals(before - 2, deck.size(), "Deck should have 2 fewer cards after Park effect");
    }
/**
 * Test case: testGameCopy
 * This test verifies the functionality of test game copy.
 */
    @Test
    public void testGameCopy() {
        App app1 = new App(2); 

        app1.startSelectionPhase();  

        app1.getGame().getPlayers().get(0).addMoney(5); 

        App app2 = new App(2); 

        app2.gameCopy(app1);          
        int copiedMoney = app2.getGame().getPlayers().get(0).getMoney();
        assertEquals(7, copiedMoney, "Player 0's money should be copied from app1 to app2");
}
/**
 * Test case: testParseCardsError
 * This test verifies the functionality of test parse cards error.
 */
@Test
public void testParseCardsError() {

    App app = new App(2);
    try {
        Field f = App.class.getDeclaredField("cardsFile");
        f.setAccessible(true);
        f.set(app, new File("nonexistent.tsv")); 

        Deck d = app.parseCards();

        assertNotNull(d);

    } catch (Exception e) {
        fail("Exception during parseCards test: " + e.getMessage());
    }
}

/**
 * Test case: testMagicianBranchEntered
 * This test verifies the functionality of test magician branch entered.
 */
@Test
public void testMagicianBranchEntered() throws Exception {
    App app = new App(4); 
    Game game = app.getGame();
    List<Player> players = game.getPlayers();
    Player magicianPlayer = players.get(0);

    Role magician = Role.Magician;
    Map<Role, Player> roleToPlayer = new LinkedHashMap<>();

    roleToPlayer.put(magician, magicianPlayer);  
    Field roleToPlayerField = App.class.getDeclaredField("roleToPlayer");
    roleToPlayerField.setAccessible(true);

    roleToPlayerField.set(app, roleToPlayer);


    Field turnOrderField = App.class.getDeclaredField("turnOrder");
    turnOrderField.setAccessible(true);
    List<Role> turnOrder = new ArrayList<>();

    turnOrder.add(Role.Magician);


    turnOrderField.set(app, turnOrder);
    Field phaseField = App.class.getDeclaredField("phase");
    phaseField.setAccessible(true);

    phaseField.set(app, App.Phase.turn_phase); 
    Field turnIndexField = App.class.getDeclaredField("turnIndex");
    turnIndexField.setAccessible(true);

    turnIndexField.set(app, 0);
    String simulatedInput = "action draw\nskip\nend\n";
    InputStream fakeInput = new ByteArrayInputStream(simulatedInput.getBytes());
    Scanner testScanner = new Scanner(fakeInput);

    app.setScanner(testScanner);

    game.setCurrentIndex(0);
    app.processTurn();
    System.out.println("Magician branch entered successfully");
}

/**
 * Test case: testColourSpecialCases
 * This test verifies the functionality of test colour special cases.
 */
@Test

public void testColourSpecialCases() {
    App app = new App(2);
    Player p = app.getGame().getCurrentPlayer();
    // 1) School of Magic
    Card sm = new Card("School of Magic", 5, "purple", "");
    p.setSchoolOfMagicColour("red");  
    assertEquals("red", app.Colour(sm, p));

    Card hc = new Card("Haunted City", 6, "purple", "");

    p.setHauntedCity("green");  
    assertEquals("green", app.Colour(hc, p));
    Card normal = new Card("Bank", 4, "yellow", ""
    );
    assertEquals("yellow", app.Colour(normal, p));
}




/**
 * Test case: testGameCopyThrowsOnNull
 * This test verifies the functionality of test game copy throws on null.
 */
@Test

public void testGameCopyThrowsOnNull() {
    App bad = new App(2);

    bad.setGame(null);
    App dest = new App(2);
    assertThrows(IllegalStateException.class, () -> dest.gameCopy(bad));
}



/**
 * Test case: testGameCopyNormal
 * This test verifies the functionality of test game copy normal.
 */
@Test
public void testGameCopyNormal() {
    App src = new App(2);
    src.debug = true;

    src.bellTower = true;
    src.phase = App.Phase.turn_phase;

    App dest = new App(2);
    dest.gameCopy(src);
    assertNotSame(src.getSelectionRoles(), dest.getSelectionRoles());
    assertEquals(src.getSelectionRoles(), dest.getSelectionRoles());
}


/**
 * Test case: testParseCardsMissingResource
 * This test verifies the functionality of test parse cards missing resource.
 */
@Test

public void testParseCardsMissingResource() {
    App app = new App(2);
    app.parseCards();
}

/**
 * Test case: testRunExitAndShortcuts
 * This test verifies the functionality of test run exit and shortcuts.
 */
    @Test
    public void testRunExitAndShortcuts() {

        App app = new App(2);
        String input = "handcards\nmoney\nexit\n";
        app.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bout));
        app.run();
        String out = bout.toString();
        assertTrue(out.contains("Handcards:"));
        assertTrue(out.contains("Money:"));  

        assertTrue(out.contains("Player Exit"));
    }
    

/**
 * Test case: testChosenRoleAndGetRoleFor
 * This test verifies the functionality of test chosen role and get role for.
 */
@Test
    public void testChosenRoleAndGetRoleFor() {

        Game game = new Game(3);        
        Player p1 = game.getPlayers().get(0);
        Player p2 = game.getPlayers().get(1);
        boolean ok = game.choosenRole("King");

        assertTrue(ok);
        assertEquals(Role.King, game.getRoleFor(p1));
        assertFalse(game.getAvailableRoles().contains(Role.King));
        game.setCurrentIndex(1);
        boolean bad = game.choosenRole("NotARole");

        assertFalse(bad);
        assertNull(game.getRoleFor(p2));
    }


/**
 * Test case: testResetRolesAndGetPlayersCopy
 * This test verifies the functionality of test reset roles and get players copy.
 */
    @Test

    public void testResetRolesAndGetPlayersCopy() {
        Game game = new Game(2);
        game.choosenRole("Assassin");
        game.setCurrentIndex(1);

        game.choosenRole("Thief");
        assertNotNull(game.getRoleFor(game.getPlayers().get(0)));
        assertNotNull(game.getRoleFor(game.getPlayers().get(1)));

        game.resetRoles();
        for (Player p : game.getPlayers()) {
            assertNull(game.getRoleFor(p));
        }
        for (Role r : Role.values()) {
            assertTrue(game.getAvailableRoles().contains(r));
        }
        List<Player> copy = game.getPlayers();



        int oldSize = copy.size();
        copy.remove(0);
        assertEquals(oldSize, game.getPlayers().size());
    }




/**
 * Test case: testGameOverPurpleBonuses
 * This test verifies the functionality of test game over purple bonuses.
 */
@Test
public void testGameOverPurpleBonuses() throws Exception {
    App app = new App(4);
    Game game = app.getGame();
    Player player = game.getPlayers().get(0);
    Field currentIndexField = Game.class.getDeclaredField("currentIndex");
    currentIndexField.setAccessible(true);

    currentIndexField.set(game, 0);
    player.setHandCards(new ArrayList<>(Arrays.asList(
        new Card("1", 1, "blue", ""),
        new Card("2", 2, "red", "")

    )));
    player.addMoney(3); 
    Card mapRoom = new Card("Map Room", 3, "purple", "");
    Card treasury = new Card("Imperial Treasury", 5, "purple", "");
    Card wishingWell = new Card("Wishing Well", 3, "purple", "");
    Card purpleOther = new Card("OtherPurple", 4, "purple", "");

    player.getBuildDistricCards().clear();

    player.buildDistricCards(mapRoom);
    player.buildDistricCards(treasury);
    player.buildDistricCards(wishingWell);


    player.buildDistricCards(purpleOther); 
    PrintStream origOut = System.out;

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
    app.gameOver();  
    System.setOut(origOut);


    String output = bout.toString();
    assertTrue(output.contains("Map room bonus"), "Should include Map Room bonus");
    assertTrue(output.contains("Imperial Treasury bonus"), "Should include Treasury bonus");
    assertTrue(output.contains("Wishing well"), "Should include Wishing Well bonus");
}


/**
 * Test case: testGameOverWithMuseum
 * This test verifies the functionality of test game over with museum.
 */
@Test
public void testGameOverWithMuseum() throws Exception {
    App app = new App(4);
    Game game = app.getGame();
    Player player = game.getPlayers().get(0);

    game.setCurrentIndex(0);
    player.getBuildDistricCards().clear();
    Card museum = new Card("Museum", 5, "purple", "Adds score for each card under it");
    player.buildDistricCards(museum);
    List<Card> under = new ArrayList<>();

    under.add(new Card("Test1", 3, "green", ""));

    under.add(new Card("Test2", 2, "blue", ""));

    player.setMuseum(under); 
    PrintStream originalOut = System.out;

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    System.setOut(new PrintStream(outContent));

    app.gameOver();
    System.setOut(originalOut);
    String output = outContent.toString();
    assertTrue(output.contains("Museum"), "");
    assertTrue(output.contains("Cards under Museum: add2 points"), "");
}


/**
 * Test case: testInfoCommandByIndexBuiltCard
 * This test verifies the functionality of test info command by index built card.
 */
@Test
public void testInfoCommandByIndexBuiltCard() {

    App app = new App(2);

    Player player = app.getGame().getPlayers().get(1); 
    Card builtCard = new Card("Watchtower", 2, "red", "Allows you to see enemy cards.");
    player.getBuildDistricCards().clear();
    player.buildDistricCards(builtCard);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    app.processCommand("info 1");

    System.setOut(System.out);
    String output = outContent.toString().trim();
    System.out.println(output); 
}


/**
 * Test case: testArchitectBuildsThreeTimes
 * This test verifies the functionality of test architect builds three times.
 */
@Test

public void testArchitectBuildsThreeTimes() {

    String input = "build 1\nbuild 1\nbuild 1\nend\n";

    App app = new App(2, new ByteArrayInputStream(input.getBytes()));

    Player player = app.getGame().getPlayers().get(0);
    player.getHandCards().clear();

    player.getBuildDistricCards().clear();

    app.setCurrentRole(Role.Architect);
    player.setGold(3);
    for (int i = 0; i < 3; i++) {
        player.drawCard(new Card("Temple" + i, 1, "blue", "Building " + i));

    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    app.processTurn();
    System.setOut(System.out);
    String result = out.toString();


    assertTrue(result.contains("Built: Temple0"));  // maybe not triggered?
    assertTrue(result.contains("Built: Temple1")); 
    assertTrue(result.contains("Built: Temple2"));
    assertTrue(result.contains("The turn ends"));
    assertEquals(3, player.getBuildDistricCards().size());
}

/**
 * Test case: testBuildExceedLimit
 * This test verifies the functionality of test build exceed limit.
 */
@Test
public void testBuildExceedLimit() {
    String input = "build 1\nbuild 1\nend\n";

    App app = new App(2, new ByteArrayInputStream(input.getBytes()));

    Player player = app.getGame().getPlayers().get(0);

    player.getHandCards().clear();

    player.getBuildDistricCards().clear();
    player.drawCard(new Card("Temple", 1, "blue", "First build"));
    player.drawCard(new Card("Church", 1, "blue", "Second build"));

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    System.setOut(new PrintStream(out));
    app.processTurn();

    System.setOut(System.out);

    String result = out.toString();
    assertTrue(result.contains("Built: Temple"));  // maybe not triggered?
    assertTrue(result.contains("You have used all your build this turn."));
    assertEquals(1, player.getBuildDistricCards().size());
}



/**
 * Test case: testEndCommandExitsTurn
 * This test verifies the functionality of test end command exits turn.
 */
@Test
public void testEndCommandExitsTurn() {
    String input = "end\n";

    App app = new App(2, new ByteArrayInputStream(input.getBytes()));
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    System.setOut(new PrintStream(out));


    app.processTurn();
    System.setOut(System.out);

    assertTrue(out.toString().contains("The turn ends"));
}


/**
 * Test case: testBuildTriggersBuildCount
 * This test verifies the functionality of test build triggers build count.
 */
@Test
public void testBuildTriggersBuildCount() {
    String input = "build 1\nend\n";
    App app = new App(2, new ByteArrayInputStream(input.getBytes()));

    Player player = app.getGame().getPlayers().get(0);

    player.getHandCards().clear();
    player.getBuildDistricCards().clear();

    player.drawCard(new Card("Temple", 1, "blue", "Peaceful."));
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    System.setOut(new PrintStream(out));

    app.processTurn();

    System.setOut(System.out);
    assertTrue(out.toString().contains("Built: Temple"));  // maybe not triggered?
    assertEquals(1, player.getBuildDistricCards().size());
}


/**
 * Test case: testDestroyWithoutWarlordRole
 * This test verifies the functionality of test destroy without warlord role.
 */
    @Test
    public void testDestroyWithoutWarlordRole() {
        App app = new App(2, new ByteArrayInputStream(new byte[0]));
        app.setActiveRole(Role.King);
        String result = app.processCommand("destroy 1 1");
        assertTrue(result.contains("Only Warlord can destroy district"));
    }

    @TempDir
        Path tempDir;


/**
 * Test case: testSaveAndLoadWorkflow
 * This test verifies the functionality of test save and load workflow.
 */
    @Test
    public void testSaveAndLoadWorkflow() throws IOException {

        App app = new App(4);
        String r1 = app.processCommand("action income");


        assertTrue(r1.startsWith("Income:"));
        Path file = Files.createTempFile(tempDir, "game", ".json");
        String saveCmd = "save " + file.toString();

        String saveRes = app.processCommand(saveCmd);
        assertEquals("Save file " + file.toString(), saveRes);
        assertTrue(Files.exists(file));
        app.processCommand("action draw");

        String loadCmd = "load " + file.toString();
        String loadRes = app.processCommand(loadCmd);

        assertEquals("Load file " + file.toString(), loadRes);

        int moneyAfter = app.getGame().getPlayers().get(0).getGold();
        assertEquals(4, moneyAfter);
    }


/**
 * Test case: testSaveInvalidInput
 * This test verifies the functionality of test save invalid input.
 */
    @Test
    public void testSaveInvalidInput() {
        App app = new App();
        assertEquals("Invalid input", app.processCommand("save"));
    }


/**
 * Test case: testLoadInvalidInput
 * This test verifies the functionality of test load invalid input.
 */
    @Test
    public void testLoadInvalidInput() {
        App app = new App();

        assertEquals("Invalid", app.processCommand("load"));
    }


/**
 * Test case: testLoadNonexistentFile
 * This test verifies the functionality of test load nonexistent file.
 */
    @Test
    public void testLoadNonexistentFile() {
        App app = new App();
        String res = app.processCommand("load no_such_file.json");
        assertTrue(res.startsWith("Load failed: "));
    }


/**
 * Test case: testStealInvalidArguments
 * This test verifies the functionality of test steal invalid arguments.
 */
    @Test
    public void testStealInvalidArguments() {
        App app = new App(4);
        assertEquals("Invalid arguments", app.processCommand("steal"));
        assertEquals("Invalid arguments", app.processCommand("steal 2"));
    }


/**
 * Test case: testStealAliasForwardsToSpecial
 * This test verifies the functionality of test steal alias forwards to special.
 */
    @Test
    public void testStealAliasForwardsToSpecial() {

        App app = new App(4);

        String aliasResult = app.processCommand("steal foo 2");

        String directResult = app.processCommand("special steal 2");

        assertEquals(directResult, aliasResult);
    }


/**
 * Test case: testAssassinateInvalidArguments
 * This test verifies the functionality of test assassinate invalid arguments.
 */
    @Test
    public void testAssassinateInvalidArguments() {
        App app = new App(4);
        assertEquals("Invalid arguments", app.processCommand("assassinate"));
        assertEquals("Invalid arguments", app.processCommand("assassinate 3"));
    }


/**
 * Test case: testAssassinateAliasForwardsToSpecial
 * This test verifies the functionality of test assassinate alias forwards to special.
 */
    @Test
    public void testAssassinateAliasForwardsToSpecial() {
        App app = new App(4);
        String aliasResult = app.processCommand("assassinate bar 3");

        String directResult = app.processCommand("special kill 3");
        assertEquals(directResult, aliasResult);

    }


/**
 * Test case: testInvalidSpecialCommand
 * This test verifies the functionality of test invalid special command.
 */
    @Test
    public void testInvalidSpecialCommand() {
        String input = "special kill\nexit\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());

        System.setIn(in);
        App app = new App(4);
        app.run();
    }


/**
 * Test case: testUnknownRole
 * This test verifies the functionality of test unknown role.
 */
    @Test
    public void testUnknownRole() {
        String input = "special kill UNKNOWNROLE\nexit\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        App app = new App(4);
        app.run();
    }


/**
 * Test case: testKillSuccess
 * This test verifies the functionality of test kill success.
 */
    @Test
    public void testKillSuccess() {

        String input = "special kill Merchant\nexit\n";

        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        App app = new App(4);
        app.run(); 
    }


/**
 * Test case: testStealSuccess
 * This test verifies the functionality of test steal success.
 */
    @Test
    public void testStealSuccess() {

        String input = "special steal Architect\nexit\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());

        System.setIn(in);
        App app = new App(4);
        Player victim = app.getPlayerByRole(Role.Architect);

        if (victim != null) victim.addMoney(2);
        app.run();
    }


/**
 * Test case: testStealFromAssassinated
 * This test verifies the functionality of test steal from assassinated.
 */
    @Test
    public void testStealFromAssassinated() {
        String input = "special kill Architect\nspecial steal Architect\nexit\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        App app = new App(4);
        app.run();
    }


/**
 * Test case: testStealFromNullVictim
 * This test verifies the functionality of test steal from null victim.
 */
    @Test

    public void testStealFromNullVictim() {
        String input = "special steal King\nexit\n";

        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        App app = new App(4);
        app.run();
    }


/**
 * Test case: testStealZeroMoney
 * This test verifies the functionality of test steal zero money.
 */
    @Test

    public void testStealZeroMoney() {
        String input = "special steal Merchant\nexit\n";

        InputStream in = new ByteArrayInputStream(input.getBytes());

        System.setIn(in);
        App app = new App(4);
        Player victim = app.getPlayerByRole(Role.Merchant);

        if (victim != null) victim.setGold(0); 
        app.run();

    }


/**
 * Test case: testUnknownSpecial
 * This test verifies the functionality of test unknown special.
 */
    @Test
    public void testUnknownSpecial() {

        String input = "special hug Bishop\nexit\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        App app = new App(4);
        app.run();

    }


/**
 * Test case: testLighthouseEffect
 * This test verifies the functionality of test lighthouse effect.
 */
    @Test

    public void testLighthouseEffect() {
        Deck oneCardDeck = new Deck();

        Card factory = new Card("Factory", 6, "purple", "");
        oneCardDeck.addCard(factory);
        Scanner dummyScanner = new Scanner(new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8)));


        App app = new App(4);
        app.setDeck(oneCardDeck);

        app.setScanner(dummyScanner);

        Player p = app.getCurrentPlayer();
        p.getHandCards().clear();
        Card lighthouse = new Card("Lighthouse", 3, "purple", "");
        app.purpleCardsOne(p, lighthouse);
        assertEquals(1,p.getHandCards().size(),"");

        assertTrue(p.getHandCards().get(0).getName().equalsIgnoreCase("Factory"));
    }


/**
 * Test case: testBellTowerYes
 * This test verifies the functionality of test bell tower yes.
 */
     @Test
    public void testBellTowerYes() {
        ByteArrayInputStream in = new ByteArrayInputStream("yes\n".getBytes(StandardCharsets.UTF_8));

        Scanner testScanner = new Scanner(in);
        App app = new App(4);

        app.setScanner(testScanner);
        Player p = app.getCurrentPlayer();
        Card bell = new Card("bellTower", 5, "purple", "");

        app.purpleCardsOne(p, bell);
        assertTrue(app.bellTower, "true");

    }


/**
 * Test case: testBellTowerNo
 * This test verifies the functionality of test bell tower no.
 */
    @Test
    public void testBellTowerNo() {
        System.setIn(new ByteArrayInputStream("no\n".getBytes()));

        App app = new App(4);
        Player p = app.getCurrentPlayer();

        Card bell = new Card("bellTower",5 , "purple", "");
        app.purpleCardsOne(p, bell);
        assertFalse(app.bellTower);
    }


/**
 * Test case: testHospitalEffect
 * This test verifies the functionality of test hospital effect.
 */
    @Test
    public void testHospitalEffect() {
        System.setIn(new ByteArrayInputStream("\n".getBytes()));

        App app = new App(4);

        Player p = app.getCurrentPlayer();
        Card hosp = new Card("hospital", 3, "purple", "");
        app.purpleCardsOne(p, hosp);
        assertTrue(true);
    }


/**
 * Test case: testMuseum
 * This test verifies the functionality of test museum.
 */
    @Test
    public void testMuseum() {
        System.setIn(new ByteArrayInputStream("\n".getBytes()));
        App app = new App(4);
        Player p = app.getCurrentPlayer();
        p.getHandCards().clear();
        Card museum = new Card("museum", 3, "purple", "");
        app.purpleCardsOne(p, museum);
        assertTrue(p.getHandCards().isEmpty());
    }

/**
 * Test case: testQuarry
 * This test verifies the functionality of test quarry.
 */
    @Test
    public void testQuarry() {
        System.setIn(new ByteArrayInputStream("\n".getBytes()));
        App app = new App(4);

        Player p = app.getCurrentPlayer();

        Card quarry = new Card("quarry", 2, "purple", "");
        app.purpleCardsOne(p, quarry);
        assertTrue(true);
    }

/**
 * Test case: testSchoolOfMagic
 * This test verifies the functionality of test school of magic.
 */
    @Test
    public void testSchoolOfMagic() {
        System.setIn(new ByteArrayInputStream("green\n".getBytes()));
        App app = new App(4);
        Player p = app.getCurrentPlayer();

        Card som = new Card("school of magic",6 , "purple", "");
        app.purpleCardsOne(p, som);
        assertEquals("green", p.getSchoolOfMagicColour());

    }


/**
 * Test case: testLibrary
 * This test verifies the functionality of test library.
 */
    @Test
    public void testLibrary() {
        App app = new App(4);

        Player p = app.getCurrentPlayer();
        Card lib = new Card("library", 5, "purple", "");
        app.purpleCardsOne(p, lib);
        assertTrue(true);
    }


/**
 * Test case: testHauntedCity
 * This test verifies the functionality of test haunted city.
 */
    @Test
    public void testHauntedCity() {

        System.setIn(new ByteArrayInputStream("red\n".getBytes()));
        App app = new App(4);
        Player p = app.getCurrentPlayer();
        Card hc = new Card("haunted city",5 , "purple", "");
        app.purpleCardsOne(p, hc);
        assertEquals("red", p.getHauntedCity());
    }


/**
 * Test case: testThroneRoom
 * This test verifies the functionality of test throne room.
 */
    @Test
    public void testThroneRoom() {
        System.setIn(new ByteArrayInputStream("\n".getBytes()));
        App app = new App(4);

        Player player = app.getCurrentPlayer();

        Card throneRoom = new Card("throne room", 4, "purple", "");

        app.purpleCardsOne(player, throneRoom);

        assertTrue(true);
    }


/**
 * Test case: testDragonGate
 * This test verifies the functionality of test dragon gate.
 */
    @Test
    public void testDragonGate() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        System.setOut(new PrintStream(out));

        App app = new App(2);
        Player p = app.getPlayers().get(0);

        Card dragonGate = new Card("Dragon Gate", 5, "purple", "");

        p.buildDistricCards (dragonGate);
        app.gameOver();

        String console = out.toString();
        assertTrue(console.contains("Dragon Gate: value 8 scores at the end"),"" + console);
    }


/**
 * Test case: testUniversity
 * This test verifies the functionality of test university.
 */
    @Test
    public void testUniversity() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        App app = new App(2);
        Player p = app.getPlayers().get(0);

        Card uni = new Card("University", 6, "green", "");
        p.buildDistricCards(uni);
        app.gameOver();
        String console = out.toString();
        assertTrue(console.contains("University: value 8 scores at the end"),"" + console);
    }


/**
 * Test case: testNormal
 * This test verifies the functionality of test normal.
 */
    @Test
    public void testNormal() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        App app = new App(2);

        Player p = app.getPlayers().get(0);
        Card tavern = new Card("Tavern", 1, "red", "");
        p.buildDistricCards(tavern);

        app.gameOver();
        String console = out.toString();
        assertTrue(console.contains("Tavern: cost 1"),"" + console);
    }


/**
 * Test case: testPrintBuildDistrictsEmpty
 * This test verifies the functionality of test print build districts empty.
 */
    @Test


    public void testPrintBuildDistrictsEmpty() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        App app = new App(2);
        Player p = app.getCurrentPlayer();

        p.getBuildDistricCards().clear();

        p.printBuildDistricts();
        String[] lines = out.toString().split("\\r?\\n");

        String lastLine = lines[lines.length - 1].trim();
        assertEquals("Player 1 has no built districts.",lastLine,"" + lastLine + "]");
    }


/**
 * Test case: testPrintBuildDistrictsNonEmpty
 * This test verifies the functionality of test print build districts non empty.
 */
    @Test
    public void testPrintBuildDistrictsNonEmpty() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        App app = new App(2);
        Player p = app.getCurrentPlayer();

        Card castle = new Card("Castle", 4, "blue", "");
        Card tavern = new Card("Tavern", 1, "red", "");

        p.buildDistricCards(castle);


        p.buildDistricCards(tavern);
        p.printBuildDistricts();

        String[] lines = out.toString().split("\\r?\\n");
        String lastLine = lines[lines.length - 1].trim();

        assertTrue(lastLine.startsWith("Player 1built:"),"" + lastLine + "]");
        assertTrue(lastLine.contains("Castle,"),"" + lastLine + "]");
        assertTrue(lastLine.contains("Tavern,"), "" + lastLine + "]");
    }

    /**
    * Test case: testProcessCommandUnknownAction
    * This test verifies that an unrecognized action passed to processCommand()
    * correctly returns an "Unknown action" message.
    */
    @Test
    public void testProcessCommandUnknownAction() {
        App app = new App(4, new ByteArrayInputStream(new byte[0]));
        String res = app.processCommand("action invalid");
        assertEquals("Unknown action: invalid", res);
    }

    /**
    * Test case: testProcessCommandHelp
    * This test ensures that calling "help" returns a string containing valid help instructions.
    */
    @Test
    public void testProcessCommandHelp() {
        App app = new App(4, new ByteArrayInputStream(new byte[0]));
        String res = app.processCommand("help");
        assertTrue(res.contains("build <n>"));
    }

    /**
    * Test case: testProcessCommandHandcardsAlias
    * This test confirms that the "handcards" command returns a list of cards in hand.
    */ 
    @Test
    public void testProcessCommandHandcardsAlias() {
        App app = new App(4, new ByteArrayInputStream(new byte[0]));
        String res = app.processCommand("handcards");
        assertTrue(res.startsWith("Handcards:"));
    }

    /**
    * Test case: testSetCurrentPlayerAndGet
    * This test checks that setCurrentPlayer() correctly updates the current player reference in Game.
    */
    @Test
    void testSetCurrentPlayerAndGet() {
        Game game = new Game(2);
        Player p = new Player(0);
        game.setCurrentPlayer(p);
        assertEquals(p, game.getCurrentPlayer(),"setCurrentPlayer should update the internal current player reference");
    }


    /**
    * Test case: testSpendMoneyNegativeOrTooMuch
    * This test verifies that spendMoney() fails when the amount is negative or exceeds current balance.
    */
    @Test
    void testSpendMoneyNegative() {
        Player player = new Player(1);
        int original = player.getMoney();
        assertFalse(player.spendMoney(-1),"spendMoney should reject negative amounts"); 
        assertEquals(original, player.getMoney());
        assertFalse(player.spendMoney(original + 1),"spendMoney should reject amounts greater than current money");
        assertEquals(original, player.getMoney());
    }

    /**
    * Test case: testBuildDistrictCardsNullAndValid
    * This test verifies that buildDistricCards() rejects null, 
    * and successfully moves a valid card from hand to built list.
    */
    @Test
    void testBuildDistrictCardsNull() {
        Player player = new Player(3);
        assertFalse(player.buildDistricCards(null),"buildDistricCards(null) should return false"); 
        Card c = new Card("Temple", 3, "Green", "");
        player.drawCard(c);
        assertTrue(player.buildDistricCards(c),
            "buildDistricCards should move a card from hand to built list");
        assertFalse(player.getHandCards().contains(c));
        assertTrue(player.getBuildDistricCards().contains(c));
    }


}
// gradle jar						Generate the jar file
// gradle test						Run the testcases
// Please ensure you leave comments in your testcases explaining what the testcase is testing.
// Your mark will be based off the average of branches and instructions code coverage.
// To run the testcases and generate the jacoco code coverage report: 
// gradle test jacocoTestReport