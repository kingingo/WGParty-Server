package net.kingingo.server.countdown;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.EventListener;
import net.kingingo.server.event.EventManager;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.packets.client.CountdownPacket;
import net.kingingo.server.packets.server.CountdownAckPacket;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Utils;

public class Countdown implements EventListener, Runnable{
	private static Countdown counter;
	
	public static Countdown getInstance() {
		if(counter==null)counter = new Countdown();
		return counter;
	}
	
	private long start = 0;
	private long time = 0;
	private Thread thread;
	
	private Countdown() {
		EventManager.register(this);
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(ev.getPacket() instanceof CountdownPacket) {
			ev.getUser().setTimeDifference(System.currentTimeMillis()-ev.getPacket(CountdownPacket.class).getTime());
			
			CountdownAckPacket ack = new CountdownAckPacket(getEnd());
			ev.getUser().write(ack);
		}
	}
	
	public void run() {
		try {
			Thread.sleep(this.time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		over();
	}
	
	public void over() {
		start(this.time / (60*1000));
	}
	
	public boolean isOver() {
		return System.currentTimeMillis() > (getEnd());
	}
	
	public void start(long min) {
		if(!isOver())return;
		Main.debug("Countedown start "+min+" min");
		this.start = System.currentTimeMillis();
		this.time=min * 60 * 1000;
		this.thread = new Thread(this);
		this.thread.start();
		
		broadcast();
	}
	
	private void broadcast() {
		CountdownAckPacket packet = new CountdownAckPacket(getEnd());
		State state;
		for(User user : User.getUsers().values()) {
			state = user.getState();
			if(state==State.VS_PAGE || state==State.DASHBOARD_PAGE)
				user.write(packet);
		}
	}
	
	public long getEnd() {
		return this.start+this.time;
	}
	
	public String toString() {
		if(isOver())
			return "00:00:00";
		else
			return Utils.toTime(getEnd()-System.currentTimeMillis());
	}
}
