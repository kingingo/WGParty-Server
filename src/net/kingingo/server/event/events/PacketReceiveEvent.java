package net.kingingo.server.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kingingo.server.event.Event;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.user.User;

@AllArgsConstructor
public class PacketReceiveEvent extends Event{
	@Getter
	private User user;
	@Getter
	private Packet packet;
	
	@SuppressWarnings({ "unchecked", "FinalStaticMethod" })
	public <P extends Packet> P getPacket(Class<P> clazz){
		return ((P) packet);
	}
}
