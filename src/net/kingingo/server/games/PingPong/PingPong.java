package net.kingingo.server.games.PingPong;

import java.util.Arrays;

import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.games.Game;
import net.kingingo.server.packets.client.pingpong.PingPongUserPacket;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Callback;

public class PingPong extends Game{

	public PingPong(Callback<User[]> endCallback) {
		super(endCallback);
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(isActive()) {
			if(ev.getPacket() instanceof PingPongUserPacket) {
				User.broadcast(ev.getPacket(), State.INGAME, Arrays.asList(ev.getUser()));
			}
		}
	}
}
