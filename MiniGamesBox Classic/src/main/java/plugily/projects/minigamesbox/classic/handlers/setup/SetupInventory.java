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

package plugily.projects.minigamesbox.classic.handlers.setup;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import plugily.projects.minigamesbox.classic.PluginMain;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.setup.inventories.ArenaEditorInventory;
import plugily.projects.minigamesbox.classic.handlers.setup.inventories.ArenaListInventory;
import plugily.projects.minigamesbox.classic.handlers.setup.inventories.HomeInventory;
import plugily.projects.minigamesbox.classic.utils.configuration.ConfigUtils;
import plugily.projects.minigamesbox.classic.utils.serialization.LocationSerializer;
import plugily.projects.minigamesbox.classic.utils.version.TextComponentBuilder;

import java.util.List;

/**
 * @author Tigerpanzer_02
 * <p>Created at 21.06.2022
 */
public class SetupInventory {
  // ToDo Recommend player setup settings, survival and fly active
  private SetupInventoryUtils.SetupInventoryStage inventoryStage;
  private String arenaKey = null;
  private final Player player;
  private final PluginMain plugin;

  public SetupInventory(PluginMain plugin, Player player) {
    this.plugin = plugin;
    this.player = player;
    this.inventoryStage = SetupInventoryUtils.SetupInventoryStage.HOME;
  }

  public SetupInventory(PluginMain plugin, Player player, String arenaKey) {
    this.plugin = plugin;
    this.player = player;
    this.arenaKey = arenaKey;
    this.inventoryStage = SetupInventoryUtils.SetupInventoryStage.ARENA_EDITOR;
  }

  public SetupInventory(PluginMain plugin, Player player, String arenaKey, SetupInventoryUtils.SetupInventoryStage inventoryStage) {
    this.plugin = plugin;
    this.player = player;
    this.arenaKey = arenaKey;
    this.inventoryStage = inventoryStage;
  }

  public void open() {
    switch(inventoryStage) {
      default:
      case HOME:
        new HomeInventory(54, plugin.getPluginMessagePrefix() + "设置菜单", this).open(player);
        break;
      case ARENA_LIST:
        if(plugin.getArenaRegistry().getArenas().isEmpty()) {
          new MessageBuilder("&c没有竞技场。请先创建一个！").prefix().send(player);
          return;
        }
        new ArenaListInventory(54, plugin.getPluginMessagePrefix() + "设置菜单 | 竞技场", this).open(player);
        break;
      case ARENA_EDITOR:
        new ArenaEditorInventory(54, plugin.getPluginMessagePrefix() + "竞技场编辑器菜单", this).open(player);
        break;
    }
    sendProTip(player);
  }

  public void open(SetupInventoryUtils.SetupInventoryStage inventoryStage) {
    setInventoryStage(inventoryStage);
    open();
  }

  public void setInventoryStage(SetupInventoryUtils.SetupInventoryStage inventoryStage) {
    this.inventoryStage = inventoryStage;
  }

  public SetupInventoryUtils.SetupInventoryStage getInventoryStage() {
    return inventoryStage;
  }

  public void setArenaKey(String arenaKey) {
    SetupInventoryUtils.addSetupInventory(player, arenaKey);
    this.arenaKey = arenaKey;
  }

  public String getArenaKey() {
    String newKey = SetupInventoryUtils.getArenaKey(player);
    if(arenaKey == null && newKey != null) {
      return newKey;
    }
    return arenaKey;
  }

  public Player getPlayer() {
    return player;
  }

  public PluginMain getPlugin() {
    return plugin;
  }

  public FileConfiguration getConfig() {
    return ConfigUtils.getConfig(plugin, "arenas");
  }

  public void setConfig(String keyName, Object value) {
    FileConfiguration arenasFile = getConfig();
    arenasFile.set("instances." + getArenaKey() + "." + keyName, value);
    ConfigUtils.saveConfig(getPlugin(), arenasFile, "arenas");
  }

  public String isOptionDone(String path) {
    Object option = getConfig().get("instances." + getArenaKey() + "." + path);

    if(option != null) {
      return "&a&l✔ 已完成 &7(值: &8" + option + "&7)";
    }

    return "&c&l✘ 未完成 | 原因: 无数据";
  }

  public String isSectionOptionDone(String path, int minimum) {
    ConfigurationSection section = getConfig().getConfigurationSection("instances." + getArenaKey() + "." + path);
    if(minimum == 0 && section == null) {
      return "&e&l✔ 可选";
    }
    if(section != null) {
      int keysSize = section.getKeys(false).size();

      if(keysSize < minimum) {
        return "&c&l✘ 未完成 | &c请添加更多位置";
      }

      return "&a&l✔ 已完成 &7(值: &8" + keysSize + "&7)";
    }

    return "&c&l✘ 未完成 | 原因: 无数据";
  }

  public String isLocationSectionOptionDone(String path, int minimum) {
    List<String> option = getConfig().getStringList("instances." + getArenaKey() + "." + path);
    if(minimum == 0 && option.isEmpty()) {
      return "&e&l✔ 可选";
    }
    if(!option.isEmpty()) {
      Location location = LocationSerializer.getLocation(option.get(0));
      if(location != null) {
        int keysSize = option.size();
        if(keysSize < minimum) {
          return "&c&l✘ 未完成 | &c请添加更多位置";
        }
        return "&a&l✔ 已完成 &7(值: &8" + keysSize + "&7)";
      }
    }
    return "&c&l✘ 未完成 | 原因: 无数据";
  }

  public String isLocationOptionDone(String path) {
    String option = getConfig().getString("instances." + getArenaKey() + "." + path);
    if(option != null) {
      Location location = LocationSerializer.getLocation(option);
      if(location != null) {
        return "&a&l✔ 已完成 &7(值: &8" + option + "&7)";
      }
    }
    return "&c&l✘ 未完成 | 原因: 无数据";
  }

  public int getMinimumValue(String path) {
    int amount = getConfig().getInt("instances." + getArenaKey() + "." + path, 1);
    return amount == 0 ? 1 : amount;
  }

  public void sendProTip(HumanEntity entity) {
    switch(plugin.getRandom().nextInt(35)) {
      case 0:
        new MessageBuilder("&e&l小贴士: &7我们还有预制的设置，请在 &8https://wiki.plugily.xyz/" + plugin.getPluginNamePrefixLong().toLowerCase() + "/setup/maps &7查看", false).send(entity);
        break;
      case 1:
        new MessageBuilder("&e&l小贴士: &7帮助我们翻译插件到你的语言：https://translate.plugily.xyz", false).send(entity);
        break;
      case 2:
        new MessageBuilder("&e&l小贴士: &7PlaceholderApi 插件已支持我们的插件！查看：https://wiki.plugily.xyz/" + plugin.getPluginNamePrefixLong().toLowerCase() + "/placeholders/placeholderapi", false).send(entity);
        break;
      case 3:
        new MessageBuilder("&e&l小贴士: &7成就、自定义工具包和回放功能是我们为这个小游戏提供的付费附加插件中的内容：https://patreon.com/plugily", false).send(entity);
        break;
      case 4:
        new MessageBuilder("&e&l小贴士: &7我们是开源的！你可以随时通过贡献来帮助我们！查看 https://github.com/Plugily-Projects/", false).send(entity);
        break;
      case 5:
        new MessageBuilder("&e&l小贴士: &7需要帮助？查看维基 &8https://wiki.plugily.xyz/" + plugin.getPluginNamePrefixLong().toLowerCase() + " &7或 Discord https://discord.plugily.xyz", false).send(entity);
        break;
      case 6:
        new MessageBuilder("&e&l小贴士: &7如果你喜欢我们的插件：你可以在 patreon 上支持我们 https://patreon.com/plugily", false).send(entity);
        break;
      case 7:
        new MessageBuilder("&e&l小贴士: &7为插件提出新想法或对现有想法投票！https://app.feedbacky.net/b/" + plugin.getPluginNamePrefixLong().toLowerCase(), false).send(entity);
        break;
      default:
        break;
    }
  }

  public void createInstanceInConfig(String id, Player player) {
    if(ConfigUtils.getConfig(plugin, "arenas").contains("instances." + id)) {
      player.sendRawMessage(ChatColor.DARK_RED + "实例/竞技场已存在！请使用其他 ID 或先删除它！");
      return;
    }
    String path = "instances." + id + ".";
    FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
    config.set(path + "isdone", false);
    ConfigUtils.saveConfig(plugin, config, "arenas");

    plugin.getArenaRegistry().registerArena(id);

    player.sendRawMessage(ChatColor.BOLD + "------------------------------------------");
    player.sendRawMessage(new MessageBuilder("      &e实例 &6" + id + " &e已创建！").build());
    player.sendRawMessage("");
    new TextComponentBuilder("&a通过 &7/" + plugin.getCommandAdminPrefix() + " setup edit " + id + " &a编辑此竞技场").player(player)
        .setHoverEvent(TextComponentBuilder.HoverAction.SHOW_TEXT, "/" + plugin.getCommandAdminPrefix() + " setup edit " + id)
        .setClickEvent(TextComponentBuilder.ClickAction.RUN_COMMAND, "/" + plugin.getCommandAdminPrefix() + " setup edit " + id)
        .sendPlayer();
    player.sendRawMessage("");
    new TextComponentBuilder("&a通过 &7/" + plugin.getCommandAdminPrefix() + " setup &a进入设置界面").player(player)
        .setHoverEvent(TextComponentBuilder.HoverAction.SHOW_TEXT, "/" + plugin.getCommandAdminPrefix() + " setup")
        .setClickEvent(TextComponentBuilder.ClickAction.RUN_COMMAND, "/" + plugin.getCommandAdminPrefix() + " setup")
        .sendPlayer();
    player.sendRawMessage("");
    player.sendRawMessage(ChatColor.GOLD + "不知道从哪里开始？请查看教程视频");
    player.sendRawMessage(ChatColor.GRAY + TUTORIAL_SITE + getPlugin().getPluginNamePrefixLong());
    player.sendRawMessage(ChatColor.BOLD + "-------------------------------------------");
  }

  public final String TUTORIAL_SITE = "https://tutorial.plugily.xyz/";

  public String getTutorialSite() {
    return TUTORIAL_SITE;
  }

  public void closeInventory(HumanEntity humanEntity) {
    Bukkit.getScheduler().runTask(plugin, () -> humanEntity.closeInventory());
  }
}
