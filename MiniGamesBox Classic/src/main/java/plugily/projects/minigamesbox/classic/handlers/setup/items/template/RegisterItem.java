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

package plugily.projects.minigamesbox.classic.handlers.setup.items.template;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import plugily.projects.minigamesbox.api.arena.IPluginArena;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventory;
import plugily.projects.minigamesbox.classic.handlers.setup.categories.PluginSetupCategoryManager;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.inventory.common.item.ClickableItem;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 21.06.2022
 */
public class RegisterItem implements ClickableItem {
  private final SetupInventory setupInventory;

  private final PluginSetupCategoryManager pluginSetupCategoryManager;
  private final RegisterStatus registerStatus;

  public RegisterItem(SetupInventory setupInventory, PluginSetupCategoryManager pluginSetupCategoryManager) {
    this.setupInventory = setupInventory;
    this.pluginSetupCategoryManager = pluginSetupCategoryManager;
    if(pluginSetupCategoryManager.canRegister()) {
      IPluginArena arena = setupInventory.getPlugin().getArenaRegistry().getArena(setupInventory.getArenaKey());
      if(arena != null && arena.isReady()) {
        registerStatus = RegisterStatus.ARENA_READY;
      } else {
        registerStatus = RegisterStatus.ARENA_REGISTER;
      }
    } else {
      registerStatus = RegisterStatus.ARENA_SETUP;
    }
  }

  @Override
  public ItemStack getItem() {
    ItemBuilder item;
    switch(registerStatus) {
      case ARENA_READY:
        item = new ItemBuilder(XMaterial.POTATO.parseMaterial())
            .name(new MessageBuilder("&a&l竞技场设置完成 - 恭喜").build())
            .lore(ChatColor.GRAY + "此竞技场已注册！")
            .lore(ChatColor.GRAY + "你现在可以在此竞技场上游玩了！")
            .lore("&a控制")
            .lore("&e点击 \n&7-> 重新加载竞技场");
        break;
      case ARENA_REGISTER:
        item = new ItemBuilder(XMaterial.FIREWORK_ROCKET.parseMaterial())
            .name(new MessageBuilder("&e&l注册竞技场 - 完成设置").build())
            .lore(ChatColor.GRAY + "当你完成配置后点击此项。")
            .lore(ChatColor.GRAY + "它将验证并注册你的竞技场。")
            .lore(ChatColor.GRAY + "做得好，你完成了整个设置过程！")
            .lore("&a控制")
            .lore("&e点击 \n&7-> 注册竞技场")
            .enchantment(XEnchantment.UNBREAKING.get());
        break;
      case ARENA_SETUP:
      default:
        item = new ItemBuilder(XMaterial.BARRIER.parseMaterial())
            .name(new MessageBuilder("&c&l竞技场设置未完成").build())
            .lore(ChatColor.GRAY + "继续进行设置！")
            .lore(ChatColor.GRAY + "请随意观看你在 GUI 上找到的教程！")
            .lore(ChatColor.GRAY + "支持：discord.plugily.xyz - #general-questions");

        break;
    }

    return item.colorizeItem().build();
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    switch(registerStatus) {
      case ARENA_READY:
      case ARENA_REGISTER:
        IPluginArena arena = setupInventory.getPlugin().getArenaRegistry().getArena(setupInventory.getArenaKey());
        if(arena != null) {
          arena.setReady(true);
        }
        setupInventory.closeInventory(event.getWhoClicked());
        break;
      case ARENA_SETUP:
      default:
        break;
    }
  }

  private enum RegisterStatus {
    ARENA_READY, ARENA_REGISTER, ARENA_SETUP
  }

}