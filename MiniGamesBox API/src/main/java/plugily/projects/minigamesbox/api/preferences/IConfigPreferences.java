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

package plugily.projects.minigamesbox.api.preferences;

import java.util.List;
import java.util.Map;

/**
 * @author Lagggpixel
 * @since April 24, 2024
 */
public interface IConfigPreferences {

  /**
   * Returns whether option value is true or false
   *
   * @param name option to get value from
   * @return true or false based on user configuration
   */
  boolean getOption(String name);

  /**
   * Register a new config option
   *
   * @param name   The name of the Option
   * @param option Contains the path and the default value
   */
  void registerOption(String name, IConfigOption option);

  /**
   * Remove config options that are not protected
   *
   * @param name The name of the Option
   */
  void unregisterOption(String name);

  Map<String, IConfigOption> getOptions();

  List<ICommandShorter> getCommandShorts();

  void addCommandShorter(ICommandShorter commandShorter);
}
