package me.zrunner.olympicsdatapersistency;

import javax.annotation.Nonnull;
import java.util.ArrayList;
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
     * The amount of money the user has. Cannot be null.
     */
    private long money;
    /**
     * The list of achievement IDs the user has. Empty by default
     */
    private @Nonnull List<String> achievements;

    public FirebaseUser() {
        this.displayName = "";
        this.minecraftName = null;
        this.mcUUID = null;
        this.money = 0;
        this.achievements = new ArrayList<>();
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

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public @Nonnull List<String> getAchievements() {
        return achievements;
    }

    public void setAchievements(@Nonnull List<String> achievements) {
        this.achievements = achievements;
    }

}
