/*
 * MiniGamesBox - Library box with massive content that could be seen as minigames core.
 * Copyright (C) 2026 Plugily Projects - maintained by Tigerpanzer_02 and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package plugily.projects.minigamesbox.classic.commands.arguments.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugily.projects.minigamesbox.classic.commands.arguments.PluginArgumentsRegistry;
import plugily.projects.minigamesbox.classic.commands.arguments.data.CommandArgument;
import plugily.projects.minigamesbox.classic.commands.arguments.data.LabelData;
import plugily.projects.minigamesbox.classic.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 12.10.2025
 */
public class KitFileArgument {

  public KitFileArgument(PluginArgumentsRegistry registry) {
    registry.mapArgument(registry.getPlugin().getCommandAdminPrefixLong(), new LabeledCommandArgument("kitfile", registry.getPlugin().getPluginNamePrefixLong() + ".admin.kitfile", CommandArgument.ExecutorType.BOTH,
        new LabelData("/" + registry.getPlugin().getCommandAdminPrefix() + " kitfile", "/" + registry.getPlugin().getCommandAdminPrefix() + " kitfile <name>",
            "&7Creates kit file \n&6Permission: &7" + registry.getPlugin().getPluginNamePrefixLong() + ".admin.kitfile")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if(args.length != 2) {
          new MessageBuilder(ChatColor.DARK_RED + "Please provide a name!").prefix().send(sender);
          return;
        }
        if(!(sender instanceof Player)) {
          new MessageBuilder("COMMANDS_ONLY_BY_PLAYER").asKey().send(sender);
          return;
        }
        String name = args[1];
        registry.getPlugin().getKitRegistry().savePlayerAsNewKit(name, (Player) sender);
        new MessageBuilder("COMMANDS_COMMAND_EXECUTED").asKey().send(sender);
      }
    });
  }


}
