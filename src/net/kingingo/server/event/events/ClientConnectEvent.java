package net.kingingo.server.event.events;

import org.java_websocket.handshake.ClientHandshake;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kingingo.server.event.Event;
import net.kingingo.server.user.User;

@AllArgsConstructor
@Getter
public class ClientConnectEvent extends Event{
	private User user;
	private ClientHandshake handshake;
}
