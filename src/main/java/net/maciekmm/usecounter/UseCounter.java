package net.maciekmm.usecounter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class UseCounter extends JavaPlugin implements Listener {
    
    public enum Result {
        
        NOT_COUNTABLE(ChatColor.RED + "Item you are holding is not supported."), ALREADY_COUNTABLE(ChatColor.RED + "Your item is already countable."), SUCCESS(ChatColor.GREEN + "Successfully set your item to countable.");
        
        private final String message;
        
        private Result(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return this.message;
        }
    }
    
    private Pattern matcher;
    private int group;
    private String suffix;
    private boolean countOnlyOnPlayerKillsInSwords;
    private boolean autoAddSuffix;
    private ItemListener itemListener;
    
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.matcher = Pattern.compile(this.getConfig().getString("matcher", "\\[(&[1-9a-f])?([0-9]+)(&[1-9a-f])?\\]").replace('&', '�'));
        this.group = this.getConfig().getInt("group", 2);
        this.suffix = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("defaultSuffix", " &8[&2{number}&8]")).replace("{number}", "0");
        this.countOnlyOnPlayerKillsInSwords = this.getConfig().getBoolean("countOnlyOnPlayerKillsInSwords", true);
        this.autoAddSuffix = this.getConfig().getBoolean("addSuffixWhenCrafting", false);
        this.itemListener = new ItemListener(this);
        this.getServer().getPluginManager().registerEvents(itemListener, this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You have to be a player to cast this command.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            sender.sendMessage(this.setItemCountable(player.getItemInHand()).getMessage());
        } else {
            if (args[0].equalsIgnoreCase("remove")) {
                ItemMeta meta = player.getItemInHand().getItemMeta();
                Matcher mat = this.isCountableItem(meta.getDisplayName());
                if (mat == null) {
                    sender.sendMessage(ChatColor.RED + "Item you are holding has no counter.");
                    return true;
                }
                meta.setDisplayName(mat.replaceAll(""));
                player.getItemInHand().setItemMeta(meta);
                sender.sendMessage(ChatColor.GREEN + "Your item is no longer countable.");
            }
        }
        return true;
    }

    /**
     * Represents if current item has item counter in name.
     *
     * @param displayName - name to check
     * @return Matcher for displayName.
     */
    public Matcher isCountableItem(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return null;
        }
        Matcher globalMatch = matcher.matcher(displayName);
        return globalMatch.find() ? globalMatch : null;
    }

    /**
     * Sets item to countable state
     *
     * @param is - item to set countable
     * @return Outcome of event, if it was successfull or not.
     */
    public Result setItemCountable(ItemStack is) {
        ItemMeta meta = is.getItemMeta();
        
        if (meta == null) {
            return Result.NOT_COUNTABLE;
        }
        
        if (isCountableItem(meta.getDisplayName()) != null) {
            return Result.ALREADY_COUNTABLE;
        }
        
        if (!canBeCountable(is.getType())) {
            return Result.NOT_COUNTABLE;
        }
        
        String newName = meta.getDisplayName();
        if (newName == null || newName.isEmpty()) {
            newName = WordUtils.capitalize(is.getType().toString().toLowerCase().replace('_', ' '));
        }
        meta.setDisplayName(ChatColor.RESET + newName + suffix);
        is.setItemMeta(meta);
        return Result.SUCCESS;
    }

    /**
     * Checks whether specified @see{Material} is countable.
     *
     * @param material - checked @see{Material}
     * @return whether or not @see{Material} is countable
     */
    public boolean canBeCountable(Material material) {
        for (List<Material> materials : ItemListener.ITEM_MAPPINGS.values()) {
            if (materials.contains(material)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Bumps a stat of item in name by one
     *
     * @param is - ItemStack to bump count on.
     * @return if stat was bumped.
     */
    public Result bumpUseName(ItemStack is) {
        ItemMeta meta = is.getItemMeta();
        if (meta == null) {
            return Result.NOT_COUNTABLE;
        }
        
        Matcher globalMatch = isCountableItem(meta.getDisplayName());
        if (globalMatch == null) {
            return Result.NOT_COUNTABLE;
        }
        
        meta.setDisplayName(new StringBuilder(meta.getDisplayName()).replace(globalMatch.start(this.group), globalMatch.end(this.group), String.valueOf(Long.valueOf(globalMatch.group(this.group)) + 1)).toString());
        is.setItemMeta(meta);
        return Result.SUCCESS;
    }
    
    int getGroupToChange() {
        return group;
    }
    
    Pattern getPatternMatcher() {
        return matcher;
    }
    
    boolean getCountOnlyOnPlayerKillsInSwords() {
        return countOnlyOnPlayerKillsInSwords;
    }
    
    boolean shouldSuffixBeAutomaticallyAdded() {
        return autoAddSuffix;
    }
}
