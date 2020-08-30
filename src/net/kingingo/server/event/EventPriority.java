package net.kingingo.server.event;

import lombok.Getter;

public enum EventPriority {
LOWEST(4),//Wird als letztes ausgef�hrt 
LOW(3),
MEDIUM(2),
HIGH(1),
HIGHEST(0);//Wird als erstes ausgef�hrt 

@Getter
private int priority=2;
EventPriority(int priority){
	this.priority=priority;
}

}
