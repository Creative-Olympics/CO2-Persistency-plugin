package me.zrunner.olympicsdatapersistency;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.UUID;

public class ScoreboardListener implements Listener {

    private final Main plugin;
    private final FirebaseClient fbClient;

    private final HashMap<UUID, HashMap<String, Integer>> cachedScores = new HashMap<>();

    public ScoreboardListener(Main plugin, FirebaseClient fbClient) {
        this.plugin = plugin;
        this.fbClient = fbClient;
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
                setCachedScore(player.getUniqueId(), objectiveName, scoreValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    private int getCachedScore(UUID playerUUID, String objectiveName) {
        return cachedScores.getOrDefault(playerUUID, new HashMap<>()).getOrDefault(objectiveName, 0);
    }

    private void setCachedScore(UUID playerUUID, String objectiveName, int score) {
        cachedScores.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(objectiveName, score);
    }
}
