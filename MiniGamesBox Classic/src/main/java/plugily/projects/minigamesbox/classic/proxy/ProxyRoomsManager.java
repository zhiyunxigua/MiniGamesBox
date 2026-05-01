/*
 *  MiniGamesBox - Library box with massive content that could be seen as minigames core.
 *  Copyright (C) 2023 Plugily Projects - maintained by Tigerpanzer_02 and contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package plugily.projects.minigamesbox.classic.proxy;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import plugily.projects.minigamesbox.api.arena.IPluginArena;
import plugily.projects.minigamesbox.api.events.game.PlugilyGameStateChangeEvent;
import plugily.projects.minigamesbox.classic.PluginMain;
import plugily.projects.minigamesbox.classic.utils.configuration.ConfigUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ProxyRoomsManager implements Listener {

  private final PluginMain plugin;
  private final FileConfiguration config;
  private final Gson gson = new Gson();
  private final Map<UUID, ProxyJoinReservation> reservations = new ConcurrentHashMap<>();
  private final Map<String, Long> arenaStateStartedAt = new ConcurrentHashMap<>();
  private final String gameId;
  private final String serverId;
  private final boolean enabled;
  private final boolean redisEnabled;
  private final int publishIntervalTicks;
  private final int reservationDelayTicks;
  private final int snapshotTtlSeconds;
  private final int reservationTtlSeconds;
  private final String redisNamespace;
  private JedisPool jedisPool;
  private JedisPubSub subscriber;
  private Thread subscriberThread;
  private int heartbeatTaskId = -1;

  public ProxyRoomsManager(PluginMain plugin) {
    this.plugin = plugin;
    this.config = ConfigUtils.getConfig(plugin, "proxy_rooms");
    this.enabled = config != null && config.getBoolean("Enabled", false);
    this.redisEnabled = enabled && config.getBoolean("Redis.Enabled", false);
    this.gameId = resolveGameId();
    this.serverId = resolveServerId();
    this.publishIntervalTicks = Math.max(20, config == null ? 40 : config.getInt("Publish-Interval-Ticks", 40));
    this.reservationDelayTicks = Math.max(1, config == null ? 10 : config.getInt("Reservation-Delay-Ticks", 10));
    this.snapshotTtlSeconds = Math.max(5, config == null ? 15 : config.getInt("Snapshot-TTL-Seconds", 15));
    this.reservationTtlSeconds = Math.max(5, config == null ? 15 : config.getInt("Reservation-TTL-Seconds", 15));
    this.redisNamespace = config == null ? "minigamesbox" : config.getString("Redis.Namespace", "minigamesbox");
  }

  public void start() {
    if(!enabled) {
      plugin.getDebugger().debug("[ProxyRooms] Disabled in proxy_rooms.yml");
      return;
    }
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    initializeRedis();
    heartbeatTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
      cleanupExpiredReservations();
      publishAllArenas();
    }, 20L, publishIntervalTicks);
    plugin.getDebugger().debug("[ProxyRooms] Enabled for gameId={0}, serverId={1}, redis={2}", gameId, serverId, redisEnabled);
  }

  public void disable() {
    HandlerList.unregisterAll(this);
    if(heartbeatTaskId != -1) {
      Bukkit.getScheduler().cancelTask(heartbeatTaskId);
      heartbeatTaskId = -1;
    }
    reservations.clear();
    if(subscriber != null) {
      try {
        subscriber.unsubscribe();
      } catch(Exception ignored) {
        // ignored on shutdown
      }
      subscriber = null;
    }
    if(subscriberThread != null) {
      subscriberThread.interrupt();
      subscriberThread = null;
    }
    if(jedisPool != null) {
      jedisPool.close();
      jedisPool = null;
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getGameId() {
    return gameId;
  }

  public String getServerId() {
    return serverId;
  }

  public void publishArenaSnapshot(IPluginArena arena) {
    if(!enabled || arena == null) {
      return;
    }
    publishSnapshots(Collections.singletonList(createSnapshot(arena)));
  }

  public void publishAllArenas() {
    if(!enabled || plugin.getArenaRegistry() == null) {
      return;
    }
    List<ProxyRoomSnapshot> snapshots = new ArrayList<>();
    for(IPluginArena arena : plugin.getArenaRegistry().getArenas()) {
      snapshots.add(createSnapshot(arena));
    }
    publishSnapshots(snapshots);
  }

  private void publishSnapshots(List<ProxyRoomSnapshot> snapshots) {
    if(!redisEnabled || snapshots.isEmpty()) {
      return;
    }
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      try(Jedis jedis = jedisPool.getResource()) {
        for(ProxyRoomSnapshot snapshot : snapshots) {
          jedis.setex(snapshotKey(snapshot.getArenaId()), snapshotTtlSeconds, gson.toJson(snapshot));
          jedis.publish(snapshotChannel(), gson.toJson(snapshot));
        }
      } catch(Exception exception) {
        plugin.getDebugger().debug(Level.WARNING, "[ProxyRooms] Snapshot publish failed: {0}", exception.getMessage());
      }
    });
  }

  private ProxyRoomSnapshot createSnapshot(IPluginArena arena) {
    long stateStartedAt = arenaStateStartedAt.computeIfAbsent(arena.getId(), key -> System.currentTimeMillis());
    return new ProxyRoomSnapshot(
      gameId,
      serverId,
      arena.getId(),
      arena.getMapName(),
      arena.getArenaState().name(),
      stateStartedAt,
      arena.isReady(),
      arena.getPlayers().size(),
      arena.getMinimumPlayers(),
      arena.getMaximumPlayers(),
      System.currentTimeMillis()
    );
  }

  @EventHandler
  public void onGameStateChange(PlugilyGameStateChangeEvent event) {
    arenaStateStartedAt.put(event.getArena().getId(), System.currentTimeMillis());
    publishArenaSnapshot(event.getArena());
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if(!enabled) {
      return;
    }
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      ProxyJoinReservation reservation = consumeReservation(event.getPlayer().getUniqueId());
      if(reservation == null) {
        publishAllArenas();
        return;
      }
      joinReservedArena(event.getPlayer(), reservation);
    }, reservationDelayTicks);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    reservations.remove(event.getPlayer().getUniqueId());
    publishAllArenas();
  }

  private void joinReservedArena(Player player, ProxyJoinReservation reservation) {
    IPluginArena arena = plugin.getArenaRegistry() == null ? null : plugin.getArenaRegistry().getArena(reservation.getArenaId());
    if(arena == null) {
      plugin.getDebugger().debug(Level.WARNING, "[ProxyRooms] Reservation {0} targets missing arena {1}", reservation.getReservationId(), reservation.getArenaId());
      return;
    }
    IPluginArena currentArena = plugin.getArenaRegistry().getArena(player);
    if(currentArena != null && !currentArena.getId().equalsIgnoreCase(arena.getId())) {
      plugin.getArenaManager().leaveAttempt(player, currentArena);
    }
    if(plugin.getArenaRegistry().getArena(player) == null) {
      plugin.getArenaManager().joinAttempt(player, arena);
    }
  }

  private ProxyJoinReservation consumeReservation(UUID playerId) {
    ProxyJoinReservation reservation = reservations.remove(playerId);
    if(reservation == null) {
      return null;
    }
    if(reservation.isExpired()) {
      return null;
    }
    return reservation;
  }

  private void acceptReservation(ProxyControlMessage message) {
    if(message.playerId == null || message.arenaId == null) {
      plugin.getDebugger().debug(Level.WARNING, "[ProxyRooms] Ignoring invalid reservation payload");
      return;
    }
    UUID playerId;
    try {
      playerId = UUID.fromString(message.playerId);
    } catch(IllegalArgumentException exception) {
      plugin.getDebugger().debug(Level.WARNING, "[ProxyRooms] Invalid player UUID in reservation payload: {0}", message.playerId);
      return;
    }
    long expiresAt = message.expiresAt > 0 ? message.expiresAt : System.currentTimeMillis() + (reservationTtlSeconds * 1000L);
    reservations.put(playerId, new ProxyJoinReservation(message.reservationId, playerId, message.arenaId, expiresAt));
    plugin.getDebugger().debug("[ProxyRooms] Stored reservation {0} for {1} -> {2}", message.reservationId, playerId, message.arenaId);
  }

  private void cleanupExpiredReservations() {
    long now = System.currentTimeMillis();
    reservations.values().removeIf(reservation -> reservation.getExpiresAt() <= now);
  }

  private void initializeRedis() {
    if(!redisEnabled) {
      return;
    }
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(8);
    String host = config.getString("Redis.Host", "127.0.0.1");
    int port = config.getInt("Redis.Port", 6379);
    int timeoutMillis = config.getInt("Redis.Timeout-Millis", 2000);
    String password = config.getString("Redis.Password", "");
    int database = config.getInt("Redis.Database", 0);
    boolean useSsl = config.getBoolean("Redis.Use-SSL", false);
    if(password == null || password.trim().isEmpty()) {
      jedisPool = new JedisPool(poolConfig, host, port, timeoutMillis, null, database, null, useSsl);
    } else {
      jedisPool = new JedisPool(poolConfig, host, port, timeoutMillis, password, database, null, useSsl);
    }
    subscriber = new JedisPubSub() {
      @Override
      public void onMessage(String channel, String message) {
        try {
          ProxyControlMessage controlMessage = gson.fromJson(message, ProxyControlMessage.class);
          if(controlMessage == null || controlMessage.type == null) {
            return;
          }
          if(!"RESERVE_JOIN".equalsIgnoreCase(controlMessage.type)) {
            return;
          }
          Bukkit.getScheduler().runTask(plugin, () -> acceptReservation(controlMessage));
        } catch(JsonSyntaxException exception) {
          plugin.getDebugger().debug(Level.WARNING, "[ProxyRooms] Invalid control payload: {0}", exception.getMessage());
        }
      }
    };
    subscriberThread = new Thread(() -> {
      while(!Thread.currentThread().isInterrupted() && jedisPool != null && !jedisPool.isClosed()) {
        try(Jedis jedis = jedisPool.getResource()) {
          jedis.subscribe(subscriber, controlChannel());
        } catch(Exception exception) {
          if(Thread.currentThread().isInterrupted()) {
            return;
          }
          plugin.getDebugger().debug(Level.WARNING, "[ProxyRooms] Redis subscribe failed: {0}", exception.getMessage());
          try {
            Thread.sleep(2000L);
          } catch(InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            return;
          }
        }
      }
    }, "MiniGamesBox-ProxyRooms-" + serverId);
    subscriberThread.setDaemon(true);
    subscriberThread.start();
  }

  private String resolveGameId() {
    return plugin.getName().toLowerCase();
  }

  private String resolveServerId() {
    String ip = plugin.getServer().getIp();
    if(ip == null || ip.trim().isEmpty()) {
      ip = "127.0.0.1";
    }
    return (ip + ":" + plugin.getServer().getPort()).toLowerCase();
  }

  private String snapshotKey(String arenaId) {
    return redisNamespace + ":rooms:" + gameId + ":" + serverId + ":" + arenaId;
  }

  private String snapshotChannel() {
    return redisNamespace + ":rooms:snapshots";
  }

  private String controlChannel() {
    return redisNamespace + ":rooms:control:" + serverId;
  }

  public static class ProxyRoomSnapshot {

    private final String gameId;
    private final String serverId;
    private final String arenaId;
    private final String mapName;
    private final String state;
    private final long stateStartedAt;
    private final boolean ready;
    private final int currentPlayers;
    private final int minimumPlayers;
    private final int maximumPlayers;
    private final long updatedAt;

    public ProxyRoomSnapshot(String gameId, String serverId, String arenaId, String mapName, String state, long stateStartedAt, boolean ready, int currentPlayers, int minimumPlayers, int maximumPlayers, long updatedAt) {
      this.gameId = gameId;
      this.serverId = serverId;
      this.arenaId = arenaId;
      this.mapName = mapName;
      this.state = state;
      this.stateStartedAt = stateStartedAt;
      this.ready = ready;
      this.currentPlayers = currentPlayers;
      this.minimumPlayers = minimumPlayers;
      this.maximumPlayers = maximumPlayers;
      this.updatedAt = updatedAt;
    }

    public String getArenaId() {
      return arenaId;
    }
  }

  public static class ProxyJoinReservation {

    private final String reservationId;
    private final UUID playerId;
    private final String arenaId;
    private final long expiresAt;

    public ProxyJoinReservation(String reservationId, UUID playerId, String arenaId, long expiresAt) {
      this.reservationId = reservationId == null ? "" : reservationId;
      this.playerId = playerId;
      this.arenaId = arenaId;
      this.expiresAt = expiresAt;
    }

    public String getReservationId() {
      return reservationId;
    }

    public UUID getPlayerId() {
      return playerId;
    }

    public String getArenaId() {
      return arenaId;
    }

    public long getExpiresAt() {
      return expiresAt;
    }

    public boolean isExpired() {
      return expiresAt <= System.currentTimeMillis();
    }
  }

  private static class ProxyControlMessage {

    private String type;
    private String reservationId;
    private String playerId;
    private String arenaId;
    private long expiresAt;
  }
}
