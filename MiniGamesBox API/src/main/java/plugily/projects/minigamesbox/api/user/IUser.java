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

package plugily.projects.minigamesbox.api.user;

import org.bukkit.entity.Player;
import plugily.projects.minigamesbox.api.arena.IPluginArena;
import plugily.projects.minigamesbox.api.kit.IKit;
import plugily.projects.minigamesbox.api.stats.IStatisticType;

import java.util.UUID;

/**
 * @author Lagggpixel
 * @since April 24, 2024
 */
public interface IUser {
  UUID getUniqueId();

  IKit getKit();

  void setKit(IKit kit);

  IPluginArena getArena();

  Player getPlayer();

  boolean isSpectator();

  void setSpectator(boolean spectator);

  boolean isPermanentSpectator();

  void setPermanentSpectator(boolean permanentSpectator);

  int getStatistic(String statistic);

  int getStatistic(IStatisticType statisticType);

  void setStatistic(IStatisticType statisticType, int value);

  void setStatistic(String statistic, int value);

  void adjustStatistic(IStatisticType statisticType, int value);

  void adjustStatistic(String statistic, int value);

  void resetNonePersistentStatistics();

  boolean checkCanCastCooldownAndMessage(String cooldown);

  void setCooldown(String key, double seconds);

  double getCooldown(String key);

  boolean isInitialized();

  void setInitialized(boolean initialized);
}
