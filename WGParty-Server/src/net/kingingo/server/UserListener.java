package net.kingingo.server;

import java.util.UUID;

import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.EventListener;
import net.kingingo.server.event.EventManager;
import net.kingingo.server.event.events.ClientConnectEvent;
import net.kingingo.server.event.events.ClientDisonnectEvent;
import net.kingingo.server.event.events.ClientErrorEvent;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.event.events.PacketSendEvent;
import net.kingingo.server.event.events.StateChangeEvent;
import net.kingingo.server.packets.client.HandshakePacket;
import net.kingingo.server.packets.client.PongPacket;
import net.kingingo.server.packets.client.RegisterPacket;
import net.kingingo.server.packets.client.StatsPacket;
import net.kingingo.server.packets.client.WheelSpinPacket;
import net.kingingo.server.packets.server.HandshakeAckPacket;
import net.kingingo.server.packets.server.IdsPacket;
import net.kingingo.server.packets.server.PingPacket;
import net.kingingo.server.packets.server.RegisterAckPacket;
import net.kingingo.server.packets.server.StatsAckPacket;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;

public class UserListener implements EventListener{
	public UserListener() {
		EventManager.register(this);
	}
	
	@EventHandler
	public void connect(ClientConnectEvent ev) {
		Main.printf(ev.getUser(),"connected "+ev.getHandshake().getResourceDescriptor());
		
		ev.getUser().write(new IdsPacket());
	}
	
	@EventHandler
	public void disconnect(ClientDisonnectEvent ev) {
		Main.printf("Disconnected -> "+ev.getUser(),"code:"+ev.getCode()+" reason:"+ev.getReason()+" remote:"+ev.isRemote());
		ev.getUser().setState(State.OFFLINE);
	}
	
	@EventHandler
	public void change(StateChangeEvent ev) {
		if(ev.getNewState() == State.OFFLINE) {
			ev.getUser().setOffline(System.currentTimeMillis());
		} else {
			ev.getUser().setOffline(0);
		}
	}
	
	@EventHandler
	public void err(ClientErrorEvent ev) {
		Main.printf(ev.getUser(),"error: "+ev.getEx().getMessage());
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(!(ev.getPacket() instanceof PongPacket))Main.debug(ev.getUser().toString() + " -> "+ev.getPacket().toString());
		
		if(ev.getPacket() instanceof WheelSpinPacket) {
			State state;
			for(User user : User.getUsers().values()) {
				state = user.getState();
				if(user.getUuid() != ev.getUser().getUuid() && state == State.DASHBOARD_PAGE)
					user.write(ev.getPacket());
			}
		}else if(ev.getPacket() instanceof HandshakePacket) {
			ev.getUser().load(ev.getPacket(HandshakePacket.class));
		}else if(ev.getPacket() instanceof RegisterPacket) {
			UUID uuid = ev.getUser().register(ev.getPacket(RegisterPacket.class));
			ev.getUser().write(new RegisterAckPacket(uuid));
		}else if(ev.getPacket() instanceof StatsPacket) {
			StatsPacket packet = ev.getPacket(StatsPacket.class);
			ev.getUser().getStats().setUpdate(packet.isUpdate());
			
			if(packet.isUpdate()) {
				StatsAckPacket ack = new StatsAckPacket(User.getAllStats());
				ev.getUser().write(ack);
			}
		}
	}
	
	@EventHandler
	public void send(PacketSendEvent ev) {
		if(!(ev.getPacket() instanceof PingPacket))Main.debug(ev.getPacket().toString() + " -> "+ev.getUser().toString());
	}
}
