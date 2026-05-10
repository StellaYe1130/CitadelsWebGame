package citadels.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lobby")
@CrossOrigin(origins = "*")
public class LobbyController {

    @Autowired
    private GameRoomManager roomManager;

    @PostMapping("/create")
    public LobbyDTO create(
            @RequestParam(defaultValue = "4") int players,
            @RequestParam(defaultValue = "1") int humans) {
        humans = Math.max(1, Math.min(humans, players));
        return roomManager.createRoom(players, humans);
    }

    @PostMapping("/join/{gameId}")
    public ResponseEntity<LobbyDTO> join(@PathVariable String gameId) {
        LobbyDTO dto = roomManager.joinRoom(gameId);
        if (dto == null) return ResponseEntity.badRequest().build();
        roomManager.broadcast(gameId);
        return ResponseEntity.ok(dto);
    }
}
