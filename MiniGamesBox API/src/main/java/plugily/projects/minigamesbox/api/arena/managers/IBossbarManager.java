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

package plugily.projects.minigamesbox.api.arena.managers;

import org.bukkit.entity.Player;
import plugily.projects.minigamesbox.api.arena.IPluginArena;

/**
 * @author Lagggpixel
 * @since April 24, 2024
 */
public interface IBossbarManager {
  void bossBarUpdate();

  /**
   * Executes boss bar action for arena
   *
   * @param action add or remove a player from boss bar
   * @param player player
   */
  void doBarAction(IPluginArena.IBarAction action, Player player);
}
