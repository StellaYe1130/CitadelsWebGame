package citadels.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameRoomManager {

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate template;

    public synchronized LobbyDTO createRoom(int totalPlayers, int humanCount) {
        String gameId = generateId();
        Set<Integer> humanIds = new HashSet<>();
        for (int i = 1; i <= humanCount; i++) humanIds.add(i);
        GameRoom room = new GameRoom(gameId, totalPlayers, humanCount, humanIds);
        room.joinedCount = 1;
        rooms.put(gameId, room);
        return new LobbyDTO(gameId, 1, totalPlayers, humanCount, 1);
    }

    public synchronized LobbyDTO joinRoom(String gameId) {
        GameRoom room = rooms.get(gameId);
        if (room == null || room.joinedCount >= room.humanCount) return null;
        room.joinedCount++;
        int playerId = room.joinedCount;
        return new LobbyDTO(gameId, playerId, room.totalPlayers, room.humanCount, room.joinedCount);
    }

    // Starts the game if all humans have joined and game hasn't started yet.
    public synchronized void ensureStarted(String gameId) {
        GameRoom room = rooms.get(gameId);
        if (room == null || room.started || !room.isReady()) return;
        room.started = true;
        room.service.newGame();
        room.service.autoAdvance();
    }

    public GameRoom getRoom(String gameId) {
        return rooms.get(gameId);
    }

    public void processAction(String gameId, int playerId, String command) {
        GameRoom room = rooms.get(gameId);
        if (room == null || !room.started) return;
        room.service.humanAction(playerId, command);
        room.service.autoAdvance();
        broadcast(gameId);
    }

    public void broadcast(String gameId) {
        GameRoom room = rooms.get(gameId);
        if (room == null) return;
        GameStateDTO dto = room.service.getState();
        dto.gameId = gameId;
        dto.waitingFor = Math.max(0, room.humanCount - room.joinedCount);
        template.convertAndSend("/topic/game/" + gameId, dto);
    }

    private String generateId() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rand.nextInt(chars.length())));
        return sb.toString();
    }
}
