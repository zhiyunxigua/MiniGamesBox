/*
 *  MiniGamesBox - Library box with massive content that could be seen as minigames core.
 *  Copyright (C) 2023 Plugily Projects - maintained by Tigerpanzer_02 and contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package plugily.projects.minigamesbox.classic.handlers.setup.items.category;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventory;
import plugily.projects.minigamesbox.classic.utils.configuration.ConfigUtils;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.items.HandlerItem;
import plugily.projects.minigamesbox.classic.utils.serialization.LocationSerializer;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.events.api.PlugilyPlayerInteractEvent;
import plugily.projects.minigamesbox.inventory.common.RefreshableFastInv;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 31.12.2021
 */
public class MultiLocationItem implements CategoryItemHandler {

  private final SetupInventory setupInventory;
  private final ItemStack item;

  private final String name;
  private final String description;
  private final String keyName;

  private final int minimumValue;
  private final Consumer<InventoryClickEvent> clickConsumer;
  private final Consumer<PlugilyPlayerInteractEvent> interactConsumer;
  private final boolean rightClick;
  private final boolean leftClick;
  private final boolean physical;

  public MultiLocationItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName, int minimumValue) {
    this(setupInventory, item, name, description, keyName, minimumValue, emptyConsumer -> {
    }, emptyConsumer -> {
    });
  }

  public MultiLocationItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName, int minimumValue, Consumer<InventoryClickEvent> clickConsumer, Consumer<PlugilyPlayerInteractEvent> interactConsumer) {
    this(setupInventory, item, name, description, keyName, minimumValue, clickConsumer, interactConsumer, true, true, true);
  }
  public MultiLocationItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName, int minimumValue, Consumer<InventoryClickEvent> clickConsumer, Consumer<PlugilyPlayerInteractEvent> interactConsumer, boolean leftClick, boolean rightClick, boolean physical) {
    this.setupInventory = setupInventory;
    this.name = name;
    this.description = description;
    this.keyName = keyName;
    this.minimumValue = minimumValue;
    item
            .name("&7添加 &a" + name.toUpperCase() + " &7位置")
            .lore("&a信息")
            .lore("&7" + description)
            .lore("&a状态")
            .lore("&7" + getSetupInfo())
            .lore("&a控制")
            .lore("&e左键点击 \n&7-> 在你站立的位置添加位置")
            .lore("&eShift+左键点击 \n&7-> 将设置物品放入你的物品栏")
            .lore("&e右键点击 \n&7-> 移除你位置附近的位置")
            .lore("&eShift+右键点击 \n&7-> 移除所有位置")
            .colorizeItem();
    this.item = item.build();
    this.clickConsumer = clickConsumer;
    this.interactConsumer = interactConsumer;
    this.leftClick = leftClick;
    this.rightClick = rightClick;
    this.physical = physical;
  }


  @Override
  public ItemStack getItem() {
    return item;
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    switch(event.getClick()) {
      case LEFT:
        addLocation(event.getWhoClicked(), event.getWhoClicked().getLocation());
        break;
      case SHIFT_LEFT:
        ItemStack itemStack =
            new ItemBuilder(item.getType())
                .amount(1)
                .name("&7添加 &a" + name.toUpperCase() + " &7位置")
                .lore("&a信息")
                .lore("&7" + description)
                .lore("&a状态")
                .lore("&7在竞技场编辑器中查看！")
                .lore("&a控制")
                .lore("&e丢弃 \n&7-> 移除/停用物品")
                //.lore(physical ? "&e物理交互 \n&7-> 在物理事件上添加位置（例如压力板）" : "&c物理交互 - 已停用")
                .lore(leftClick ? "&e左键点击空气 \n&7-> 在你站立的位置添加位置" : "&c左键点击空气 - 已停用")
                .lore(leftClick ? "&e左键点击方块 \n&7-> 在你点击的位置添加位置" : "&c左键点击方块 - 已停用")
                .lore(rightClick ? "&e右键点击空气 \n&7-> 在位置间传送" : "&c右键点击空气 - 已停用")
                .lore(rightClick ? "&e右键点击方块 \n&7-> 移除你位置附近的位置" : "&c右键点击方块 - 已停用")
                .colorizeItem()
                .build();
        HandlerItem handlerItem = new HandlerItem(itemStack);
        handlerItem.addDropHandler(dropEvent -> {
          dropEvent.setCancelled(false);
          dropEvent.getItemDrop().remove();
          dropEvent.getPlayer().updateInventory();
          handlerItem.remove();
          new MessageBuilder("&a已移除/&a已停用 " + name.toUpperCase() + " 位置物品！").prefix().send(dropEvent.getPlayer());
        });
        handlerItem.addConsumeHandler(consumeEvent -> consumeEvent.setCancelled(true));
        handlerItem.addInteractHandler(interactEvent -> {
          interactEvent.setCancelled(true);
          switch(interactEvent.getAction()) {
            case PHYSICAL:
            case LEFT_CLICK_AIR:
              addLocation(interactEvent.getPlayer(), interactEvent.getPlayer().getLocation());
              new MessageBuilder("&c请记住使用方块而不是玩家位置来获得精确坐标！").prefix().send(interactEvent.getPlayer());
              break;
            case LEFT_CLICK_BLOCK:
              addLocation(interactEvent.getPlayer(), interactEvent.getClickedBlock().getLocation().clone().add(0, 1, 0));
              break;
            case RIGHT_CLICK_BLOCK:
              removeLocation(interactEvent.getPlayer(), false);
              break;
            case RIGHT_CLICK_AIR:
              teleport(interactEvent.getPlayer());
              break;
          }
          interactConsumer.accept(interactEvent);
        });
        handlerItem.setLeftClick(leftClick);
        handlerItem.setPhysical(physical);
        handlerItem.setRightClick(rightClick);
        event.getWhoClicked().getInventory().addItem(handlerItem.getItemStack());
        break;
      case RIGHT:
        removeLocation(event.getWhoClicked(), false);
        break;
      case SHIFT_RIGHT:
        removeLocation(event.getWhoClicked(), true);
        break;
      default:
        break;
    }
    clickConsumer.accept(event);
    setupInventory.closeInventory(event.getWhoClicked());
    InventoryHolder holder = event.getInventory().getHolder();
    if(holder instanceof RefreshableFastInv) {
      ((RefreshableFastInv) holder).refresh();
    }
  }

  private void teleport(HumanEntity player) {
    if(!getLocationsList().isEmpty()) {
      Location location = getLocationsList().get(setupInventory.getPlugin().getRandom().nextInt(getLocationsList().size()));
      VersionUtils.teleport(player, location);
      new MessageBuilder("&a已传送到竞技场 " + setupInventory.getArenaKey() + " 的 " + name.toUpperCase() + " 位置（" + location + "）").prefix().send(player);
      return;
    }
    new MessageBuilder("&c未找到竞技场 " + setupInventory.getArenaKey() + " 的 " + name.toUpperCase() + " 位置").prefix().send(player);
  }

  private void addLocation(HumanEntity player, Location location) {
    FileConfiguration config = ConfigUtils.getConfig(setupInventory.getPlugin(), "arenas");
    List<String> locs = config.getStringList("instances." + setupInventory.getArenaKey() + "." + keyName);
    String spawnLoc = location.getBlock().getWorld().getName() + "," + location.getBlock().getX() + "," + location.getBlock().getY() + "," + location.getBlock().getZ() + ",0.0,0.0";
    if(!locs.contains(spawnLoc)) {
      locs.add(spawnLoc);
      config.set("instances." + setupInventory.getArenaKey() + "." + keyName, locs);
      ConfigUtils.saveConfig(setupInventory.getPlugin(), config, "arenas");
    }

    String progress = locs.size() >= minimumValue ? "&e✔ 完成 | " : "&c✘ 未完成 | ";
    new MessageBuilder(progress + "&a" + name.toUpperCase() + " 出生点已添加！ &8(&7" + locs.size() + "/" + minimumValue + "&8)").prefix().send(player);
    if(locs.size() == minimumValue) {
      new MessageBuilder("&e信息 | &a你可以添加超过 " + minimumValue + " 个 " + name.toUpperCase() + " 出生点！" + minimumValue + " 只是最低要求！").prefix().send(player);
    }
  }

  private void removeLocation(HumanEntity player, boolean deleteAll) {
    if(!getLocationsList().isEmpty()) {
      for(Location location : getLocationsList()) {
        double distance = player.getLocation().distanceSquared(location);
        if(deleteAll || distance <= 2) {
          FileConfiguration config = ConfigUtils.getConfig(setupInventory.getPlugin(), "arenas");
          List<String> locs = config.getStringList("instances." + setupInventory.getArenaKey() + "." + keyName);
          if(deleteAll) {
            locs.clear();
          } else {
            String signLoc = location.getBlock().getWorld().getName() + "," + location.getBlock().getX() + "," + location.getBlock().getY() + "," + location.getBlock().getZ() + ",0.0,0.0";
            locs.remove(signLoc);
          }
          config.set("instances." + setupInventory.getArenaKey() + "." + keyName, locs);
          ConfigUtils.saveConfig(setupInventory.getPlugin(), config, "arenas");
          // 考虑添加竞技场方法来移除位置
          new MessageBuilder("&e✔ 已移除 | &a竞技场 " + setupInventory.getArenaKey() + " 的 " + name.toUpperCase() + " 位置！（" + location + "）").prefix().send(player);
          return;
        }
      }
    }
    new MessageBuilder("&c在你的位置附近未找到 " + name.toUpperCase() + " 位置！").prefix().send(player);
  }

  @Nullable
  private List<String> getRawLocations() {
    return setupInventory.getConfig().getStringList("instances." + setupInventory.getArenaKey() + "." + keyName);
  }

  private List<Location> getLocationsList() {
    List<Location> locations = new ArrayList<>();
    List<String> configurationSection = getRawLocations();
    if(!configurationSection.isEmpty()) {
      for(String key : configurationSection) {
        locations.add(LocationSerializer.getLocation(key));
      }
    }
    return locations;
  }


  @Override
  public String getSetupInfo() {
    return setupInventory.isLocationSectionOptionDone(keyName, minimumValue);
  }

  @Override
  public boolean getSetupStatus() {
    return getSetupInfo().contains("✔");
  }
}
