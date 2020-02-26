package net.kingingo.server.stage.stages;

import java.util.ArrayList;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.packets.client.WheelSpinPacket;
import net.kingingo.server.packets.server.MatchPacket;
import net.kingingo.server.packets.server.StartMatchPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.TimeSpan;
import net.kingingo.server.wheel.Wheel;

public class WheelStage extends Stage{
	
	public WheelStage() {
		super(TimeSpan.MINUTE * 10);
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(!isActive())return;
		if(ev.getPacket() instanceof WheelSpinPacket) {
			State state;
			for(User user : User.getUsers().values()) {
				state = user.getState();
				if(user.getUuid() != ev.getUser().getUuid() && state == State.DASHBOARD_PAGE)
					user.write(ev.getPacket());
			}
			
			Stage.next();
		}
	}
	

	public boolean running() {
		Main.printf("TimeOut Wheel hasn't been rolled...");
		return true;
	}
	
	@Override
	public void start() {
		super.start();
		printf("Start Celebration and init Wheel");
		MatchPacket packet = new MatchPacket(User.getUser("Oskar"), User.getUser("Felix"), Wheel.getInstance().getAlk());
		User.broadcast(packet);
	}
}
