package net.kingingo.server.stage.stages;

import java.util.Random;

import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.event.events.UserLoggedInEvent;
import net.kingingo.server.packets.client.WheelSpinPacket;
import net.kingingo.server.packets.server.MatchPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.TimeSpan;
import net.kingingo.server.wheel.Wheel;

public class WheelStage extends Stage{
	
	private long rolled = 0;
	private WheelSpinPacket spin_packet;
	
	public WheelStage() {
		super(TimeSpan.SECOND*20);
	}
	
	@EventHandler
	public void login(UserLoggedInEvent ev) {
		if(!isActive())return;

		GameStage stage = Stage.get(GameStage.class);
		MatchPacket packet = new MatchPacket(stage.win, stage.lose, Wheel.getInstance().getAlk());
		ev.getUser().write(packet);
		
		if(this.rolled != 0) {
			ev.getUser().write(spin_packet);
		}
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(!isActive())return;
		if(ev.getPacket() instanceof WheelSpinPacket) {
			if(ev.getUser().equals(Stage.get(GameStage.class).lose)) {
				this.spin_packet = ev.getPacket(WheelSpinPacket.class);
				this.rolled=System.currentTimeMillis();
				
				State state;
				for(User user : User.getUsers().values()) {
					state = user.getState();
					if(user.getUuid() != ev.getUser().getUuid() && state == State.INGAME)
						user.write(ev.getPacket());
				}
			}else printf(ev.getUser() + " send WheelSpinPacket but "+Stage.get(GameStage.class).win+" is loser!");
		}
	}

	public int running() {
		if(this.rolled==0 && !Stage.get(GameStage.class).drawn()) {
			this.rolled=System.currentTimeMillis();
			broadcast(new WheelSpinPacket(new Random().nextFloat()));
		}
		
		long diff = System.currentTimeMillis() - this.rolled;
		
		if(diff <= TimeSpan.SECOND*4) {
			diff = TimeSpan.SECOND*4 - diff;
			printf("Wheel sleeps for "+diff+"ms");
			try {
				Thread.sleep(diff);
				printf("Wheel waked up");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return Stage.NEXT_STAGE;
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
