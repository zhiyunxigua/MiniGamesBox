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

package plugily.projects.minigamesbox.api.utils.services.locale;

import java.util.List;

/**
 * @author Lagggpixel
 * @since April 24, 2024
 */
public interface ILocale {

  /**
   * Gets name of locale, ex. English or German
   *
   * @return name of locale
   */
  String getName();

  /**
   * Gets original name of locale ex. for German it will return Deutsch, Polish returns Polski etc.
   *
   * @return name of locale in its language
   */
  String getOriginalName();

  /**
   * @return authors of locale
   */
  String getAuthor();

  /**
   * Language code ex. en_GB, de_DE, pl_PL etc.
   *
   * @return language code of locale
   */
  String getPrefix();

  /**
   * Valid aliases of locale ex. for German - deutsch, de, german; Polish - polski, pl, polish etc.
   *
   * @return aliases for locale
   */
  List<String> getAliases();
}
