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

package plugily.projects.minigamesbox.classic.handlers.setup.inventories;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import plugily.projects.minigamesbox.classic.PluginMain;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventory;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventoryUtils;
import plugily.projects.minigamesbox.classic.utils.conversation.SimpleConversationBuilder;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.inventory.common.item.ClickableItem;
import plugily.projects.minigamesbox.inventory.normal.NormalFastInv;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 21.06.2022
 */
public class HomeInventory extends NormalFastInv implements InventoryHandler {

  private final SetupInventory setupInventory;
  private final PluginMain plugin;
  private final FileConfiguration config;

  public HomeInventory(int size, String title, SetupInventory setupInventory) {
    super(size, title);
    this.setupInventory = setupInventory;
    this.plugin = setupInventory.getPlugin();
    this.config = setupInventory.getConfig();
    prepare();
  }

  @Override
  public void prepare() {
    injectItems();
    setForceRefresh(true);
    refresh();
  }

  @Override
  public void injectItems() {
    setItem(19, ClickableItem.of(new ItemBuilder(XMaterial.REDSTONE_BLOCK.parseItem())
        .name(new MessageBuilder("&c竞技场列表").build())
        .lore(ChatColor.GRAY + "编辑、删除或者复制竞技场")
        .build(), event -> setupInventory.open(SetupInventoryUtils.SetupInventoryStage.ARENA_LIST)
    ));

    setItem(22, ClickableItem.of(new ItemBuilder(XMaterial.OAK_SIGN.parseItem())
            .name(new MessageBuilder("&c创建竞技场").build())
            .lore(ChatColor.GRAY + "创建一个全新的竞技场")
            .build(), event -> {
          setupInventory.closeInventory(event.getWhoClicked());
          new SimpleConversationBuilder(setupInventory.getPlugin()).withPrompt(new StringPrompt() {
            @Override
            public @NotNull String getPromptText(ConversationContext context) {
              return new MessageBuilder("&e请在聊天中输入竞技场名称以创建新竞技场！你可以使用颜色代码。&c输入 'CANCEL' 来取消！").prefix().build();
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
              String name = new MessageBuilder(input, false).build();
              if(name.contains(" ")) {
                context.getForWhom().sendRawMessage(new MessageBuilder("&c竞技场键不能有空格。你之后可以给它起一个漂亮的地图名称 ;)").prefix().build());
                return Prompt.END_OF_CONVERSATION;
              }
              setupInventory.createInstanceInConfig(name, (Player) context.getForWhom());
              if(setupInventory.getPlugin().getArenaRegistry().getArena(name) == null) {
                return Prompt.END_OF_CONVERSATION;
              }
              setupInventory.setArenaKey(name);
              setupInventory.open(SetupInventoryUtils.SetupInventoryStage.ARENA_EDITOR);
              return Prompt.END_OF_CONVERSATION;
            }
          }).buildFor((Player) event.getWhoClicked());
        }
    ));


    setItem(25, ClickableItem.of(new ItemBuilder(XMaterial.SLIME_BLOCK.parseItem())
            .name(new MessageBuilder("&c继续竞技场设置").build())
            .lore(ChatColor.GRAY + "继续之前开始的竞技场编辑器")
            .lore(ChatColor.RED + "竞技场: " + setupInventory.getArenaKey())
            .build(), event -> {
          if(setupInventory.getArenaKey() == null) {
            new MessageBuilder("你需要先创建或编辑一个竞技场").prefix().player((Player) event.getWhoClicked()).sendPlayer();
            return;
          }
          setupInventory.open(SetupInventoryUtils.SetupInventoryStage.ARENA_EDITOR);
        }
    ));


    setItem(39, ClickableItem.of(new ItemBuilder(XMaterial.GOLD_INGOT.parseItem())
        .name(new MessageBuilder("&6&l► Patreon 插件 ◄ &8(AD)").build())
        .lore(new MessageBuilder("&7通过付费插件增强游戏体验！").build())
        .lore(new MessageBuilder("&6插件功能选择：").build())
        .lore(new MessageBuilder("&6自定义工具包、成就、回放功能").build())
        .lore(new MessageBuilder("&7点击获取 Patreon 计划链接！").build())
        .build(), event -> {
      setupInventory.closeInventory(event.getWhoClicked());
      new MessageBuilder("&6查看会员计划于").prefix().send(event.getWhoClicked());
      new MessageBuilder("&6 https://wiki.plugily.xyz/" + setupInventory.getPlugin().getPluginNamePrefixLong().toLowerCase() + "/addon/overview", false).send(event.getWhoClicked());
    }));
    setItem(41, ClickableItem.of(new ItemBuilder(XMaterial.MAP.parseItem())
        .name(new MessageBuilder("&e&l► 查看设置视频 ◄").build())
        .lore(ChatColor.GRAY + "设置遇到问题吗")
        .lore(ChatColor.GRAY + "或者想了解一些")
        .lore(ChatColor.YELLOW + "有用的提示？")
        .lore(ChatColor.YELLOW + "点击获取视频链接！")
        .build(), event -> {
      setupInventory.closeInventory(event.getWhoClicked());
      new MessageBuilder("&6查看教程视频在").prefix().send(event.getWhoClicked());
      new MessageBuilder("&e " + SetupInventoryUtils.SetupInventoryStage.HOME.getTutorialURL(), false).send(event.getWhoClicked());
    }));
    setDefaultItem(ClickableItem.of(new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()).name(" ").build()));
  }
}