package plugily.projects.minigamesbox.api.kit.ability;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

/**
 * @author Lagggpixel
 * @since April 24, 2024
 */
public interface IKitAbility {
  String getName();

  Consumer<InventoryClickEvent> getClickConsumer();

  Consumer<Player> getCustomPlayerPluginConsumer();

  Consumer<BlockPlaceEvent> getBlockPlaceConsumer();

  Consumer<EntityDeathEvent> getDeathEventKillerConsumer();

  Consumer<EntityDamageByEntityEvent> getPlayerDamageConsumer();
}
