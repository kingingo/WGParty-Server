package net.kingingo.server.stage.stages;

import java.util.ArrayList;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.packets.server.StartMatchPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.TimeSpan;

public class PlayerChoose extends Stage{
	
	public PlayerChoose() {
		super(TimeSpan.MINUTE/2);
	}
	
	public boolean running() {
		printf("TimeOut Wheel hasn't been rolled...");
		return true;
	}
	
	@Override
	public void start() {
		super.start();
		ArrayList<User> users = new ArrayList<User>(User.getAllStats().keySet());
		User u1 = User.getUser("Felix");
		User u2 = User.getUser("Oskar");

		StartMatchPacket start = new StartMatchPacket(u1,u2,users);
		User.broadcast(start);
		printf("Choose Player with "+u1.getName()+" "+u2.getName());
	}
}
