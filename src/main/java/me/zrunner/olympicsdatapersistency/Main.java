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
        File firebaseKeyFile = new File(getDataFolder(), "firebaseServiceAccountKey.json");
        if (!firebaseKeyFile.exists()) {
            setEnabled(false);
            throw new RuntimeException(
                    "Could not find firebaseServiceAccountKey.json in folder %s"
                            .formatted(getDataFolder().getAbsolutePath())
            );
        }
        System.out.println(firebaseKeyFile.toURI());
        try {
            this.fbClient = new FirebaseClient(new FileInputStream(firebaseKeyFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bukkit.getPluginManager().registerEvents(new AdvancementListener(fbClient), this);

        CommandAPI.onEnable();
        new PersistencyCommand(fbClient).register();
    }

    @Override
    public void onDisable() {
        CommandAPI.unregister("persistency");
        CommandAPI.onDisable();
    }
}
