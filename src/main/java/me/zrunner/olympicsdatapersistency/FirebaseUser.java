package me.zrunner.olympicsdatapersistency;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FirebaseUser {

    /**
     * The chosen display name of the user. Cannot be null.
     */
    private @Nonnull String displayName;
    /**
     * The Minecraft username of the user. May be null.
     */
    private String minecraftName;
    /**
     * The UUID of the user. May be null;
     */
    private String mcUUID;
    /**
     * The scores of the user. Empty by default
     */
    private HashMap<String, Integer> scores;
    /**
     * The list of advancements IDs the user has. Empty by default
     */
    private @Nonnull List<String> advancements;

    public FirebaseUser() {
        this.displayName = "";
        this.minecraftName = null;
        this.mcUUID = null;
        this.scores = new HashMap<>();
        this.advancements = new ArrayList<>();
    }

    public FirebaseUser(@Nonnull String mcUUID, @Nonnull String playerName) {
        this();
        this.mcUUID = mcUUID;
        this.displayName = playerName;
        this.minecraftName = playerName;
    }

    public @Nonnull String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@Nonnull String displayName) {
        this.displayName = displayName;
    }

    public String getMinecraftName() {
        return minecraftName;
    }

    public void setMinecraftName(String minecraftName) {
        this.minecraftName = minecraftName;
    }

    public String getMcUUID() {
        return mcUUID;
    }

    public void setMcUUID(String mcUUID) {
        this.mcUUID = mcUUID;
    }

    public HashMap<String, Integer> getScores() {
        return scores;
    }

    public void setScores(HashMap<String, Integer> scores) {
        this.scores = scores;
    }

    public @Nonnull List<String> getAdvancements() {
        return advancements;
    }

    public void setAdvancements(@Nonnull List<String> advancements) {
        this.advancements = advancements;
    }


    public void syncAdvancementsToPlayer(Player player) {
        for (String advancementKey : advancements) {
            System.out.println("Syncing advancement: " + advancementKey + " for player " + player.getName());
            Advancement advancement = Bukkit.getAdvancement(NamespacedKey.minecraft(advancementKey));
            if (advancement == null) {
                System.out.println("Advancement " + advancementKey + " not found.");
                continue;
            }
            for (String criteria : advancement.getCriteria()) {
                player.getAdvancementProgress(advancement).awardCriteria(criteria);
            }
        }
    }

    public void syncScoresToPlayer(Player player) {
        for (String objectiveName : scores.keySet()) {
            System.out.println("Syncing score: " + objectiveName + " for player " + player.getName());
            Objective objective = player.getScoreboard().getObjective(objectiveName);
            if (objective == null) {
                System.out.println("Objective " + objectiveName + " not found.");
                continue;
            }
            objective.getScore(player.getName()).setScore(scores.get(objectiveName));
        }
    }

    public void syncAllToPlayer(Player player) {
        syncAdvancementsToPlayer(player);
        syncScoresToPlayer(player);
    }
}
