package citadels.web;

public class LobbyDTO {
    public String gameId;
    public int playerId;
    public int totalPlayers;
    public int humanCount;
    public int joinedCount;

    public LobbyDTO() {}

    public LobbyDTO(String gameId, int playerId, int totalPlayers, int humanCount, int joinedCount) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.totalPlayers = totalPlayers;
        this.humanCount = humanCount;
        this.joinedCount = joinedCount;
    }
}
