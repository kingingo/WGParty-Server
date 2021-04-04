package net.kingingo.server.stage.stages;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.event.events.UserLoggedInEvent;
import net.kingingo.server.packets.client.games.PlayerReadyPacket;
import net.kingingo.server.packets.server.ReadyPacket;
import net.kingingo.server.packets.server.ToggleStagePacket;
import net.kingingo.server.packets.server.games.PlayerReadyAckPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.TimeSpan;

public class ReadyStage extends Stage{
	
	private boolean u1_ready;
	private boolean u2_ready;
	
	public ReadyStage() {
		super(TimeSpan.SECOND*60);
	}
	
	public void forceReady() {
		this.u1_ready=true;
		this.u2_ready=true;
	}

	@Override
	public int running() {
		PlayerChoose.restart(u1_ready, u2_ready);
		return Stage.BREAK;
	}
	
	@EventHandler
	public void login(UserLoggedInEvent ev) {
		if(!isActive())return;

		ev.getUser().write(new ToggleStagePacket("ingame"));
		ev.getUser().write(new ToggleStagePacket("stage1"));
		ev.getUser().write(new ReadyPacket());
		setCountdown(ev.getUser());
		
		if(this.u1_ready)
			ev.getUser().write(new PlayerReadyAckPacket(getUser1(),true));
		if(this.u2_ready)
			ev.getUser().write(new PlayerReadyAckPacket(getUser2(),true));
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(!isActive())return;
		
		if(ev.getPacket() instanceof PlayerReadyPacket) {
			User u1 = PlayerChoose.getUser1();
			User u2 = PlayerChoose.getUser2();
			
			if(ev.getUser().equals(u1)) {
				this.u1_ready = true;
			} else if(ev.getUser().equals(u2)) {
				this.u2_ready = true;
			} else {
				Main.error("ReadyStage: Das Packet PlayerReady wurde von den User:");
				Main.error(ev.getUser().getDetails());
				Main.error("gesendet, aber ist ein Spectator");
				Main.error("User1: "+(u1 == null ? "NULL?!" : u1.getDetails()));
				Main.error("User2: "+(u2 == null ? "NULL?!" : u2.getDetails()));
				return;
			}
			
			//BEIDE SIND BEREIT...
			if(this.u1_ready && this.u2_ready) {
				Stage.next();
			}else {
				broadcast(new PlayerReadyAckPacket(ev.getUser()));
			}
		}
	}
	
	@Override
	public void start() {
		super.start();
		
		broadcast(new ReadyPacket());
		
		this.u1_ready=false;
		this.u2_ready=false;
		User u1 = PlayerChoose.getUser1();
		User u2 = PlayerChoose.getUser2();
		
		if(u1.isTester()) {
			this.u1_ready=true;
		}
		
		if(u2.isTester()) {
			this.u2_ready=true;
		}
		setCountdown("time to get ready");
	}
}
