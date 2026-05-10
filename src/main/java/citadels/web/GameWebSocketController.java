package citadels.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
public class GameWebSocketController {

    @Autowired
    private GameRoomManager roomManager;

    @MessageMapping("/game/{gameId}/action")
    public void action(@DestinationVariable String gameId, @Payload ActionMessage msg) {
        roomManager.processAction(gameId, msg.playerId, msg.command);
    }

    @MessageMapping("/game/{gameId}/state")
    public void requestState(@DestinationVariable String gameId) {
        roomManager.ensureStarted(gameId);
        roomManager.broadcast(gameId);
    }
}
