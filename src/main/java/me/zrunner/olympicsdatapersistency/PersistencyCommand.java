package me.zrunner.olympicsdatapersistency;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;


public class PersistencyCommand {

    private final FirebaseClient fbClient;

    private final AdvancementListener advancementListener;

    public PersistencyCommand(FirebaseClient fbClient, AdvancementListener advancementListener) {
        this.fbClient = fbClient;
        this.advancementListener = advancementListener;
    }

    public void register() {
        new CommandTree("persistency")
                .then(userInfoSubcommand())
                .then(playerInfoSubcommand())
                .then(syncPlayerFromFirebaseSubcommand())
                .register();
        System.out.println("Command /persistency registered.");
    }

    private Argument<String> userInfoSubcommand() {
        return new LiteralArgument("user-info")
                .then(new StringArgument("firebaseID")
                        .executes((sender, args) -> {
                            String firebaseID = (String) args.get("firebaseID");
                            if (firebaseID == null) {
                                sender.sendMessage(ChatColor.RED + "No Firebase ID provided.");
                                return 0;
                            }
                            FirebaseUser user = fbClient.getUserFromFirebaseID(firebaseID);
                            if (user == null) {
                                sender.sendMessage(ChatColor.RED + "No user found with the Firebase ID " + firebaseID + ".");
                                return 0;
                            }
                            sender.sendMessage(ChatColor.GREEN + describeUser(user));
                            return 1;
                        })
                );
    }

    private Argument<String> playerInfoSubcommand() {
        return new LiteralArgument("player-info")
                .then(new PlayerArgument("player")
                        .executes((sender, args) -> {
                            Player player = (Player) args.get("player");
                            if (player == null) {
                                sender.sendMessage(ChatColor.RED + "No player provided.");
                                return 0;
                            }
                            FirebaseUser user = null;
                            try {
                                user = fbClient.getUserFromMinecraftUUID(player.getUniqueId().toString());
                            } catch (ExecutionException e) {
                                sender.sendMessage(ChatColor.RED + "Timeout while fetching user data.");
                                return 1;
                            }
                            if (user == null) {
                                sender.sendMessage(ChatColor.RED + "No user found with the UUID " + player.getUniqueId() + ".");
                                return 0;
                            }
                            sender.sendMessage(ChatColor.GREEN + describeUser(user));
                            return 1;
                        })
                );
    }

    private Argument<String> syncPlayerFromFirebaseSubcommand() {
        return new LiteralArgument("sync-player-from-firebase")
                .then(new PlayerArgument("player")
                        .executes((sender, args) -> {
                            Player player = (Player) args.get("player");
                            if (player == null) {
                                sender.sendMessage(ChatColor.RED + "No player provided.");
                                return 0;
                            }
                            FirebaseUser user;
                            try {
                                user = fbClient.getOrCreateUserFromMinecraftUUID(
                                        player.getUniqueId().toString(),
                                        player.getName()
                                );
                            } catch (ExecutionException | InterruptedException e) {
                                sender.sendMessage(
                                        ChatColor.RED + "Unable to get or create user for player with UUID "
                                                + player.getUniqueId() + "."
                                );
                                return 1;
                            }
                            advancementListener.setAdvancementsSyncedToCache(player.getUniqueId(), user.getAdvancements());
                            user.syncAllToPlayer(player);
                            sender.sendMessage(ChatColor.GREEN + "Synced user data to player " + player.getName() + ".");
                            return 1;
                        })
                );
    }

    private @Nonnull String describeUser(@Nonnull FirebaseUser user) {
        return """
                User found: %s
                Minecraft name: %s (%s)
                Scores: %s
                Advancements: %s""".formatted(
                user.getDisplayName(),
                user.getMinecraftName(),
                user.getMcUUID(),
                user.getScores(),
                user.getAdvancements()
        );
    }
}
