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
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventory;
import plugily.projects.minigamesbox.classic.utils.dimensional.CuboidSelector;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.serialization.LocationSerializer;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.inventory.common.RefreshableFastInv;

import java.util.function.Consumer;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 31.12.2021
 */
public class LocationSelectorItem implements CategoryItemHandler {

  private final SetupInventory setupInventory;
  private final ItemStack item;

  private final String name;
  private final String description;
  private final String keyName;
  private final Consumer<InventoryClickEvent> clickConsumer;

  public LocationSelectorItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName) {
    this(setupInventory, item, name, description, keyName, emptyConsumer -> {
    });
  }

  public LocationSelectorItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName, Consumer<InventoryClickEvent> clickConsumer) {
    this.setupInventory = setupInventory;
    this.name = name;
    this.description = description;
    this.keyName = keyName;
    item
        .name("&7设置 &a" + name.toUpperCase() + " &7地点选择")
        .lore("&a信息")
        .lore("&7" + description)
        .lore("&a状态")
        .lore("&7" + getSetupInfo())
        .lore("&a控制")
        .lore("&e左键 \n&7-> 设置选择器项的位置")
        .lore("&eSHIFT+左键 \n&7-> 将选择器物品放入你的物品栏")
        .lore("&e右键 \n&7-> 传送到位置1")
        .lore("&eSHIFT+右键 \n&7-> 移除您位置附近的地点")
        .colorizeItem();
    this.item = item.build();
    this.clickConsumer = clickConsumer;
  }


  @Override
  public ItemStack getItem() {
    return item;
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    switch(event.getClick()) {
      case LEFT:
        addLocation(event.getWhoClicked());
        break;
      case SHIFT_LEFT:
        setupInventory.getPlugin().getCuboidSelector().giveSelectorWand((Player) event.getWhoClicked());
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
        new MessageBuilder("&a传送到" + name.toUpperCase() + "位置在竞技场" + setupInventory.getArenaKey()).prefix().send(player);
        return;
      }
    }
    new MessageBuilder("&c" + name.toUpperCase() + "位置没有找到在竞技场" + setupInventory.getArenaKey()).prefix().send(player);
  }

  private void addLocation(HumanEntity player) {
    CuboidSelector.Selection selection = setupInventory.getPlugin().getCuboidSelector().getSelection((Player) player);
    if(selection == null || selection.getFirstPos() == null || selection.getSecondPos() == null) {
      new MessageBuilder("&c请在添加前选择两个角" + name.toUpperCase() + "位置!").prefix().send(player);
      return;
    }

    LocationSerializer.saveLoc(setupInventory.getPlugin(), setupInventory.getConfig(), "arenas", "instances." + setupInventory.getArenaKey() + "." + keyName + ".1", selection.getFirstPos());
    LocationSerializer.saveLoc(setupInventory.getPlugin(), setupInventory.getConfig(), "arenas", "instances." + setupInventory.getArenaKey() + "." + keyName + ".2", selection.getSecondPos());
    new MessageBuilder("&e✔ 已完成 | &a" + name.toUpperCase() + "位置在竞技场" + setupInventory.getArenaKey() + "设置在你的位置!").prefix().send(player);
  }

  private void removeLocation(HumanEntity player) {
    if(getRawLocation() != null) {
      Location location = LocationSerializer.getLocation(getRawLocation());
      if(location != null) {
        double distance = player.getLocation().distanceSquared(location);
        if(distance <= 3) {
          setupInventory.setConfig(keyName, null);
          //considerable to add arena method to remove location
          new MessageBuilder("&e✔ 已移除 | &a" + name.toUpperCase() + "位置在竞技场" + setupInventory.getArenaKey() + "!").prefix().send(player);
          return;
        }
      }
    }
    new MessageBuilder("&c你的位置周围没有" + name.toUpperCase() + "找到竞技场!").prefix().send(player);
  }

  @Nullable
  private String getRawLocation() {
    return setupInventory.getConfig().getString("instances." + setupInventory.getArenaKey() + "." + keyName + ".1");
  }

  @Override
  public String getSetupInfo() {
    return setupInventory.isLocationOptionDone(keyName + ".1");
  }

  @Override
  public boolean getSetupStatus() {
    return getSetupInfo().contains("✔");
  }
}
