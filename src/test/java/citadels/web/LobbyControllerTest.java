package citadels.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LobbyController.class)
class LobbyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameRoomManager roomManager;

    @Test
    void createClampsHumanCountToTotalPlayers() throws Exception {
        when(roomManager.createRoom(3, 3))
                .thenReturn(new LobbyDTO("ABC123", 1, 3, 3, 1));

        mockMvc.perform(post("/api/lobby/create")
                        .param("players", "3")
                        .param("humans", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("ABC123"))
                .andExpect(jsonPath("$.humanCount").value(3));

        verify(roomManager).createRoom(3, 3);
    }

    @Test
    void joinReturnsLobbyAndBroadcastsOnSuccess() throws Exception {
        when(roomManager.joinRoom("ABC123"))
                .thenReturn(new LobbyDTO("ABC123", 2, 4, 2, 2));

        mockMvc.perform(post("/api/lobby/join/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(2))
                .andExpect(jsonPath("$.joinedCount").value(2));

        verify(roomManager).broadcast("ABC123");
    }

    @Test
    void joinReturnsBadRequestWhenRoomCannotBeJoined() throws Exception {
        when(roomManager.joinRoom("MISSING")).thenReturn(null);

        mockMvc.perform(post("/api/lobby/join/MISSING"))
                .andExpect(status().isBadRequest());

        verify(roomManager, never()).broadcast("MISSING");
    }
}
