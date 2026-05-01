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
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventory;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventoryUtils;
import plugily.projects.minigamesbox.classic.utils.conversation.SimpleConversationBuilder;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.inventory.common.RefreshableFastInv;

import java.util.function.Consumer;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 29.12.2021
 */
public class CountItem implements CategoryItemHandler {
  private final SetupInventory setupInventory;
  private final ItemStack item;
  private int count;
  private final String name;
  private final String description;
  private final String keyName;
  private final Consumer<InventoryClickEvent> clickConsumer;

  /**
   * Constructor
   *
   * @param setupInventory
   * @param item           the display item
   * @param count          the count the item should have
   * @param name
   * @param description
   * @param keyName
   * @param clickConsumer  the consumer to be called when the item is clicked
   */
  public CountItem(SetupInventory setupInventory, ItemBuilder item, int count, String name, String description, String keyName, Consumer<InventoryClickEvent> clickConsumer) {
    this.setupInventory = setupInventory;
    this.name = name;
    this.description = description;
    this.keyName = keyName;
    setLore(item);
    item
        .name("&7设置 &a" + this.name.toUpperCase() + " &7数量")
        .colorizeItem();
    this.item = item.build();
    this.clickConsumer = clickConsumer;
    this.count = count >= 0 ? count : 1;
  }

  private void setLore(ItemBuilder itemBuilder) {
    itemBuilder.lore("&a信息")
        .lore("&7" + this.description)
        .lore("&a状态")
        .lore("&7" + getSetupInfo())
        .lore("&a控制")
        .lore("&e左键")
        .lore("&7-> 增加数量")
        .lore("&eSHIFT+左键")
        .lore("&7-> 在聊天中输入数字")
        .lore("&e右键")
        .lore("&7-> 减少数量");
  }

  /**
   * Constructor
   *
   * @param setupInventory
   * @param item           the display item
   * @param clickConsumer  the consumer to be called when the item is clicked
   * @param name
   * @param description
   * @param keyName
   */
  public CountItem(SetupInventory setupInventory, ItemBuilder item, Consumer<InventoryClickEvent> clickConsumer, String name, String description, String keyName) {
    this(setupInventory, item, item.build().getAmount(), name, description, keyName, clickConsumer);
  }

  public CountItem(SetupInventory setupInventory, ItemBuilder item, String name, String description, String keyName) {
    this(setupInventory, item, setupInventory.getMinimumValue(keyName), name, description, keyName, emptyConsumer -> {
    });
  }


  @Override
  public ItemStack getItem() {
    item.setAmount(Math.min(count, 64));
    ItemBuilder itemBuilder = new ItemBuilder(item).removeLore();
    setLore(itemBuilder);
    return itemBuilder.colorizeItem().build();
  }


  @Override
  public void onClick(InventoryClickEvent event) {
    switch(event.getClick()) {
      case SHIFT_LEFT:
        new SimpleConversationBuilder(setupInventory.getPlugin()).withPrompt(new NumericPrompt() {
          @Override
          protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull Number number) {
            int countInput = number.intValue();
            if(countInput < 1) {
              conversationContext.getForWhom().sendRawMessage("§c§l✖ §c警告 | 请不要将数量设置为低于 1！对于更高的数值，请通过聊天轻松设置数字！");
              countInput = 1;
            }
            updateArenaFile(countInput);
            conversationContext.getForWhom().sendRawMessage(new MessageBuilder("&e✔ 已完成 | &a计数用于" + name.toUpperCase() + " 在 " + setupInventory.getArenaKey() + " 设置到 " + countInput).prefix().build());
            //considerable to open setup inventory again?
            return Prompt.END_OF_CONVERSATION;
          }

          @Override
          public @NotNull String getPromptText(@NotNull ConversationContext conversationContext) {
            return new MessageBuilder("&e请在聊天中输入数量" + name.toUpperCase() + "! 只允许整数!").prefix().build();
          }
        }).buildFor((Player) event.getWhoClicked());
        break;
      case LEFT:
        count++;
        break;
      case RIGHT:
        count--;
        break;
      default:
        break;
    }
    if(count < 1) {
      event.getWhoClicked().sendMessage("§c§l✖ §c警告 | 请不要将数量设置低于1！对于更高的数值，请通过聊天轻松设置数字!");
      count = 1;
    }
    clickConsumer.accept(event);
    updateCount();
    InventoryHolder holder = event.getInventory().getHolder();
    if(holder instanceof RefreshableFastInv) {
      ((RefreshableFastInv) holder).refresh();
    }
    if(event.getClick() == ClickType.SHIFT_LEFT) {
      setupInventory.closeInventory(event.getWhoClicked());
    } else {
      setupInventory.open(SetupInventoryUtils.SetupInventoryStage.ARENA_EDITOR);
    }
  }

  private void updateCount() {
    updateArenaFile(count);
  }

  private void updateArenaFile(int amount) {
    setupInventory.setConfig(keyName, amount);
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