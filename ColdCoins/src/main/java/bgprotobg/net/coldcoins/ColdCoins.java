package bgprotobg.net.coldcoins;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ColdCoins extends JavaPlugin {
    private CoinManager coinManager;
    private ShopManager shopManager;
    private File shopConfigFile;
    private FileConfiguration shopConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadShopConfig();
        coinManager = new CoinManager(this);
        shopManager = new ShopManager(coinManager, getConfig(), shopConfig);

        getCommand("mobcoins").setExecutor(new CommandHandler(coinManager, shopManager, this));
        getServer().getPluginManager().registerEvents(shopManager, this);
        getServer().getPluginManager().registerEvents(new MobKillListener(coinManager, getConfig()), this);
    }

    @Override
    public void onDisable() {
        saveConfig();
        saveShopConfig();
    }

    public void reloadConfigs() {
        reloadConfig();
        loadShopConfig();
        shopManager.reloadConfigs(getConfig(), shopConfig);
    }

    private void loadShopConfig() {
        shopConfigFile = new File(getDataFolder(), "shop.yml");
        if (!shopConfigFile.exists()) {
            shopConfigFile.getParentFile().mkdirs();
            saveResource("shop.yml", false);
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopConfigFile);
    }

    public FileConfiguration getShopConfig() {
        return shopConfig;
    }

    public void saveShopConfig() {
        try {
            shopConfig.save(shopConfigFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save shop.yml", e);
        }
    }
}
