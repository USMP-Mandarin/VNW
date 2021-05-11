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
        handler.<Player>register("newwave", "Голосование за скип волны", (c, player) -> {
            if(isVotingStarted) {
                player.sendMessage("[scarlet]Голосование уже идёт!");
                return;
            }
            votes++;
            voteTime = 600;
            Call.sendMessage(player + " [accent]проголосовал(а) [green]за[] скип волны. Напишите y/n чтобы согласиться/отказаться.[cyan]" + votes + "/" + limit);
            isVoted.put(player.uuid(), true);
            Timer.schedule(() -> {
                voteTime -= updateInterval;
                if(voteTime < 1){
                    Call.sendMessage("[scarlet]Голосование провалилось.");
                    voteTime = 5;
                    updateInterval = 0;
                    votes = 0;
                }
            }, 0, updateInterval);
            Vars.netServer.admins.addChatFilter((player1, text) -> {
                if(text.equals("y")) {
                    isVoted.put(player1.uuid(), true);
                    votes++;
                    Call.sendMessage(player1 + " [accent]проголосовал(а) [green]за[]скип волны.[cyan]" + votes + "/" + limit);
                    if(votes == limit) {
                        Call.sendMessage("[green]Голосование закончилось успешно! Пропускаем волну...");
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
                    Call.sendMessage(player1 + " [accent]проголосовал(а) [scarlet]против[]скипа волны.[cyan]" + votes + "/" + limit);
                    return null;
                }
                return text;
            });
        });
    }
}
