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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.block.Action;
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
public class MaterialLocationItem implements CategoryItemHandler {

  private final SetupInventory setupInventory;
  private final ItemStack item;

  private final String name;
  private final String description;
  private final String keyName;
  private final Material checkMaterial;
  private final Consumer<InventoryClickEvent> clickConsumer;
  private final Consumer<PlugilyPlayerInteractEvent> interactConsumer;
  private final boolean rightClick;
  private final boolean leftClick;
  private final boolean physical;

  public MaterialLocationItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName, Material checkMaterial) {
    this(setupInventory, item, name, description, keyName, checkMaterial, emptyConsumer -> {
    }, emptyConsumer -> {
    });
  }

  public MaterialLocationItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName, Material checkMaterial, Consumer<InventoryClickEvent> clickConsumer, Consumer<PlugilyPlayerInteractEvent> interactConsumer) {
    this(setupInventory, item, name, description, keyName, checkMaterial, clickConsumer, interactConsumer, true, true, false);
  }

  public MaterialLocationItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName, Material checkMaterial, Consumer<InventoryClickEvent> clickConsumer, Consumer<PlugilyPlayerInteractEvent> interactConsumer, boolean leftClick, boolean rightClick, boolean physical) {
    this.setupInventory = setupInventory;
    this.name = name;
    this.description = description;
    this.keyName = keyName;
    this.checkMaterial = checkMaterial;
    item
        .name("&7添加 &a" + name.toUpperCase() + " &7位置")
        .lore("&a信息")
        .lore("&7" + description)
        .lore("&a状态")
        .lore("&7" + getSetupInfo())
        .lore("&a控制")
        .lore("&e左键 \n&7-> 添加" + name.toUpperCase() + "位于你*正在看*的位置")
        .lore("&eSHIFT+左键 \n&7-> 将安装物品放入你的库存")
        .lore("&e右键")
        .lore("&7-> 传送到当前位置")
        .lore("&eSHIFT+右键")
        .lore("&7-> 移除您位置附近的地点")
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
        Block targetBlock = event.getWhoClicked().getTargetBlock(null, 7);
        if(checkMaterial != targetBlock.getType()) {
          new MessageBuilder("&c&l✘ &c请只看向已经放置了 " + checkMaterial + " 的位置来添加为 " + name.toUpperCase() + "！").prefix().send(event.getWhoClicked());
          return;
        }
        addLocation(event.getWhoClicked(), targetBlock.getLocation());
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
                //.lore(physical ? "&e物理交互 \n&7-> 不支持" : "&c物理交互 - 已停用")
                .lore(leftClick ? "&e左键点击空气 \n&7-> 不支持" : "&c左键点击空气 - 已停用")
                .lore(leftClick ? "&e左键点击方块 \n&7-> 移除点击位置的位置" : "&c左键点击方块 - 已停用")
                .lore(rightClick ? "&e右键点击空气 \n&7-> 在位置间传送" : "&c右键点击空气 - 已停用")
                .lore(rightClick ? "&e右键点击方块 \n&7-> 添加点击位置的位置" : "&c右键点击方块 - 已停用")
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
          if(interactEvent.getClickedBlock() == null && (interactEvent.getAction() != Action.RIGHT_CLICK_AIR)) {
            new MessageBuilder("&c&l✘ &c你不能使用玩家所在位置的位置，请选择 " + checkMaterial + "！").prefix().send(interactEvent.getPlayer());
            return;
          }

          switch(interactEvent.getAction()) {
            case PHYSICAL:
            case LEFT_CLICK_AIR:
              new MessageBuilder("&c&l✘ &c你不能使用玩家所在位置的位置，请选择 " + checkMaterial + "！").prefix().send(interactEvent.getPlayer());
              break;
            case LEFT_CLICK_BLOCK:
              if(checkMaterial != interactEvent.getClickedBlock().getLocation().getBlock().getType()) {
                new MessageBuilder("&c&l✘ &c请只使用已经放置了 " + checkMaterial + " 的位置来移除 " + name.toUpperCase() + "！").prefix().send(interactEvent.getPlayer());
                return;
              }
              removeLocation(interactEvent.getPlayer());
              break;
            case RIGHT_CLICK_BLOCK:
              Location location = interactEvent.getClickedBlock().getLocation();
              if(checkMaterial != location.getBlock().getType()) {
                new MessageBuilder("&c&l✘ &c请只使用已经放置了 " + checkMaterial + " 的位置来添加为 " + name.toUpperCase() + "！").prefix().send(interactEvent.getPlayer());
                return;
              }

              if(location.distance(interactEvent.getClickedBlock().getWorld().getSpawnLocation()) <= Bukkit.getServer().getSpawnRadius()) {
                new MessageBuilder("&c&l✖ &c警告 | 服务器出生点保护半径设置为 &6" + Bukkit.getServer().getSpawnRadius()
                        + " &c并且你想要放置的位置在此保护半径内！&c&l没有OP权限的玩家将无法与此 " + checkMaterial + " 交互也无法加入游戏！请减小出生点保护半径（server.properties）或更改你的位置！").prefix().send(interactEvent.getPlayer());
              }
              addLocation(interactEvent.getPlayer(), location.getBlock().getLocation());
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
        new MessageBuilder("&a已传送到竞技场 " + setupInventory.getArenaKey() + " 的 " + name.toUpperCase() + " 位置").prefix().send(player);
        return;
      }
    }
    new MessageBuilder("&c未找到竞技场 " + setupInventory.getArenaKey() + " 的 " + name.toUpperCase() + " 位置").prefix().send(player);
  }

  private void addLocation(HumanEntity player, Location location) {
    LocationSerializer.saveLoc(setupInventory.getPlugin(), setupInventory.getConfig(), "arenas", "instances." + setupInventory.getArenaKey() + "." + keyName, location);
    new MessageBuilder("&e✔ 完成 | &a竞技场 " + setupInventory.getArenaKey() + " 的 " + name.toUpperCase() + " 位置已设置在你的当前位置！").prefix().send(player);
  }

  private void removeLocation(HumanEntity player) {
    if(getRawLocation() != null) {
      Location location = LocationSerializer.getLocation(getRawLocation());
      if(location != null) {
        double distance = player.getLocation().distanceSquared(location);
        if(distance <= 3) {
          setupInventory.setConfig(keyName, null);
          // 考虑添加竞技场方法来移除位置
          new MessageBuilder("&e✔ 已移除 | &a竞技场 " + setupInventory.getArenaKey() + " 的 " + name.toUpperCase() + " 位置！").prefix().send(player);
          return;
        }
      }
    }
    new MessageBuilder("&c在你的位置附近未找到 " + name.toUpperCase() + " 位置！").prefix().send(player);
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
