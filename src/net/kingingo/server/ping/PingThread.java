package net.kingingo.server.ping;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.EventListener;
import net.kingingo.server.event.EventManager;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.packets.client.PongPacket;
import net.kingingo.server.packets.server.PingPacket;
import net.kingingo.server.user.User;

public class PingThread implements Runnable, EventListener{

	private Thread thread;
	private boolean active=false;
	
	public PingThread() {
		this.thread=new Thread(this);
		this.active=true;
		this.thread.start();
		EventManager.register(this);
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(ev.getPacket() instanceof PongPacket) {
			ev.getUser().RTT();
		}
	}
	
	@Override
	public void run() {
		while(active) {
			PingPacket packet = new PingPacket();
			for(User user : User.getUsers().values()) {
				if(user.isOnline()) {
					user.RTT();
					user.write(packet);
				}
			}
			
			try {
				Thread.sleep(1000 * Main.PING_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
