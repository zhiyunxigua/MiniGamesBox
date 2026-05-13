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

package plugily.projects.minigamesbox.classic.arena.states;

import org.bukkit.plugin.java.JavaPlugin;
import plugily.projects.minigamesbox.api.arena.IArenaState;
import plugily.projects.minigamesbox.api.arena.IPluginArena;
import plugily.projects.minigamesbox.classic.PluginMain;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lagggpixel
 * @since April 24, 2024
 */
public class ArenaState {

  private static final Map<String, String> cache = new HashMap<>();

  public static String getFormattedName(IArenaState arenaState) {
    return arenaState.getFormattedName();
  }

  public static String getPlaceholder(IArenaState arenaState) {
    if (!cache.containsKey(arenaState.getFormattedName())) {
      PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);
      cache.put(arenaState.getFormattedName(), new MessageBuilder(plugin.getLanguageManager().getLanguageMessage("Placeholders.Game-States." + getFormattedName(arenaState))).build());
    }

    return cache.get(arenaState.getFormattedName());
  }

  public static boolean isLobbyStage(IPluginArena arena) {
    return arena.getArenaState() == IArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == IArenaState.STARTING || arena.getArenaState() == IArenaState.FULL_GAME;
  }

  public static boolean isStartingStage(IPluginArena arena) {
    return arena.getArenaState() == IArenaState.STARTING || arena.getArenaState() == IArenaState.FULL_GAME;
  }
}
