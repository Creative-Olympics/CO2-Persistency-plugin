package me.zrunner.olympicsdatapersistency;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;

public final class Main extends JavaPlugin {

    private FirebaseClient fbClient;

    @Override
    public void onLoad() {
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdirs()) {
                setEnabled(false);
                throw new RuntimeException("Could not create data folder.");
            }
        }

        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        File firebaseKeyFile = new File(getDataFolder(), "firebaseServiceAccountKey.json");
        if (!firebaseKeyFile.exists()) {
            setEnabled(false);
            throw new RuntimeException(
                    "Could not find firebaseServiceAccountKey.json in folder %s"
                            .formatted(getDataFolder().getAbsolutePath())
            );
        }
        String firebaseURL = getConfig().getString("firebaseURL");
        if (firebaseURL == null || !firebaseURL.startsWith("https://")) {
            setEnabled(false);
            throw new RuntimeException("firebaseURL not set in config.yml");
        }
        try {
            this.fbClient = new FirebaseClient(new FileInputStream(firebaseKeyFile), firebaseURL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        AdvancementListener advancementListener = new AdvancementListener(this, fbClient);
        ScoreboardListener scoreboardListener = new ScoreboardListener(this, fbClient);
        Bukkit.getPluginManager().registerEvents(advancementListener, this);
        Bukkit.getPluginManager().registerEvents(scoreboardListener, this);

        CommandAPI.onEnable();
        new PersistencyCommand(fbClient, advancementListener).register();
    }

    @Override
    public void onDisable() {
        CommandAPI.unregister("persistency");
        CommandAPI.onDisable();
    }
}
