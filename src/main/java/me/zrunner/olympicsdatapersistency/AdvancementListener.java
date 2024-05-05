package me.zrunner.olympicsdatapersistency;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.concurrent.ExecutionException;

public class AdvancementListener implements Listener {

    private final FirebaseClient fbClient;

    public AdvancementListener(FirebaseClient fbClient) {
        this.fbClient = fbClient;
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        // get the player who has completed the advancement
        String playerName = event.getPlayer().getName();
        // get the advancement key
        String advancementKey = event.getAdvancement().getKey().getKey();
        // log the completion of the advancement
        System.out.println(playerName + " has completed the advancement: " + advancementKey);
        try {
            fbClient.addUserAdvancement(event.getPlayer().getUniqueId().toString(), "TEST_" + advancementKey);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
