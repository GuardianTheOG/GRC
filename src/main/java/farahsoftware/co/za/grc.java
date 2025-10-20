package farahsoftware.co.za;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.broadcastMessage;

public class grc extends JavaPlugin implements Listener {

    private final Map<String, Long> cooldowns = new HashMap<>();
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("grc")).setExecutor(new RCCommand(this));
        Objects.requireNonNull(getCommand("grc")).setTabCompleter(new RCTabCompleter(this));
        Logger logger = Bukkit.getLogger();
        logger.info("<<<<< GRC Plugin enabled >>>>>");
    }

    public void onDisable() {
        Logger logger = Bukkit.getLogger();
        logger.info("<<<<< GRC Plugin Disabled >>>>>");
    }

    public FileConfiguration cfg() {
        return getConfig();
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) return;

        Player trigger = event.getPlayer();
        String targetName = target.getName();

        ConfigurationSection playerSec = cfg().getConfigurationSection("players");
        if (playerSec == null || !playerSec.isConfigurationSection(targetName)) return;

        ConfigurationSection sec = playerSec.getConfigurationSection(targetName);
        if (sec == null) return;

        Material required = materialOf(sec.getString("triggeritem"));
        if (required == null) return;

        ItemStack held = trigger.getInventory().getItemInMainHand();
        if (held == null || held.getType() != required) return;

        int cooldownMinutes = sec.getInt("cooldown", 5);
        String key = targetName.toLowerCase(Locale.ROOT);
        Long now = System.currentTimeMillis();
        Long last = cooldowns.get(key);
        Long cdMillis = cooldownMinutes * 60000L;

        if (last != null && (now - last) < cdMillis) {
            String failed = colorizeMini(sec.getString("failed", "<red>{target} is still on cooldown"));
            broadcastMessage(gformat(failed, trigger.getName(), targetName));
            return;
        }

        cooldowns.put(key, now);

        String success = colorizeMini(sec.getString("success", "<green>{target} successfully triggered"));
        broadcastMessage(gformat(success, trigger.getName(), targetName));

        ItemStack give = readGiveItem(sec);
        if (give != null && give.getType() != Material.AIR) {
            trigger.getWorld().dropItemNaturally(trigger.getLocation(), give);
        }
    }

    static String gformat(String msg, String trigger, String target) {
        return msg.replace("{trigger}", trigger).replace("{target}", target);
    }
    static Material materialOf(String name) {
        if (name == null) return null;
        return Material.matchMaterial(name);
    }
    static ItemStack readGiveItem(ConfigurationSection sec) {
        String type = sec.getString("giveitem.type");
        Material mat = materialOf(type);
        if (mat == null) return null;
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            String display = sec.getString("giveitem.name");
            if (display != null) meta.setDisplayName(colorAmpersand(display));
            List<String> rawLore = sec.getStringList("giveitem.lore");
            if (!rawLore.isEmpty()) {
                List<String> lore = new ArrayList<>((rawLore.size()));
                for (String line : rawLore) lore.add(colorAmpersand(line));
                meta.setLore(lore);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    static String colorizeMini(String input) {
        if (input == null) return "";
        String out = input;
        out = out.replace("<red>", ChatColor.RED.toString());
        out = out.replace("<dark_red>", ChatColor.DARK_RED.toString());
        out = out.replace("<green>", ChatColor.GREEN.toString());
        out = out.replace("<dark_green>", ChatColor.DARK_GREEN.toString());
        out = out.replace("<yellow>", ChatColor.YELLOW.toString());
        out = out.replace("<gold>", ChatColor.GOLD.toString());
        out = out.replace("<aqua>", ChatColor.AQUA.toString());
        out = out.replace("<dark_aqua>", ChatColor.DARK_AQUA.toString());
        out = out.replace("<blue>", ChatColor.BLUE.toString());
        out = out.replace("<dark_blue>", ChatColor.DARK_BLUE.toString());
        out = out.replace("<gray>", ChatColor.GRAY.toString());
        out = out.replace("<grey>", ChatColor.GRAY.toString());
        out = out.replace("<dark_gray>", ChatColor.DARK_GRAY.toString());
        out = out.replace("<dark_grey>", ChatColor.DARK_GRAY.toString());
        out = out.replace("<light_purple>", ChatColor.LIGHT_PURPLE.toString());
        out = out.replace("<dark_purple>", ChatColor.DARK_PURPLE.toString());
        out = out.replace("<black>", ChatColor.BLACK.toString());
        out = out.replace("<magic>", ChatColor.MAGIC.toString());
        out = out.replace("<bold>", ChatColor.BOLD.toString());
        out = out.replace("<italic>", ChatColor.ITALIC.toString());
        out = out.replace("<strikethrough>", ChatColor.STRIKETHROUGH.toString());
        out = out.replace("<underline>", ChatColor.UNDERLINE.toString());
        out = out.replace("<white>", ChatColor.WHITE.toString());
        out = out.replace("<reset>", ChatColor.RESET.toString());
        return out;
    }

    static String colorAmpersand(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }


}