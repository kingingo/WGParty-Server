package net.kingingo.server.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kingingo.server.event.Cancellable;
import net.kingingo.server.event.Event;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.user.User;

@AllArgsConstructor
@Getter
public class PacketSendEvent  extends Event implements Cancellable{
	@Setter
	private boolean cancelled = false;
	private User user;
	private Packet packet;
}
