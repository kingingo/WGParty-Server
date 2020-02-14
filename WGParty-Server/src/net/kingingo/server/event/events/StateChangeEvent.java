package net.kingingo.server.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kingingo.server.event.Event;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;

@AllArgsConstructor
@Getter
public class StateChangeEvent extends Event{
	private User user;
	private State oldState;
	private State newState;
}
