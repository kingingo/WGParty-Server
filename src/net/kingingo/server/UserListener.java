package net.kingingo.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.EventListener;
import net.kingingo.server.event.EventManager;
import net.kingingo.server.event.EventPriority;
import net.kingingo.server.event.events.ClientConnectEvent;
import net.kingingo.server.event.events.ClientDisonnectEvent;
import net.kingingo.server.event.events.ClientErrorEvent;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.event.events.PacketSendEvent;
import net.kingingo.server.event.events.StateChangeEvent;
import net.kingingo.server.event.events.UserLoggedInEvent;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.packets.client.HandshakePacket;
import net.kingingo.server.packets.client.PongPacket;
import net.kingingo.server.packets.client.RegisterPacket;
import net.kingingo.server.packets.client.SpectatePacket;
import net.kingingo.server.packets.client.StatsPacket;
import net.kingingo.server.packets.server.IdsPacket;
import net.kingingo.server.packets.server.PingPacket;
import net.kingingo.server.packets.server.RegisterAckPacket;
import net.kingingo.server.packets.server.SetMatchPacket;
import net.kingingo.server.packets.server.StatsAckPacket;
import net.kingingo.server.packets.server.pingpong2.PingPongBallPacket;
import net.kingingo.server.packets.server.pingpong2.PingPongPlayerPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.stage.stages.PlayerChoose;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;

public class UserListener implements EventListener{
	public UserListener() {
		EventManager.register(this);
	}
	

	@EventHandler(priority = EventPriority.HIGHEST)
	public void login(UserLoggedInEvent ev) {
		if(Stage.inGame()) {
			ev.getUser().setState(State.INGAME);
			
			if(!(Stage.currentStage() instanceof PlayerChoose)){
				PlayerChoose stage = Stage.get(PlayerChoose.class);
				ev.getUser().write(new SetMatchPacket(stage.u1,stage.u2));
			}
		}
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
			if(!ev.getUser().isUnknown() && ev.getUser().getStats()!=null) {
				ev.getUser().getStats().save();
			}
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
		if(ev.getPacket() instanceof HandshakePacket) {
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
		}else if(ev.getPacket() instanceof SpectatePacket) {
			boolean spec = ev.getUser().isSpectate();
			ev.getUser().setSpectate(ev.getPacket(SpectatePacket.class).isSpectate());
			
			if(spec!=ev.getUser().isSpectate()) {
				StatsAckPacket ack = new StatsAckPacket(ev.getUser());
				User.broadcast(ack);
			}
		}
	}
	
	public static final ArrayList<Class<? extends Packet>> BLACK_LIST = new ArrayList<>(Arrays.asList(PingPacket.class,PongPacket.class, PingPongPlayerPacket.class, PingPongBallPacket.class));
	private static boolean containsBlacklist(Packet packet) {
		for(Class<? extends Packet> clazz : BLACK_LIST) {
			if(clazz.isInstance(packet))
				return true;
		}
		return false;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void recm(PacketReceiveEvent ev) {
		if(!containsBlacklist(ev.getPacket()))
			Main.printf("SERVER <= "+ ( ev.getUser() == null ? "USER IS NULL?!" : ev.getUser()) +" ["+(ev.getPacket() == null ? "PACKET IS NULL?!" : ev.getPacket())+"]");
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void send(PacketSendEvent ev) {
		if(!containsBlacklist(ev.getPacket())) {
			Main.printf("SERVER => "+ ( ev.getUser() == null ? "USER IS NULL?!" : ev.getUser()) +" ["+(ev.getPacket() == null ? "PACKET IS NULL?!" : ev.getPacket())+"]");
		}
	}
}
