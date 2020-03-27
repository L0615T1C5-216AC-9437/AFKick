package AFK;

//-----imports-----//
import arc.Events;
import arc.util.Log;
import mindustry.core.NetServer;
import mindustry.entities.type.Player;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.plugin.Plugin;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static mindustry.Vars.*;


public class Main extends Plugin {
    private boolean aks = false;
    private int hour = 0;
    public HashMap<String, pp> PlayerPos = new HashMap<>();
    public HashMap<String, Integer> PAFKN = new HashMap<>();
    public HashMap<String, Integer> bb = new HashMap<>();

    public Main() throws InterruptedException {
        Thread AS = new Thread() {
            public void run() {
                Log.info("AFK started Successfully!");
                while (aks) {
                    for (Player p : playerGroup.all()) {
                        if (PlayerPos.containsKey(p.uuid)) {
                            if (PlayerPos.get(p.uuid).getX() + 2.5 > p.x / 8 && p.x / 8 > PlayerPos.get(p.uuid).getX() - 2.5 && PlayerPos.get(p.uuid).getY() + 2.5 > p.y / 8 && p.y / 8 > PlayerPos.get(p.uuid).getY() - 2.5 && !bb.containsKey(p.uuid)) {//if hasn't moved +- 2.5 x y or bb = 0
                                if (PAFKN.containsKey(p.uuid)) {
                                    PlayerPos.remove(p.uuid);
                                    p.getInfo().timesKicked--;
                                    if (PAFKN.get(p.uuid) > 4) {
                                        PAFKN.remove(p.uuid);
                                        PlayerPos.remove(p.uuid);
                                        Call.onInfoToast(p.name + " [white]was [scarlet]Banned [white]for constant inactivity.", 5);
                                        p.con.kick("1h temp ban for constantly being kicked.", 60 * 60);
                                    } else {
                                        int i = PAFKN.get(p.uuid) + 1;
                                        PAFKN.replace(p.uuid, i);
                                        PlayerPos.remove(p.uuid);
                                        Call.onInfoToast(p.name + " [white]was kicked for inactivity.", 5);
                                        p.con.kick("Connection Closed for being AFK", 60);
                                    }
                                } else {
                                    PAFKN.put(p.uuid, 0);
                                    PlayerPos.remove(p.uuid);
                                    p.getInfo().timesKicked--;
                                    Call.onInfoToast(p.name + " [white]was kicked for inactivity.", 5);
                                    p.con.kick("Connection Closed for being AFK", 60);
                                }
                            } else {
                                PlayerPos.get(p.uuid).setX(p.x / 8);
                                PlayerPos.get(p.uuid).setY(p.y / 8);
                            }
                        } else {
                            Log.err("Player not in database.");
                            p.getInfo().timesKicked--;
                            p.con.kick("Error", 1);
                        }
                    }
                    bb.clear();
                    try {
                        TimeUnit.SECONDS.sleep(5 * 60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        aks = true;
        AS.start();
        Log.info("Attempting to start AFK...");

        Thread hourTimer = new Thread() {
            public void run(){
                try {
                    Thread.sleep(60 * 60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hour++;
            }
        };
        Events.on(EventType.WaveEvent.class, event -> {
            if (hour == 3) {
                PAFKN.clear();
                hour = 0;
            } else {
                hour++;
            }
        });


        Events.on(EventType.PlayerJoin.class, event -> {
            Player player = event.player;
            if (!PlayerPos.containsKey(player.uuid)) {
                PlayerPos.put(player.uuid, new pp());
                PlayerPos.get(player.uuid).setX(0);
                PlayerPos.get(player.uuid).setY(0);
            } else {
                Log.err("Joined Player somehow already in PlayerPos Database!");
            }
        });
        Events.on(EventType.PlayerLeave.class, event -> {
            Player player = event.player;
            if (PlayerPos.containsKey(player.uuid)) {
                PlayerPos.remove(player.uuid);
            } else {
                Log.err("Player Leaving not in PlayerPos database!");
            }
        });
        Events.on(EventType.PlayerBanEvent.class, event -> {
            Player player = event.player;
            if (PlayerPos.containsKey(player.uuid)) {
                PlayerPos.remove(player.uuid);
            } else {
                Log.err("Player Leaving not in PlayerPos database!");
            }
        });
        Events.on(EventType.BlockBuildEndEvent.class, event -> {
            if (event.player == null) return;
            Player player = event.player;
            if (bb.containsKey(player.uuid)) {
                bb.replace(player.uuid,bb.get(player.uuid) + 1);
            } else {
                bb.put(player.uuid, 1);
            }
        });
    }
}
