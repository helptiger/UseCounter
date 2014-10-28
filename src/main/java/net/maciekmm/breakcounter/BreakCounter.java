package net.maciekmm.breakcounter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class BreakCounter extends JavaPlugin implements Listener {

    private Pattern matcher;
    private int group;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.matcher = Pattern.compile(this.getConfig().getString("matcher", "\\[(&[1-9a-f])?([0-9]+)(&[1-9a-f])?\\]").replace('&', '§'));
        this.group = this.getConfig().getInt("group", 2);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.isCancelled()) {
            if(event.getBlock().getType()!=Material.AIR) {
                return;
            }
        }
        ItemStack is = event.getPlayer().getItemInHand();
        if (is.getType() == Material.AIR) {
            return;
        }
        ItemMeta meta = is.getItemMeta();
        if (meta == null) {
            return;
        }
        String display = meta.getDisplayName();
        if (display == null || display.isEmpty()) {
            return;
        }
        Matcher globalMatch = matcher.matcher(display);
        if (!globalMatch.find()) {
            return;
        }
        meta.setDisplayName(new StringBuilder(display).replace(globalMatch.start(this.group), globalMatch.end(this.group), String.valueOf(Integer.valueOf(globalMatch.group(this.group)) + 1)).toString());
        is.setItemMeta(meta);
    }
}
