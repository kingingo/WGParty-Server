package net.kingingo.server.stage;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;
import net.kingingo.server.Main;
import net.kingingo.server.countdown.Countdown;
import net.kingingo.server.event.EventListener;
import net.kingingo.server.event.EventManager;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.stage.stages.Game;
import net.kingingo.server.stage.stages.PlayerChoose;
import net.kingingo.server.stage.stages.WheelStage;

public abstract class Stage implements EventListener, Runnable{
	private static int currentStage = -1;
	private static HashMap<Class<? extends Stage>, Stage> stages = new HashMap<Class<? extends Stage>,Stage>();
	
	public static void init() {
		new PlayerChoose();
		new Game();
		new WheelStage();
	}
	
	public static Stage currentStage() {
		int i = 0;
		for(Stage stage : stages.values()) {
			if(currentStage == i)return stage;
			i++;
		}
		return null;
	}
	
	public static <T extends Stage> T jump(Class<T> clazz) {
		T stage = (T) stages.get(clazz);
		if(currentStage>=0) {
			currentStage().deactive();
		}
		int index = 0;
		for(Class<? extends Stage> c : stages.keySet()) {
			if(c == clazz)break;
			index++;
		}
		
		currentStage=index;
		stage.start();
		
		return stage;
	}
	
	public static Stage next() {
		if(currentStage>=0) {
			currentStage().deactive();
		}else if(currentStage == (stages.size()-1)) {
			currentStage=-1;
		}
		
		currentStage++;
		Stage s = currentStage();
		s.start();
		return s;
	}
	
	public void printf(String msg) {
		Main.printf("Stage-"+this.getClass().getSimpleName()+"-"+Stage.currentStage, msg);
	}
	
	@Getter
	private boolean active = false;
	private Thread thread;
	private long timeout;
	
	public Stage(long timeout) {
		this.timeout=timeout;
		EventManager.register(this);
	}
	
	public void run() {
		while(this.active) {
			try {
				Thread.sleep(this.timeout);
				if(this.active) {
					boolean b = this.running();
					if(b)Stage.next();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public abstract boolean running();
	
	public void deactive() {
		this.active=false;
	}
	
	public void start() {
		this.active=true;
		this.thread = new Thread(this);
		this.thread.start();
	}
}
