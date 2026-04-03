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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugily.projects.minigamesbox.api.IPluginMain;
import plugily.projects.minigamesbox.api.kit.ability.IKitAbility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Lagggpixel
 * @since April 24, 2024
 */
public interface IKit {
  boolean isUnlockedByPlayer(Player p);

  boolean isUnlockedOnDefault();

  HashMap<ItemStack, Integer> getKitItems();

  /**
   * @return main plugin
   */
  IPluginMain getPlugin();

  /**
   * Retrieves the name of the object.
   *
   * @return the name of the object
   */
  String getName();

  String getKey();

  ItemStack getItemStack();

  ArrayList<String> getDescription();

  void giveKitItems(Player player);

  /**
   * @return Returns the configuration section for the kit
   */
  ConfigurationSection getKitConfigSection();

  Object getOptionalConfiguration(String path, Object defaultValue);

  Object getOptionalConfiguration(String path);

  void addOptionalConfiguration(String path, Object object);

  public List<IKitAbility> getAbilities();

  boolean hasAbility(IKitAbility kitAbility);
}
