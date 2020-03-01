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
    public static HashMap<String, pp> PlayerPos = new HashMap<>();

    public Main() throws InterruptedException {
        Thread AS = new Thread() {
            public void run() {
                Log.info("AFK started Successfully!");
                while (aks) {
                    for (Player p : playerGroup.all()) {
                        if (p.x / 8 == PlayerPos.get(p.uuid).getX() && p.y / 8 == PlayerPos.get(p.uuid).getY()) {
                            p.getInfo().timesKicked--;
                            Call.sendMessage(p.name + " [white]was kicked for innactivity.");
                            p.con.kick("Connection Closed for being AFK",1);
                        } else {
                            PlayerPos.get(p.uuid).setX(p.x / 8);
                            PlayerPos.get(p.uuid).setY(p.y / 8);
                        }
                    }
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

        Events.on(EventType.PlayerJoin.class, event -> {
            Player player = event.player;
            PlayerPos.put(player.uuid, new pp());
            PlayerPos.get(player.uuid).setX(0);
            PlayerPos.get(player.uuid).setY(0);
        });
        Events.on(EventType.PlayerLeave.class, event -> {
            Player player = event.player;
            PlayerPos.remove(player.uuid);
        });
    }
}
