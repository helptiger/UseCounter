package net.maciekmm.usecounter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

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
    }

    private final UseCounter plugin;

    public ItemListener(UseCounter plugin) {
        this.plugin = plugin;
    }

    private void processEvent(Class event, Player player) {
        ItemStack is = player.getItemInHand();
        if (is.getType() == Material.AIR) {
            return;
        }

        if (!ITEM_MAPPINGS.get(event).contains(is.getType())) {
            return;
        }

        plugin.bumpUseName(is);
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
        if(!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }
        if(plugin.getCountOnlyOnPlayerKillsInSwords() && !(event.getEntity() instanceof Player)) {
            return;            
        }
        processEvent(event.getClass(), event.getEntity().getKiller());
    }
}
