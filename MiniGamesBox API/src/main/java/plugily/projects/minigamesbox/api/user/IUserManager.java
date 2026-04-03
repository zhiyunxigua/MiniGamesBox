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
import plugily.projects.minigamesbox.api.stats.IStatisticType;
import plugily.projects.minigamesbox.api.user.data.UserDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author Lagggpixel
 * @since April 24, 2024
 */
public interface IUserManager {
  IUser getUser(Player player);

  List<IUser> getUsers(IPluginArena arena);

  void saveStatistic(IUser user, IStatisticType stat);

  void addExperience(Player player, int i);

  void addStat(Player player, IStatisticType stat);

  void addStat(IUser user, IStatisticType stat);

  void updateLevelStat(IUser user, IPluginArena arena);

  void storeUserQuitDuringGame(Player player, IPluginArena arena);

  HashMap<UUID, IPluginArena> getUsersQuitDuringGame();

  void saveAllStatistic(IUser user);

  void loadStatistics(IUser user);

  void removeUser(IUser user);

  UserDatabase getDatabase();
}
