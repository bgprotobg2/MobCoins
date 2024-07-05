package bgprotobg.net.coldcoins;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CoinManager {

    private final Map<UUID, Integer> coinBalance;
    private final File dataFile;
    private FileConfiguration dataConfig;
    private final JavaPlugin plugin;

    public CoinManager(JavaPlugin plugin) {
        this.coinBalance = new HashMap<>();
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadCoinData();
    }

    public void giveCoins(Player player, int amount) {
        UUID playerUUID = player.getUniqueId();
        int currentBalance = coinBalance.getOrDefault(playerUUID, 0);
        coinBalance.put(playerUUID, currentBalance + amount);
        saveCoinData();
    }

    public void takeCoins(Player player, int amount) {
        UUID playerUUID = player.getUniqueId();
        int currentBalance = coinBalance.getOrDefault(playerUUID, 0);
        if (currentBalance >= amount) {
            coinBalance.put(playerUUID, currentBalance - amount);
            saveCoinData();
        } else {
            player.sendMessage(ChatColor.RED + "You don't have enough coins to take.");
        }
    }

    public int getBalance(Player player) {
        return coinBalance.getOrDefault(player.getUniqueId(), 0);
    }

    public void payCoins(Player sender, Player receiver, int amount) {
        if (getBalance(sender) >= amount) {
            takeCoins(sender, amount);
            giveCoins(receiver, amount);
            saveCoinData();
        }
    }

    private void loadCoinData() {
        if (!dataFile.exists()) {
            return;
        }

        for (String key : dataConfig.getKeys(false)) {
            UUID playerUUID = UUID.fromString(key);
            int balance = dataConfig.getInt(key);
            coinBalance.put(playerUUID, balance);
        }
    }

    protected void saveCoinData() {
        for (Map.Entry<UUID, Integer> entry : coinBalance.entrySet()) {
            dataConfig.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
