package bgprotobg.net.coldcoins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ShopManager implements Listener {

    private final CoinManager coinManager;
    private FileConfiguration config;
    private FileConfiguration shopConfig;

    public ShopManager(CoinManager coinManager, FileConfiguration config, FileConfiguration shopConfig) {
        this.coinManager = coinManager;
        this.config = config;
        this.shopConfig = shopConfig;
    }

    public void openShop(Player player) {
        int guiSize = config.getInt("shop-gui-size", 27);
        Inventory shop = Bukkit.createInventory(null, guiSize, ChatColor.GREEN + "Mobcoins Shop");

        Set<String> items = shopConfig.getConfigurationSection("shop-items").getKeys(false);
        for (String key : items) {
            String path = "shop-items." + key;
            Material material = Material.valueOf(shopConfig.getString(path + ".display-item"));
            String displayName = ChatColor.translateAlternateColorCodes('&', shopConfig.getString(path + ".display-name"));
            List<String> loreConfig = shopConfig.getStringList(path + ".lore");
            List<String> lore = new ArrayList<>();
            for (String line : loreConfig) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            int slot = shopConfig.getInt(path + ".slot");

            ItemStack item = createShopItem(material, displayName, lore, key);
            shop.setItem(slot, item);
        }

        player.openInventory(shop);
    }

    private ItemStack createShopItem(Material material, String name, List<String> lore, String key) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> tempLore = new ArrayList<>(lore);
            tempLore.add(ChatColor.BLACK + "KEY:" + key);
            meta.setLore(tempLore);
            item.setItemMeta(meta);
            tempLore.remove(tempLore.size() - 1);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GREEN + "Mobcoins Shop")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            ItemMeta meta = currentItem.getItemMeta();

            if (meta != null && meta.getLore() != null && !meta.getLore().isEmpty()) {
                List<String> lore = meta.getLore();
                String key = null;
                for (String line : lore) {
                    if (line.startsWith(ChatColor.BLACK + "KEY:")) {
                        key = line.substring((ChatColor.BLACK + "KEY:").length());
                        break;
                    }
                }

                if (key == null || !shopConfig.contains("shop-items." + key)) {
                    player.sendMessage(ChatColor.RED + "Error: Item not found in shop configuration.");
                    return;
                }

                int cost = shopConfig.getInt("shop-items." + key + ".price");

                if (coinManager.getBalance(player) >= cost) {
                    coinManager.takeCoins(player, cost);

                    for (String command : shopConfig.getStringList("shop-items." + key + ".reward-commands")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                    }

                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.coins-purchased"))
                            .replace("%item%", ChatColor.stripColor(meta.getDisplayName()))
                            .replace("%cost%", String.valueOf(cost)));
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.not-enough-coins")));
                }
            }
        }
    }

    public void reloadConfigs(FileConfiguration newConfig, FileConfiguration newShopConfig) {
        this.config = newConfig;
        this.shopConfig = newShopConfig;
    }
}
