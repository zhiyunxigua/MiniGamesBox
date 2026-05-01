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

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventory;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventoryUtils;
import plugily.projects.minigamesbox.classic.utils.conversation.SimpleConversationBuilder;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.inventory.common.RefreshableFastInv;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 26.06.2022
 */
public class SwitchItem implements CategoryItemHandler {
  private final SetupInventory setupInventory;
  private final ItemStack item;
  private final String name;
  private final String description;
  private final String keyName;

  private final List<String> switches;
  private final Consumer<InventoryClickEvent> clickConsumer;

  public SwitchItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName, List<String> switches, Consumer<InventoryClickEvent> clickConsumer) {
    this.setupInventory = setupInventory;
    this.switches = switches;
    setLore(item);
    item
        .name("&7切换 &a" + name.toUpperCase() + " &7值")
        .colorizeItem();
    this.item = item.build();
    this.name = name;
    this.description = description;
    this.keyName = keyName;
    this.clickConsumer = clickConsumer;
  }

  public SwitchItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName, List<String> switches) {
    this(setupInventory, item, name, description, keyName, switches, emptyConsumer -> {
    });
  }

  private void setLore(ItemBuilder itemBuilder) {
    itemBuilder.lore("&a信息")
        .lore("&7" + description)
        .lore("&a状态")
        .lore("&7" + getSetupInfo())
        .lore("&a控制")
        .lore("&e左键点击 \n&7-> 通过在聊天中输入来设置值")
        .lore("&e右键点击 \n&7-> 在值之间切换");
  }

  @Override
  public ItemStack getItem() {
    ItemBuilder itemBuilder = new ItemBuilder(item).removeLore();
    setLore(itemBuilder);
    return itemBuilder.colorizeItem().build();
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    switch(event.getClick()) {
      case LEFT:
        new SimpleConversationBuilder(setupInventory.getPlugin()).withPrompt(new StringPrompt() {
          @Override
          public @NotNull String getPromptText(ConversationContext context) {
            return new MessageBuilder("&e请在聊天中输入以下单词之一：" + switches.toString().toLowerCase() + "！").prefix().build();
          }

          @Override
          public Prompt acceptInput(ConversationContext context, String input) {
            if(switches.stream().noneMatch(input::equalsIgnoreCase)) {
              context.getForWhom().sendRawMessage(new MessageBuilder("&e✖ 只允许使用列表中的值，请再次点击物品重试").build());
              return Prompt.END_OF_CONVERSATION;
            }
            context.getForWhom().sendRawMessage(new MessageBuilder("&e✔ 完成 | &a已将 " + name.toUpperCase() + " " + setupInventory.getArenaKey() + " 设置为 " + input).prefix().build());
            setupInventory.setConfig(keyName, input);
            return Prompt.END_OF_CONVERSATION;
          }
        }).buildFor((Player) event.getWhoClicked());
        break;
      case RIGHT:
        String option = setupInventory.getConfig().getString("instances." + setupInventory.getArenaKey() + "." + keyName, switches.get(0));
        int position = switches.indexOf(option);
        String newOption = switches.get(switches.size() - 1 <= position ? 0 : position + 1);
        event.getWhoClicked().sendMessage(new MessageBuilder("&e✔ 完成 | &a已将 " + name.toUpperCase() + " " + setupInventory.getArenaKey() + " 设置为 " + newOption).prefix().build());
        setupInventory.setConfig(keyName, newOption);
        InventoryHolder holder = event.getInventory().getHolder();
        if(holder instanceof RefreshableFastInv) {
          ((RefreshableFastInv) holder).refresh();
        }
        break;
      default:
        break;
    }
    clickConsumer.accept(event);
    if(event.getClick() == ClickType.LEFT) {
      setupInventory.closeInventory(event.getWhoClicked());
    } else {
      setupInventory.open(SetupInventoryUtils.SetupInventoryStage.ARENA_EDITOR);
    }
  }

  @Override
  public String getSetupInfo() {
    return setupInventory.isOptionDone(keyName);
  }

  @Override
  public boolean getSetupStatus() {
    return getSetupInfo().contains("✔");
  }
}
