package citadels.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GameRoomManagerTest {

    private GameRoomManager manager;
    private SimpMessagingTemplate template;

    @BeforeEach
    void setUp() {
        manager = new GameRoomManager();
        template = mock(SimpMessagingTemplate.class);
        ReflectionTestUtils.setField(manager, "template", template);
    }

    @Test
    void createRoomRegistersCreatorAsFirstHumanPlayer() {
        LobbyDTO lobby = manager.createRoom(6, 2);

        assertNotNull(lobby.gameId);
        assertEquals(6, lobby.gameId.length());
        assertEquals(1, lobby.playerId);
        assertEquals(6, lobby.totalPlayers);
        assertEquals(2, lobby.humanCount);
        assertEquals(1, lobby.joinedCount);

        GameRoom room = manager.getRoom(lobby.gameId);
        assertNotNull(room);
        assertFalse(room.started);
        assertFalse(room.isReady());
    }

    @Test
    void joinRoomRejectsUnknownOrFullRooms() {
        assertNull(manager.joinRoom("MISSING"));

        LobbyDTO lobby = manager.createRoom(4, 2);
        LobbyDTO join = manager.joinRoom(lobby.gameId);

        assertNotNull(join);
        assertEquals(2, join.playerId);
        assertEquals(2, join.joinedCount);
        assertTrue(manager.getRoom(lobby.gameId).isReady());
        assertNull(manager.joinRoom(lobby.gameId));
    }

    @Test
    void ensureStartedInitializesReadyRoomOnlyOnce() {
        LobbyDTO lobby = manager.createRoom(4, 1);

        manager.ensureStarted(lobby.gameId);
        GameRoom room = manager.getRoom(lobby.gameId);

        assertTrue(room.started);
        assertNotNull(room.service.getState().players);

        manager.ensureStarted(lobby.gameId);
        assertTrue(room.started);
    }

    @Test
    void broadcastPublishesStateToRoomTopic() {
        LobbyDTO lobby = manager.createRoom(4, 1);
        manager.ensureStarted(lobby.gameId);

        manager.broadcast(lobby.gameId);

        verify(template).convertAndSend(eq("/topic/game/" + lobby.gameId), isA(GameStateDTO.class));
    }
}
