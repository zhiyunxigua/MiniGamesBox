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
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventory;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.items.HandlerItem;
import plugily.projects.minigamesbox.classic.utils.serialization.LocationSerializer;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.events.api.PlugilyPlayerInteractEvent;
import plugily.projects.minigamesbox.inventory.common.RefreshableFastInv;

import java.util.function.Consumer;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 31.12.2021
 */
public class LocationItem implements CategoryItemHandler {

  private final SetupInventory setupInventory;
  private final ItemStack item;

  private final String name;
  private final String description;
  private final String keyName;
  private final Consumer<InventoryClickEvent> clickConsumer;
  private final Consumer<PlugilyPlayerInteractEvent> interactConsumer;
  private final boolean rightClick;
  private final boolean leftClick;
  private final boolean physical;

  public LocationItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName) {
    this(setupInventory, item, name, description, keyName, emptyConsumer -> {
    }, emptyConsumer -> {
    });
  }

  public LocationItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName, Consumer<InventoryClickEvent> clickConsumer, Consumer<PlugilyPlayerInteractEvent> interactConsumer) {
    this(setupInventory, item, name, description, keyName, clickConsumer, interactConsumer, true, true, true);
  }

  public LocationItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName, Consumer<InventoryClickEvent> clickConsumer, Consumer<PlugilyPlayerInteractEvent> interactConsumer, boolean leftClick, boolean rightClick, boolean physical) {
    this.setupInventory = setupInventory;
    this.name = name;
    this.description = description;
    this.keyName = keyName;
    item
        .name("&7设置 &a" + name.toUpperCase() + " &7位置")
        .lore("&a信息")
        .lore("&7" + description)
        .lore("&a状态")
        .lore("&7" + getSetupInfo())
        .lore("&a控制")
        .lore("&e左键点击")
        .lore("&7-> 在你所在的位置设置位置")
        .lore("&eShift + 左键点击")
        .lore("&7-> 将设置物品放入你的物品栏")
        .lore("&e右键点击")
        .lore("&7-> 传送到当前位置")
        .lore("&eShift + 右键点击")
        .lore("&7-> 移除你附近的位置")
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
                .name("&7设置 &a" + name.toUpperCase() + " &7位置")
                .lore("&a信息")
                .lore("&7" + description)
                .lore("&a状态")
                .lore("&7在竞技场编辑器中检查！")
                .lore("&a控制")
                .lore("&e丢下 &7- 移除/停用该物品")
                //.lore(physical ? "&ePHYSICAL \n&7-> Set a location on physical event (e.g. pressure plate)" : "&cPHYSICAL - DEACTIVATED")
                .lore(leftClick ? "&e左键空气 \n&7-> 将位置设置在您当前站立的位置" : "&c左键空气 - 已停用")
                .lore(leftClick ? "&e左键方块 \n&7-> 将位置设置到你点击的位置" : "&c左键方块 - 已停用")
                .lore(rightClick ? "&e右键空气 \n&7-> 传送到当前位置" : "&c右键空气 - 已停用")
                .lore(rightClick ? "&e右键方块 \n&7-> 移除您位置附近的地点" : "&c右键方块 - 已停用")
                .colorizeItem()
                .build();
        HandlerItem handlerItem = new HandlerItem(itemStack);
        handlerItem.addDropHandler(dropEvent -> {
          dropEvent.setCancelled(false);
          dropEvent.getItemDrop().remove();
          dropEvent.getPlayer().updateInventory();
          handlerItem.remove();
          new MessageBuilder("&a已移除/&a已停用" + name.toUpperCase() + "位置项!").prefix().send(dropEvent.getPlayer());
        });
        handlerItem.addConsumeHandler(consumeEvent -> consumeEvent.setCancelled(true));
        handlerItem.addInteractHandler(interactEvent -> {
          interactEvent.setCancelled(true);
          switch(interactEvent.getAction()) {
            case PHYSICAL:
            case LEFT_CLICK_AIR:
              addLocation(interactEvent.getPlayer(), interactEvent.getPlayer().getLocation());
              new MessageBuilder("&c请记住使用方块而不是玩家位置来获取精确坐标!").prefix().send(interactEvent.getPlayer());
              break;
            case RIGHT_CLICK_BLOCK:
              removeLocation(interactEvent.getPlayer());
              break;
            case RIGHT_CLICK_AIR:
              teleport(interactEvent.getPlayer());
              break;
            case LEFT_CLICK_BLOCK:
              addLocation(interactEvent.getPlayer(), interactEvent.getClickedBlock().getLocation().clone().add(0, 1, 0));
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
        teleport(event.getWhoClicked());
        break;
      case SHIFT_RIGHT:
        removeLocation(event.getWhoClicked());
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
    if(getRawLocation() != null) {
      Location location = LocationSerializer.getLocation(getRawLocation());
      if(location != null) {
        VersionUtils.teleport(player, location);
        new MessageBuilder("&a传送到" + name.toUpperCase() + " 位置在竞技场 " + setupInventory.getArenaKey()).prefix().send(player);
        return;
      }
    }
    new MessageBuilder("&c" + name.toUpperCase() + " 未找到位置在竞技场 " + setupInventory.getArenaKey()).prefix().send(player);
  }

  private void addLocation(HumanEntity player, Location location) {
    LocationSerializer.saveLoc(setupInventory.getPlugin(), setupInventory.getConfig(), "arenas", "instances." + setupInventory.getArenaKey() + "." + keyName, location);
    new MessageBuilder("&e✔ 已完成 | &a" + name.toUpperCase() + " 位置在竞技场 " + setupInventory.getArenaKey() + " 在你的位置设定!").prefix().send(player);
  }

  private void removeLocation(HumanEntity player) {
    if(getRawLocation() != null) {
      Location location = LocationSerializer.getLocation(getRawLocation());
      if(location != null) {
        double distance = player.getLocation().distanceSquared(location);
        if(distance <= 3) {
          setupInventory.setConfig(keyName, null);
          //considerable to add arena method to remove location
          new MessageBuilder("&e✔ 已移除 | &a" + name.toUpperCase() + " 位置在竞技场 " + setupInventory.getArenaKey() + "!").prefix().send(player);
          return;
        }
      }
    }
    new MessageBuilder("&c你的位置周围没有 " + name.toUpperCase() + "找到竞技场!").prefix().send(player);
  }

  @Nullable
  private String getRawLocation() {
    return setupInventory.getConfig().getString("instances." + setupInventory.getArenaKey() + "." + keyName);
  }

  @Override
  public String getSetupInfo() {
    return setupInventory.isLocationOptionDone(keyName);
  }

  @Override
  public boolean getSetupStatus() {
    return getSetupInfo().contains("✔");
  }
}
