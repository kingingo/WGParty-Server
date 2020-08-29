package net.kingingo.server.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.Getter;

public class EventComp implements Comparable<EventComp>{
	@Getter
	private EventPriority priority;
	private Method method;
	@Getter
	private EventListener listener;
	
	public EventComp(EventPriority priority, Method method, EventListener listener) {
		this.priority = priority;
		this.method = method;
		this.listener = listener;
	}
	
	public void call(Event event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		this.method.invoke(this.listener, new Object[] {event});
	}
	
	public Class<? extends Event> getEventClass() {
		return (Class<? extends Event>) method.getParameterTypes()[0];
	}
	
	@Override
	public int compareTo(EventComp o) {
		return priority.getPriority() - o.getPriority().getPriority();
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("	Listener: "+listener.toString()+"\n");
		builder.append("	Method: "+method.getName()+"\n");
		builder.append(" 	Priority: "+priority+"\n");
		return builder.toString();
	}
}