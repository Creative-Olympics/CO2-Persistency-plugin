package me.zrunner.olympicsdatapersistency;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ScoreboardListener implements Listener {

    private final Main plugin;
    private final FirebaseClient fbClient;

    private final HashMap<UUID, HashMap<String, Integer>> cachedScores = new HashMap<>();

    public ScoreboardListener(Main plugin, FirebaseClient fbClient) {
        this.plugin = plugin;
        this.fbClient = fbClient;
    }

    public int getCachedScore(UUID playerUUID, String objectiveName) {
        return cachedScores.getOrDefault(playerUUID, new HashMap<>()).getOrDefault(objectiveName, 0);
    }

    public void setCachedScore(UUID playerUUID, String objectiveName, int score) {
        cachedScores.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(objectiveName, score);
    }

    public void setCachedScores(UUID playerUUID, HashMap<String, Integer> scores) {
        cachedScores.put(playerUUID, scores);
    }

    public void updatePlayerScores(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        for (String objectiveName : plugin.getConfig().getStringList("syncedObjectives")) {
            Objective objective = scoreboard.getObjective(objectiveName);
            if (objective == null) {
                continue;
            }
            Score score = objective.getScore(player.getName());
            int scoreValue = score.getScore();
            int cachedScore = getCachedScore(player.getUniqueId(), objectiveName);
            if (scoreValue == cachedScore) {
                continue;
            }
            try {
                fbClient.setUserScore(player.getUniqueId().toString(), objectiveName, scoreValue);
            } catch (ExecutionException | InterruptedException e) {
                score.setScore(cachedScore);
                System.err.println("Unable to sync score " + objectiveName + " for player " + player.getName());
                e.printStackTrace();
                return;
            }
            setCachedScore(player.getUniqueId(), objectiveName, scoreValue);
        }
    }

    public void updatePlayersScores() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerScores(player);
        }
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String command = event.getCommand();
        if (command.contains("scoreboard")) {
            Bukkit.getScheduler().runTask(plugin, this::updatePlayersScores);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        if (command.contains("scoreboard")) {
            Bukkit.getScheduler().runTask(plugin, () -> updatePlayerScores(event.getPlayer()));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        FirebaseUser user;
        try {
            user = fbClient.getOrCreateUserFromMinecraftUUID(
                    playerUUID.toString(),
                    event.getPlayer().getName()
            );
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("Unable to get or create user for player with UUID " + playerUUID + ".");
            return;
        }
        setCachedScores(playerUUID, user.getScores());
        user.syncScoresToPlayer(event.getPlayer());
    }

}
