package me.zrunner.olympicsdatapersistency;

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

}
