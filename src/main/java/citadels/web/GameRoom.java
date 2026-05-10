package citadels.web;

import java.util.Set;

public class GameRoom {
    public final String gameId;
    public final int totalPlayers;
    public final int humanCount;
    public volatile int joinedCount = 0;
    public volatile boolean started = false;
    public final WebGameService service;

    public GameRoom(String gameId, int totalPlayers, int humanCount, Set<Integer> humanPlayerIds) {
        this.gameId = gameId;
        this.totalPlayers = totalPlayers;
        this.humanCount = humanCount;
        this.service = new WebGameService(totalPlayers, humanPlayerIds);
    }

    public boolean isReady() {
        return joinedCount >= humanCount;
    }
}
