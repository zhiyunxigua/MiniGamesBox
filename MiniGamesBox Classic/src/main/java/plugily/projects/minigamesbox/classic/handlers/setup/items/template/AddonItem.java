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
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventory;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.inventory.common.item.ClickableItem;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 21.06.2022
 */
public class AddonItem implements ClickableItem {
  private final SetupInventory setupInventory;

  public AddonItem(SetupInventory setupInventory) {
    this.setupInventory = setupInventory;
  }

  @Override
  public ItemStack getItem() {
    ItemBuilder item = new ItemBuilder(XMaterial.GOLD_INGOT.parseMaterial());
    item
        .name("&6&l► Patreon 附加插件 ◄ &8(广告)")
        .lore(ChatColor.GRAY + "通过付费附加插件增强游戏体验！")
        .lore(ChatColor.GRAY + "支持开发！")
        .lore(ChatColor.GOLD + "附加插件功能选择：")
        .lore(ChatColor.GOLD + "自定义工具包、自定义成就、自定义称号、回放功能等！")
        .lore("&a控制")
        .lore("&e点击 \n&7-> Patreon 计划链接")
        .enchantment(XEnchantment.UNBREAKING.getEnchant())
        .colorizeItem();
    return item.build();
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    setupInventory.closeInventory(event.getWhoClicked());
    new MessageBuilder("&6查看 Patreon 计划于").prefix().send(event.getWhoClicked());
    new MessageBuilder("&6 https://wiki.plugily.xyz/" + setupInventory.getPlugin().getPluginNamePrefixLong().toLowerCase() + "/addon/overview", false).send(event.getWhoClicked());
  }
}
