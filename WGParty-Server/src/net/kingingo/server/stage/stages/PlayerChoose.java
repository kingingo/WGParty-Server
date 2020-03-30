package net.kingingo.server.stage.stages;

import java.util.ArrayList;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.event.events.StateChangeEvent;
import net.kingingo.server.packets.server.StartMatchPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.TimeSpan;

public class PlayerChoose extends Stage{
	
	public User u1;
	public User u2;
	public ArrayList<User> users;
	
	public PlayerChoose() {
		super(TimeSpan.SECOND * 20);
	}
	
	public boolean running() {
		printf("Next Stage Player has been choosen!");
		return true;
	}
	
	@Override
	public void start() {
		super.start();
		setCountdown("game starts in");
		this.users = new ArrayList<User>(User.getAllStats().keySet());
		this.u1 = User.getUser("Felix");
		this.u2 = User.getUser("Oskar");

		StartMatchPacket start = new StartMatchPacket(u1,u2,users);
		broadcast(start);
		printf("Choose Player with "+u1.getName()+" "+u2.getName());
	}
}
