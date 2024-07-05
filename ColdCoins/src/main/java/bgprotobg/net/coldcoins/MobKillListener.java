package bgprotobg.net.coldcoins;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Random;

public class MobKillListener implements Listener {
    private final FileConfiguration config;
    private final Random random;
    private final CoinManager coinManager;

    public MobKillListener(CoinManager coinManager, FileConfiguration config) {
        this.coinManager = coinManager;
        this.config = config;
        this.random = new Random();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        EntityType entityType = event.getEntity().getType();
        String entityName = entityType.toString();

        if (config.contains("mobs." + entityName)) {
            if (event.getEntity().getKiller() instanceof Player) {
                Player player = event.getEntity().getKiller();

                double dropChancePercentage = config.getDouble("mobs." + entityName + ".drop-chance", 100.0);
                double dropChance = dropChancePercentage / 100.0;

                if (random.nextDouble() <= dropChance) {
                    int minCoins = config.getInt("mobs." + entityName + ".min-coins");
                    int maxCoins = config.getInt("mobs." + entityName + ".max-coins");

                    int mobcoins = minCoins + random.nextInt(maxCoins - minCoins + 1);
                    coinManager.giveCoins(player, mobcoins);
                    player.sendMessage(ChatColor.GREEN + "You have received " + mobcoins + " mobcoins for killing a " + entityName.toLowerCase() + "!");
                }
            }
        }
    }
}
