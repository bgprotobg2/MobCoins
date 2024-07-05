package bgprotobg.net.coldcoins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {

    private final CoinManager coinManager;
    private final ShopManager shopManager;
    private final ColdCoins plugin;

    public CommandHandler(CoinManager coinManager, ShopManager shopManager, ColdCoins plugin) {
        this.coinManager = coinManager;
        this.shopManager = shopManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("mobcoins")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.usage")));
                return true;
            }

            String subCommand = args[0];

            if (subCommand.equalsIgnoreCase("pay") && args.length == 3) {
                if (sender instanceof Player) {
                    Player senderPlayer = (Player) sender;
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target != null) {
                        int amount;
                        try {
                            amount = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid-amount")));
                            return true;
                        }
                        coinManager.payCoins(senderPlayer, target, amount);
                        senderPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.coins-paid"))
                                .replace("%amount%", String.valueOf(amount))
                                .replace("%player%", target.getName()));
                        target.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.coins-received"))
                                .replace("%amount%", String.valueOf(amount))
                                .replace("%player%", senderPlayer.getName()));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.player-not-found")));
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.only-players")));
                }
            } else if (subCommand.equalsIgnoreCase("give")) {
                if (sender.hasPermission("mobcoins.give")) {
                    if (args.length == 3) {
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target != null) {
                            int amount;
                            try {
                                amount = Integer.parseInt(args[2]);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid-amount")));
                                return true;
                            }
                            coinManager.giveCoins(target, amount);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.coins-given"))
                                    .replace("%amount%", String.valueOf(amount))
                                    .replace("%player%", target.getName()));
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.player-not-found")));
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.usage")));
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission")));
                }
            } else if (subCommand.equalsIgnoreCase("balance")) {
                if (args.length == 1 && sender instanceof Player) {
                    Player player = (Player) sender;
                    int balance = coinManager.getBalance(player);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.balance"))
                            .replace("%balance%", String.valueOf(balance)));
                } else if (args.length == 2) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target != null) {
                        int balance = coinManager.getBalance(target);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.balance-other"))
                                .replace("%player%", target.getName())
                                .replace("%balance%", String.valueOf(balance)));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.player-not-found")));
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.usage")));
                }
            } else if (subCommand.equalsIgnoreCase("take")) {
                if (sender.hasPermission("mobcoins.take")) {
                    if (args.length == 3) {
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target != null) {
                            int amount;
                            try {
                                amount = Integer.parseInt(args[2]);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid-amount")));
                                return true;
                            }
                            if (coinManager.getBalance(target) >= amount) {
                                coinManager.takeCoins(target, amount);
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.coins-taken"))
                                        .replace("%amount%", String.valueOf(amount))
                                        .replace("%player%", target.getName()));
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.not-enough-coins")));
                            }
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.player-not-found")));
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.usage")));
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission")));
                }
            } else if (subCommand.equalsIgnoreCase("shop")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    shopManager.openShop(player);
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.only-players")));
                }
            } else if (subCommand.equalsIgnoreCase("reload")) {
                if (sender.hasPermission("mobcoins.reload")) {
                    plugin.reloadConfigs();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.config-reloaded")));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission")));
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.usage")));
            }
            return true;
        }
        return false;
    }
}
