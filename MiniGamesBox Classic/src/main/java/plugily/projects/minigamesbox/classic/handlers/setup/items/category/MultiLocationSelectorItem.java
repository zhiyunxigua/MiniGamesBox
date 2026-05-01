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
import org.bukkit.configuration.ConfigurationSection;
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
import plugily.projects.minigamesbox.inventory.common.RefreshableFastInv;

import java.util.function.Consumer;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 31.12.2021
 */
public class MultiLocationSelectorItem implements CategoryItemHandler {

  private final SetupInventory setupInventory;
  private final ItemStack item;

  private final String name;
  private final String description;
  private final String keyName;

  private final int minimumValue;
  private final Consumer<InventoryClickEvent> clickConsumer;

  public MultiLocationSelectorItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName, int minimumValue) {
    this(setupInventory, item, name, description, keyName, minimumValue, emptyConsumer -> {
    });
  }

  public MultiLocationSelectorItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName, int minimumValue, Consumer<InventoryClickEvent> clickConsumer) {
    this.setupInventory = setupInventory;
    this.name = name;
    this.description = description;
    this.keyName = keyName;
    this.minimumValue = minimumValue;
    item
        .name("&7添加 &a" + name.toUpperCase() + " &7位置选择")
        .lore("&a信息")
        .lore("&7" + description)
        .lore("&a状态")
        .lore("&7" + getSetupInfo())
        .lore("&a控制")
        .lore("&e左键点击 \n&7-> 添加你用选择器选择的位置")
        .lore("&eShift+左键点击 \n&7-> 将选择器物品放入你的物品栏")
        .lore("&e右键点击 \n&7-> 移除你位置附近的位置")
        .lore("&eShift+右键点击 \n&7-> 移除所有位置")
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

  private void addLocation(HumanEntity player) {
    CuboidSelector.Selection selection = setupInventory.getPlugin().getCuboidSelector().getSelection((Player) player);
    if(selection == null || selection.getFirstPos() == null || selection.getSecondPos() == null) {
      new MessageBuilder("&c请在添加 " + name.toUpperCase() + " 位置前选择两个角点！").prefix().send(player);
      return;
    }

    ConfigurationSection configurationSection = getRawLocations();
    int value = (configurationSection != null ? configurationSection.getKeys(false).size() : 0) + 1;
    LocationSerializer.saveLoc(setupInventory.getPlugin(), setupInventory.getConfig(), "arenas", "instances." + setupInventory.getArenaKey() + "." + keyName + "." + value + ".1", selection.getFirstPos());
    LocationSerializer.saveLoc(setupInventory.getPlugin(), setupInventory.getConfig(), "arenas", "instances." + setupInventory.getArenaKey() + "." + keyName + "." + value + ".2", selection.getSecondPos());
    String progress = value >= minimumValue ? "&e✔ 完成 | " : "&c✘ 未完成 | ";
    new MessageBuilder(progress + "&a" + name.toUpperCase() + " 出生点已添加！ &8(&7" + value + "/" + minimumValue + "&8)").prefix().send(player);
    if(value == minimumValue) {
      new MessageBuilder("&e信息 | &a你可以添加超过 " + minimumValue + " 个 " + name.toUpperCase() + " 出生点！" + minimumValue + " 只是最低要求！").prefix().send(player);
    }
  }

  private void removeLocation(HumanEntity player, boolean deleteAll) {
    if(deleteAll) {
      new MessageBuilder("&e✔ 已移除 | &a竞技场 " + setupInventory.getArenaKey() + " 的 " + name.toUpperCase() + " 位置！（所有位置）").prefix().send(player);
      setupInventory.getConfig().set("instances." + setupInventory.getArenaKey() + "." + keyName, null);
      return;
    }
    ConfigurationSection configurationSection = getRawLocations();
    if(configurationSection != null) {
      for(String key : configurationSection.getKeys(false)) {
        Location location1 = LocationSerializer.getLocation(configurationSection.getString(key + ".1"));
        Location location2 = LocationSerializer.getLocation(configurationSection.getString(key + ".2"));

        double distance1 = player.getLocation().distanceSquared(location1);
        double distance2 = player.getLocation().distanceSquared(location2);
        if(distance1 <= 2 || distance2 <= 2) {
          setupInventory.setConfig(keyName + "." + key, null);
          // 考虑添加竞技场方法来移除位置
          new MessageBuilder("&e✔ 已移除 | &a竞技场 " + setupInventory.getArenaKey() + " 的 " + name.toUpperCase() + " 位置！（" + location1 + "）").prefix().send(player);
          return;
        }
      }
    }
    new MessageBuilder("&c在你的位置附近未找到 " + name.toUpperCase() + " 位置！").prefix().send(player);
  }

  @Nullable
  private ConfigurationSection getRawLocations() {
    return setupInventory.getConfig().getConfigurationSection("instances." + setupInventory.getArenaKey() + "." + keyName);
  }


  @Override
  public String getSetupInfo() {
    return setupInventory.isSectionOptionDone(keyName, minimumValue);
  }

  @Override
  public boolean getSetupStatus() {
    return getSetupInfo().contains("✔");
  }
}
