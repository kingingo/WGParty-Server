package net.kingingo.server.stage.stages;

import java.util.ArrayList;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.packets.server.StartMatchPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.TimeSpan;

public class Game extends Stage{
	
	public Game() {
		super(TimeSpan.MINUTE);
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(!isActive())return;
		
	}
	public boolean running() {
		printf("Game Timeout reached");
		return true;
	}
	
	@Override
	public void start() {
		super.start();
		printf("start Game!");
		//START GAME
	}
}
