package me.zrunner.olympicsdatapersistency;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class AdvancementListener implements Listener {

    private final Main plugin;
    private final FirebaseClient fbClient;

    private final HashMap<UUID, List<String>> cachedAdvancements = new HashMap<>();

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
        // check if the advancement has already been synced
        if (isAdvancementSyncedFromCache(event.getPlayer().getUniqueId(), advancementKey)) {
            return;
        }
        // log the completion of the advancement
        System.out.println("Syncing advancement: " + advancementKey + " for player " + playerName);
        try {
            fbClient.addUserAdvancement(event.getPlayer().getUniqueId().toString(), advancementKey);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        setAdvancementSyncedToCache(event.getPlayer().getUniqueId(), advancementKey);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // get the player's UUID
        String playerUUID = event.getPlayer().getUniqueId().toString();
        // get the player's advancements
        FirebaseUser user = fbClient.getUserFromMinecraftUUID(playerUUID);
        if (user == null) {
            System.out.println("New player detected: " + event.getPlayer().getName());
            return;
        }
        setAdvancementsSyncedToCache(event.getPlayer().getUniqueId(), user.getAdvancements());
        user.syncAdvancementsToPlayer(event.getPlayer());
    }

    private List<String> getSyncedAdvancements() {
        return plugin.getConfig().getStringList("syncedAdvancements");
    }

    public Boolean isAdvancementSyncedFromCache(UUID playerUUID, String advancementKey) {
        List<String> playerAdvancements = cachedAdvancements.get(playerUUID);
        return playerAdvancements != null && playerAdvancements.contains(advancementKey);
    }

    public void setAdvancementSyncedToCache(UUID playerUUID, String advancementKey) {
        cachedAdvancements.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(advancementKey);
    }

    public void setAdvancementsSyncedToCache(UUID playerUUID, List<String> advancements) {
        cachedAdvancements.put(playerUUID, advancements);
    }

}
