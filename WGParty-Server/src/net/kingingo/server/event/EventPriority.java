package net.kingingo.server.event;

import lombok.Getter;

public enum EventPriority {
LOWEST(0), //Wird als erstes ausgef�hrt
LOW(1),
MEDIUM(2),
HIGH(3),
HIGHEST(4); //Wird als letztes ausgef�hrt

@Getter
private int priority=0;
EventPriority(int priority){
	this.priority=priority;
}

}
