package net.maciekmm.usecounter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import net.maciekmm.usecounter.UseCounter.Result;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Represents a listener for events for specific @see{Material}
 *
 * @author maciekmm
 */
public class ItemListener implements Listener {

    final static HashMap<Class<? extends Event>, List<Material>> ITEM_MAPPINGS = new HashMap<>();

    static {
        ITEM_MAPPINGS.put(BlockBreakEvent.class, Arrays.asList(Material.DIAMOND_AXE, Material.DIAMOND_SPADE, Material.DIAMOND_PICKAXE, Material.GOLD_AXE, Material.GOLD_SPADE, Material.GOLD_PICKAXE, Material.IRON_AXE, Material.IRON_SPADE, Material.IRON_PICKAXE, Material.STONE_AXE, Material.STONE_SPADE, Material.STONE_PICKAXE, Material.WOOD_AXE, Material.WOOD_SPADE, Material.WOOD_PICKAXE));
        ITEM_MAPPINGS.put(PlayerShearEntityEvent.class, Arrays.asList(Material.SHEARS));
        ITEM_MAPPINGS.put(PlayerFishEvent.class, Arrays.asList(Material.FISHING_ROD));
        ITEM_MAPPINGS.put(EntityShootBowEvent.class, Arrays.asList(Material.BOW));
        ITEM_MAPPINGS.put(EntityDeathEvent.class, Arrays.asList(Material.DIAMOND_SWORD, Material.GOLD_SWORD, Material.IRON_SWORD, Material.WOOD_SWORD));
        ITEM_MAPPINGS.put(PlayerDeathEvent.class, Arrays.asList(Material.DIAMOND_SWORD, Material.GOLD_SWORD, Material.IRON_SWORD, Material.WOOD_SWORD));

    }

    private final UseCounter plugin;

    public ItemListener(UseCounter plugin) {
        this.plugin = plugin;
    }

    private void processEvent(Class event, Player player) {
        if(!player.hasPermission("usecounter.count")) {
            return;
        }
        ItemStack is = player.getItemInHand();
        if (is.getType() == Material.AIR) {
            return;
        }
        List<Material> materials = ITEM_MAPPINGS.get(event);
        if (materials == null || !materials.contains(is.getType())) {
            return;
        }

        if(plugin.bumpUseName(is)==Result.NOT_COUNTABLE) {
            plugin.setItemCountable(is);
            plugin.bumpUseName(is);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            if (event.getBlock().getType() != Material.AIR) { //Shitty ImmortalCustoms fix
                return;
            }
        }
        processEvent(event.getClass(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSheepShear(PlayerShearEntityEvent event) {
        processEvent(event.getClass(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH || event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
            processEvent(event.getClass(), event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        processEvent(event.getClass(), (Player) event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }
        if (plugin.getCountOnlyOnPlayerKillsInSwords() && !(event.getEntity() instanceof Player)) {
            return;
        }
        processEvent(event.getClass(), event.getEntity().getKiller());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (event.getInventory().getType() != InventoryType.WORKBENCH || !plugin.shouldSuffixBeAutomaticallyAdded()) {
            return;
        }
        plugin.setItemCountable(event.getCurrentItem());
    }

    //@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRename(InventoryClickEvent event) {
        if (event.getCurrentItem().getType() == Material.AIR || event.getInventory().getType() != InventoryType.ANVIL) {
            return;
        }
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        Matcher matcher = plugin.isCountableItem(meta.getDisplayName());
        if(matcher == null) {
            return;
        }
        if(event.getSlotType() == SlotType.CRAFTING) {
            //meta.setDisplayName(matcher.);
        } else if(event.getSlotType() == SlotType.RESULT) {
            
        }
    }
    
    private boolean isPut(InventoryAction action) {
        return action==InventoryAction.PLACE_ALL || action==InventoryAction.PLACE_ONE || action==InventoryAction.PLACE_SOME;
    }
}
