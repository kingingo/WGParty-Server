package net.kingingo.server.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import net.kingingo.server.packets.Packet;


public class EventManager {
	
	
	public static HashMap<Class<? extends Event>,ArrayList<EventComp>> events = new HashMap<>();
//	@Getter
//	public static HashMap<EventListener,HashMap<Integer,ArrayList<Method>>> handlers = new HashMap<EventListener, HashMap<Integer,ArrayList<Method>>>();
	
	public static ArrayList<EventListener> registered = new ArrayList<>();
	
	//unregestiert die Class
		public static boolean unregister(Class c){
			EventListener listener;
			for(int i = 0; i<registered.size(); i++)
				if( (listener=((EventListener) registered.get(i))).getClass().equals(c)){
					unregister(listener);
					return true;
				}
			return false;
		}
		
		public static boolean unregister(EventListener listener) {
			if(registered.contains(listener)) {
				registered.remove(listener);
				
				ArrayList<EventComp> list;
				for(Class<? extends Event> clazz : events.keySet()) {
					list = events.get(clazz);
					
					list.removeIf( e -> (e.getListener() == listener));
				}
				
				return true;
			}
			return false;
		}

		//Regestriert die Class mit den Events
		public static void register(EventListener listener){
			if(registered.contains(listener))return;
			registered.add(listener);
			//Sucht alle Methoden in dem Listener raus
			Method[] methods = listener.getClass().getMethods();
	        for (int i = 0; i < methods.length; ++i) {
	        	//Filtert alle EventHandler raus!
	            EventHandler eventHandler = methods[i].getAnnotation(EventHandler.class);
	            
	            if (eventHandler != null) {
	            	//Fügt ihn zur Liste hinzu
					EventComp event = new EventComp(eventHandler.priority(), methods[i], listener);
					
					if(!events.containsKey(event.getEventClass()))
						events.put(event.getEventClass(), new ArrayList<EventComp>());
					events.get(event.getEventClass()).add(event);
	            }
	        }
	        
	        for(ArrayList<EventComp> list : events.values()) {
	        	Collections.sort(list);
	        }
		}
		
		//Feuert das Event ab
		public static void callEvent(final Event event) {
			ArrayList<EventComp> list = events.get(event.getClass());
			for(int i = 0; i < list.size(); i++) {
				try {
					list.get(i).call(event);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    e.getTargetException().printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
			}
	    }
	
//	//unregestiert die Class
//	public static boolean unregister(Class c){
//		for(int i = 0; i<handlers.size(); i++)
//			if( ((EventListener) handlers.keySet().toArray()[i]).getClass().equals(c)){
//				handlers.remove(i);
//				return true;
//			}
//		return false;
//	}
//	
//	public static boolean unregister(EventListener listener) {
//		if(handlers.containsKey(listener)) {
//			handlers.remove(listener);
//			return true;
//		}
//		return false;
//	}
//
//	//Regestriert die Class mit den Events
//	public static void register(EventListener listener){
//		if(handlers.containsKey(listener))return;
//    	if(!handlers.containsKey(listener))handlers.put(listener, new HashMap<Integer,ArrayList<Method>>());
//		//Sucht alle Methoden in dem Listener raus
//		Method[] methods = listener.getClass().getDeclaredMethods();
//        for (int i = 0; i < methods.length; ++i) {
//        	//Filtert alle EventHandler raus!
//            EventHandler eventHandler = methods[i].getAnnotation(EventHandler.class);
//            
//            if (eventHandler != null) {
//            	//Fügt ihn zur Liste hinzu
//            	
//            	if(!handlers.get(listener).containsKey(eventHandler.priority().getPriority()))handlers.get(listener).put(eventHandler.priority().getPriority(), new ArrayList<Method>());
//            	handlers.get(listener).get(eventHandler.priority().getPriority()).add(methods[i]);
//            }
//        }
//	}
//	
//	//Feuert das Event ab
//	public static void callEvent(final Event event) {
//        for (EventListener listener : getHandlers().keySet()) {
//            for (int i = 0; i<EventPriority.values().length; i++) {
//            		if(getHandlers().get(listener).containsKey(i) && !getHandlers().get(listener).get(i).isEmpty()){
//            			for(Method method : getHandlers().get(listener).get(i)){
//            				if (!event.getClass().getSimpleName().equals(method.getParameterTypes()[0].getSimpleName())) continue;
//                            try {
//                                method.invoke(listener, new Object[]{event});
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//            			}
//            		}
//                }
//            }
//    }
}
