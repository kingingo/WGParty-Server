package net.kingingo.server.stage.stages;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketSendEvent;
import net.kingingo.server.packets.server.CountdownAckPacket;
import net.kingingo.server.packets.server.ToggleStagePacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.TimeSpan;
import net.kingingo.server.utils.Utils;

public class Countdown extends Stage{
	
	private long start = 0;
	
	public Countdown() {
		super(TimeSpan.MINUTE*30);
	}
	
	public void setTime(long time) {
		super.setTime(time);
		this.start = System.currentTimeMillis();
	}
	
	/**
	 * Set Time Difference between User and Server!
	 * @param ev
	 */
	@EventHandler
	public void send(PacketSendEvent ev) {
		if(ev.getPacket() instanceof CountdownAckPacket) {
			CountdownAckPacket ack = ((CountdownAckPacket) ev.getPacket());
			Main.printf(ev.getUser(),"Time Difference "+ev.getUser().getTimeDifference()+"ms");
			ack.setTime(ack.time - ev.getUser().getTimeDifference());
		}
	}
	
	public int running() {
		printf("Countdown over...");
		
		if(User.getPlayingUsers() >= 2) {
			return Stage.NEXT_STAGE;
		}else {
			Stage.currentStage().start();
			return Stage.RUNNING;
		}
	}
	
	public long inMinutes() {
		return this.timeout / (60*1000);
	}
	
	public void start() {
		super.start();
		
		this.start = System.currentTimeMillis();
		printf("Countdown start "+inMinutes()+" min");
		
		setCountdown("next game in");
		User.broadcast(new ToggleStagePacket("table"),null);
		User.broadcast(new ToggleStagePacket("dashboard"),null);
	}
	
	public long getEnd() {
		return this.start+this.timeout;
	}
	
	public String toString() {
		if(!isActive())
			return super.toString();
		else
			return super.toString() + " Time:" + Utils.toTime(getEnd()-System.currentTimeMillis());
	}
}
