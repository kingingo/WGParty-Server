package net.kingingo.server.stage.stages;

import lombok.Setter;
import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.event.events.PacketSendEvent;
import net.kingingo.server.packets.client.CountdownPacket;
import net.kingingo.server.packets.server.CountdownAckPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.TimeSpan;
import net.kingingo.server.utils.Utils;

public class Countdown extends Stage{
	
	private long start = 0;
	@Setter
	private long time = 30 * 60 * 1000;
	
	public Countdown() {
		super(TimeSpan.MINUTE*30);
	}
	
	/**
	 * Set Time Difference between User and Server!
	 * @param ev
	 */
	@EventHandler
	public void send(PacketSendEvent ev) {
		if(ev.getPacket() instanceof CountdownAckPacket) {
			CountdownAckPacket ack = ((CountdownAckPacket) ev.getPacket());
			Main.printf("Time Difference "+ev.getUser().getTimeDifference()+"ms from "+ev.getUser().getName());
			ack.setTime(ack.time - ev.getUser().getTimeDifference());
		}
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(!isActive())return;
		if(ev.getPacket() instanceof CountdownPacket) {
			ev.getUser().setTimeDifference(System.currentTimeMillis() - ev.getPacket(CountdownPacket.class).getTime());
			
			CountdownAckPacket ack = new CountdownAckPacket(getEnd());
			ev.getUser().write(ack);
		}
	}
	
	public boolean running() {
		printf("Countdown over...");
		
		if(User.getAllStats().size()>=4) {
			return true;
		}else {
			Stage.currentStage().start();
			return false;
		}
	}
	
	public long inMinutes() {
		return this.time / (60*1000);
	}
	
	public void start() {
		super.start();
		
		this.start = System.currentTimeMillis();
		printf("Countdown start "+inMinutes()+" min");
		
		CountdownAckPacket packet = new CountdownAckPacket(getEnd());
		User.broadcast(packet);
	}
	
	public long getEnd() {
		return this.start+this.time;
	}
	
	public String toString() {
		if(!isActive())
			return super.toString();
		else
			return super.toString() + " Time:" + Utils.toTime(getEnd()-System.currentTimeMillis());
	}
}
