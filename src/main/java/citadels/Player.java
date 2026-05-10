package citadels;

import java.util.*;

public class Player{
    private final int id;
    private int money;
    private List<Card> handCards;
    // Player.java

    private List<Card> buildDistrictCards = new ArrayList<>();

    public void setBuildDistrictCards(List<Card> districts) {
        this.buildDistrictCards = districts;
    }


    public boolean laboratory = false;
    public boolean smithy = false;
    public boolean armory = false;
    public boolean isArmoryUsed() {
        return this.armory;
    }


    public List<Card> museum = new ArrayList<>();

    public List<Card> getMuseum(){
        return museum;
    }

    public void setMuseum(List<Card> cards) {
        this.museum = cards;
    }

    private String schoolOfMagicColour = null;

    public String getSchoolOfMagicColour(){
        return schoolOfMagicColour;
    }

    public void setSchoolOfMagicColour(String colour){
        this.schoolOfMagicColour = colour.toLowerCase();
    }

    private String hauntedCity = null;

    public String getHauntedCity(){
        return hauntedCity;
    }

    public void setHauntedCity(String colour){
        this.hauntedCity = colour.toLowerCase();
    }
    
    public Player(int id){
        this.id = id;
        this.money = 2;
        this.handCards = new ArrayList<>();
        this.buildDistrictCards = new ArrayList<>();
    }

    public Player(Player other){
        this.id = other.id;
        this.money = other.money;
        this.handCards = new ArrayList<>(other.handCards);
        this.buildDistrictCards = new ArrayList<>(other.buildDistrictCards);
    }

    public int getId(){
        return id;
    }
    
    public int getMoney(){
        return money;
    }

    public void addMoney(int amount){
        if(amount > 0){
            money += amount;
        }
    }

    public boolean spendMoney(int amount){
        if(amount > 0 && amount <= money){
            money -= amount;
            return true;
        }else{
            return false;
        }
    }

    public void drawCard(Card card){
        if(card != null ){
            handCards.add(card);
        }
    }

    public boolean buildDistricCards(Card card){
        if(card == null ){
            return false;
        }
        handCards.remove(card);
        buildDistrictCards.add(card);
        return true;
        
    }

    public List<Card> getHandCardsCopy() {
        return new ArrayList<>(this.handCards);
    }
    
    public List<Card> getHandCards(){
        return this.handCards;
    }

    public List<Card> getBuildDistricCards(){
        return this.buildDistrictCards;
    }

    public void setHandCards(List<Card> newHand) {
        this.handCards = newHand;
    }

    public void printBuildDistricts(){
        if(buildDistrictCards.isEmpty()){
            System.out.println("Player " + id + " has no built districts.");
        }else{
            System.out.print("Player " + id + "built: ");
            for(Card card: buildDistrictCards){
                System.out.print(card.getName() + ", ");
            }
            System.out.println();
        }
    }

    public void setGold(int amount) {
        this.money = amount;
    }

    public int getGold() {
        return this.money;
    }   

}






