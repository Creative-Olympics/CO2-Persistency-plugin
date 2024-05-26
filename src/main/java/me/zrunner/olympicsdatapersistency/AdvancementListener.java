package me.zrunner.olympicsdatapersistency;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class AdvancementListener implements Listener {

    private final Main plugin;
    private final FirebaseClient fbClient;

    public AdvancementListener(Main plugin, FirebaseClient fbClient) {
        this.plugin = plugin;
        this.fbClient = fbClient;
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        // get the player who has completed the advancement
        String playerName = event.getPlayer().getName();
        // get the advancement key
        String advancementKey = event.getAdvancement().getKey().getKey();
        // check if the advancement should be synced
        if (!getSyncedAdvancements().contains(advancementKey)) {
            return;
        }
        // log the completion of the advancement
        System.out.println("Syncing advancement: " + advancementKey + " for player " + playerName);
        try {
            fbClient.addUserAdvancement(event.getPlayer().getUniqueId().toString(), advancementKey);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getSyncedAdvancements() {
        return plugin.getConfig().getStringList("syncedAdvancements");
    }

}
