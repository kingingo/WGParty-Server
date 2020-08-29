package net.kingingo.server.stage.stages;

import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.packets.client.WheelSpinPacket;
import net.kingingo.server.packets.server.MatchPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.TimeSpan;
import net.kingingo.server.wheel.Wheel;

public class WheelStage extends Stage{
	
	private long rolled = 0;
	
	public WheelStage() {
		super(TimeSpan.SECOND*20);
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(!isActive())return;
		if(ev.getPacket() instanceof WheelSpinPacket) {
			this.rolled=System.currentTimeMillis();
			
			State state;
			for(User user : User.getUsers().values()) {
				state = user.getState();
				if(user.getUuid() != ev.getUser().getUuid() && state == State.INGAME)
					user.write(ev.getPacket());
			}
			
		}
	}

	public boolean running() {
		if(this.rolled != 0) {
			long diff = System.currentTimeMillis() - this.rolled;
			
			if(diff <= TimeSpan.SECOND*7) {
				printf("Wheel sleeps for "+diff+"ms");
				try {
					Thread.sleep(diff);
					printf("Wheel waked up");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		printf(this.rolled==0 ? "Wheel has been rolled :)" : "TimeOut Wheel hasn't been rolled...");
		return true;
	}
	
	@Override
	public void start() {
		super.start();
		setCountdown("time to wheel");
		this.rolled=0;
		printf("Start Celebration and init Wheel");
		GameStage stage = Stage.get(GameStage.class);
		MatchPacket packet = new MatchPacket(stage.win, stage.lose, Wheel.getInstance().getAlk());
		broadcast(packet);
	}
}
