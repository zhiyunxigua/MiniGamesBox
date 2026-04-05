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

package plugily.projects.minigamesbox.classic.utils.helper;

import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;

public class SoundHelper {
  public static void playArenaCountdown(PluginArena arena) {
    if(arena.getTimer() > 5) {
      return;
    }
    for(Player player : arena.getPlayers()) {
      switch(arena.getTimer()) {
        case 5:
          XSound.BLOCK_NOTE_BLOCK_PLING.play(player, 1.0f, 0.5f);
          break;
        case 4:
          XSound.BLOCK_NOTE_BLOCK_PLING.play(player, 1.0f, 0.6f);
          break;
        case 3:
          XSound.BLOCK_NOTE_BLOCK_PLING.play(player, 1.0f, 0.7f);
          VersionUtils.sendTitles(player, "§c" + arena.getTimer(), "", 5, 20, 5);
          break;
        case 2:
          XSound.BLOCK_NOTE_BLOCK_PLING.play(player, 1.0f, 0.8f);
          VersionUtils.sendTitles(player, "§6" + arena.getTimer(), "", 5, 20, 5);
          break;
        case 1:
          XSound.BLOCK_NOTE_BLOCK_PLING.play(player, 1.0f, 0.9f);
          VersionUtils.sendTitles(player, "§a" + arena.getTimer(), "", 5, 20, 5);
          break;
        case 0:
          XSound.ENTITY_PLAYER_LEVELUP.play(player, 1.0f, 1.0f);
          break;
      }
    }
  }
}
