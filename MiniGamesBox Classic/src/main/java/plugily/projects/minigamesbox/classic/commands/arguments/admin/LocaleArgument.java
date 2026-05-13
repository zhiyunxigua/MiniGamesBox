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
import plugily.projects.minigamesbox.classic.utils.services.locale.Locale;

public class LocaleArgument {

  public LocaleArgument(PluginArgumentsRegistry registry) {
    registry.mapArgument(registry.getPlugin().getCommandAdminPrefixLong(),
        new LabeledCommandArgument("locale", registry.getPlugin().getCommandAdminPrefixLong() + ".admin.locale",
            CommandArgument.ExecutorType.PLAYER,
            new LabelData("/" + registry.getPlugin().getCommandAdminPrefix() + " locale &c[locale name/prefix]",
                "/" + registry.getPlugin().getCommandAdminPrefixLong() + " locale &c[locale name/prefix]",
                "&7Used for changing locale \n&6Permission: &7" + registry.getPlugin().getPluginNamePrefixLong() + ".admin.locale")) {
          @Override
          public void execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;
            if (args.length != 2) {
              new MessageBuilder(ChatColor.DARK_RED + "Please provide a locale name/prefix!").prefix().send(player);
              return;
            }
            String localeName = args[1];
            Locale locale = Locale.getLocale(localeName);
            if (locale == null) {
              new MessageBuilder(ChatColor.DARK_RED + "Locale not found!").prefix().send(player);
              return;
            }
            registry.getPlugin().getConfig().set("locale", locale.getPrefix());
            registry.getPlugin().saveConfig();

            registry.getPlugin().getLanguageManager().setupLocale();
            new MessageBuilder(ChatColor.GREEN + "Locale changed to "+locale.getPrefix()+"! Make sure to make a proper restart of your server.").prefix().send(player);
          }
        });
  }

}