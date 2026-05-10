package citadels.web;

import java.util.List;

public class GameStateDTO {

    public String phase;
    public List<PlayerDTO> players;
    public List<String> availableRoles;
    public String activeRole;
    public List<String> log;
    public String prompt;
    public List<CardDTO> pendingCards;
    public boolean gameOver;
    public String gameId;
    public int currentActingPlayerId;
    public int waitingFor;

    public static class PlayerDTO {
        public int id;
        public boolean isHuman;
        public int gold;
        public int handSize;
        public List<CardDTO> hand;
        public List<CardDTO> city;
        public boolean isCurrentTurn;
        public String role;
    }

    public static class CardDTO {
        public String name;
        public String colour;
        public int cost;
        public String description;

        public CardDTO() {}

        public CardDTO(String name, String colour, int cost, String description) {
            this.name = name;
            this.colour = colour;
            this.cost = cost;
            this.description = description;
        }
    }
}
