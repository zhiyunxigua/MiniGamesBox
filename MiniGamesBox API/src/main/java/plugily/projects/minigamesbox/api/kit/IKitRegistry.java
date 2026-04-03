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

package plugily.projects.minigamesbox.api.kit;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Lagggpixel
 * @since April 24, 2024
 */
public interface IKitRegistry {
  HandleItem getHandleItem();

  void setHandleItem(HandleItem handleItem);

  /**
   * Method for registering clone and empty kit
   *
   * @param kit Kit to register
   */
  void registerKit(IKit kit);

  /**
   * Registers the kits by loading their configurations.
   */
  void registerKits(List<String> optionalConfigurations);

  /**
   * Creates a new kit file with the kit content of the players inventory
   *
   * @param name name of kit file and kit
   * @param player player from which the kit gets created
   */
  void savePlayerAsNewKit(String name, Player player);
  /**
   * Return default game kit
   *
   * @return default game kit
   */
  IKit getDefaultKit();

  /**
   * Sets default game kit
   *
   * @param defaultKit default kit to set, must be FreeKit
   */
  void setDefaultKit(IKit defaultKit);

  /**
   * Sets the default kit for the plugin using the config option
   *
   * @param defaultKitName name of the default kit
   */
  void setDefaultKit(String defaultKitName);

  /**
   * Returns all available kits
   *
   * @return list of all registered kits
   */
  List<IKit> getKits();

  /**
   * Retrieves a Kit object based on the provided key.
   *
   * @param key the key used to search for the Kit
   * @return the Kit object with the matching key, or null if not found
   */
  IKit getKitByKey(String key);

  /**
   * Retrieves a Kit object from the 'kits' list by its name.
   *
   * @param key the name of the Kit object to retrieve
   * @return the Kit object with the specified name, or null if not found
   */
  IKit getKitByName(String key);
}
