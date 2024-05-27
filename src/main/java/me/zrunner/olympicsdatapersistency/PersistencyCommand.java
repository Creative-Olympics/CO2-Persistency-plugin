package me.zrunner.olympicsdatapersistency;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;


public class PersistencyCommand {

    private final FirebaseClient fbClient;

    public PersistencyCommand(FirebaseClient fbClient) {
        this.fbClient = fbClient;
    }

    public void register() {
        new CommandTree("persistency")
                .then(userInfoSubcommand())
                .then(playerInfoSubcommand())
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
                            FirebaseUser user = fbClient.getUserFromMinecraftUUID(player.getUniqueId().toString());
                            if (user == null) {
                                sender.sendMessage(ChatColor.RED + "No user found with the UUID " + player.getUniqueId() + ".");
                                return 0;
                            }
                            sender.sendMessage(ChatColor.GREEN + describeUser(user));
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
