package net.kingingo.server.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kingingo.server.event.Event;
import net.kingingo.server.user.User;

@AllArgsConstructor
@Getter
public class ClientErrorEvent extends Event{
	private User user;
	private Exception ex; 
}
