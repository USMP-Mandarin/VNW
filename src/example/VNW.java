package example;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Administration.*;
import mindustry.world.blocks.storage.*;

import java.util.HashMap;

public class VNW extends Plugin{

    HashMap<String, Boolean> isVoted = new HashMap<>();
    int votes = 0;
    boolean isVotingStarted = false;
    int limit;
    int voteTime = 600;
    int updateInterval = 5;
    @Override
    public void init(){
        Events.on(EventType.PlayerJoin.class, t -> {
            isVoted.put(t.player.uuid(), false);
        });
        Events.on(EventType.PlayerLeave.class, r -> {
            if(isVoted.get(r.player.uuid())) {
                isVoted.remove(r.player.uuid());
                votes--;
            }
        });
    }


    @Override
    public void registerServerCommands(CommandHandler handler){
    }


    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("newwave", "Vote to new wave", (c, player) -> {
            if(isVotingStarted) {
                player.sendMessage("[scarlet]Voting is started!");
                return;
            }
            votes++;
            voteTime = 600;
            Call.sendMessage(player + " [gray]has voted to start new wave.Write y/n to agree." + votes + "/" + limit);
            isVoted.put(player.uuid(), true);
            Timer.schedule(() -> {
                voteTime -= updateInterval;
                if(voteTime < 1){
                    Call.sendMessage("[scarlet]Voting is over.The required nuber of votes has not been collected.");
                    voteTime = 5;
                    updateInterval = 0;
                    votes = 0;
                }
            }, 0, updateInterval);
            Vars.netServer.admins.addChatFilter((player1, text) -> {
                if(text.equals("y")) {
                    isVoted.put(player1.uuid(), true);
                    votes++;
                    Call.sendMessage(player1 + " [gray]has voted to start new wave." + votes + "/" + limit);
                    if(votes == limit) {
                        Call.sendMessage("[green]Voting is over.The required number of votes has been collected.Starting a new wave ...");
                        Vars.logic.runWave();
                        votes = 0;
                        voteTime = 5;
                        updateInterval = 0;
                    }
                    return null;
                }
                else if(text.equals("n")) {
                    isVoted.put(player1.uuid(), true);
                    votes--;
                    Call.sendMessage(player1 + " [gray]has voted aginist of start new wave." + votes + "/" + limit);
                    return null;
                }
                return text;
            });
        });
    }
}
