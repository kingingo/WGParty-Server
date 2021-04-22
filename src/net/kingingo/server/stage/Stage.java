package net.kingingo.server.stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.EventListener;
import net.kingingo.server.event.EventManager;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.packets.client.CountdownPacket;
import net.kingingo.server.packets.server.CountdownAckPacket;
import net.kingingo.server.stage.stages.Countdown;
import net.kingingo.server.stage.stages.GameStage;
import net.kingingo.server.stage.stages.PlayerChoose;
import net.kingingo.server.stage.stages.ReadyStage;
import net.kingingo.server.stage.stages.WheelStage;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;

//class StageHandler implements Runnable{
//	@Getter
//	private boolean active = false;
//	private Thread thread;
//	
//	protected StageHandler() {
//		start();
//	}
//	
//	public void stop() {
//		this.active=false;
//		this.thread.interrupt();
//	}
//	
//	public void start() {
//		this.active=true;
//		this.thread = new Thread(this);
//		this.thread.setName(this.getClass().getSimpleName());
//		this.thread.start();
//	}
//
//	@Override
//	public void run() {
//		Thread thread;
//		
//		while(this.active) {
//			Stage.currentStage().run();
//		}
//	}
//}

public abstract class Stage implements Runnable, EventListener{
	
	
	private static int currentStage = -1;//0
 //	private static StageHandler handler;
	private static ArrayList<Class<? extends Stage>> stages_order = new ArrayList<>();
	private static HashMap<Class<? extends Stage>, Stage> stages = new HashMap<Class<? extends Stage>,Stage>();
	
//	public static StageHandler getHandler() {
//		if(Stage.handler == null)Stage.handler = new StageHandler();
//		return handler;
//	}
	
	public static ArrayList<Stage> getStages(){
		ArrayList<Stage> stages = new ArrayList<>();
		for(Class<? extends Stage> c : Stage.stages_order) {
			stages.add(Stage.stages.get(c));
		}
		return stages;
	}
	
	public void put(Class<? extends Stage> clazz, Stage stage) {
		stages_order.add(clazz);
		stages.put(clazz, stage);
	}
	
	public static void init() {
		//0
		new Countdown();
		//1
		new PlayerChoose();
		//2
		new ReadyStage();
		//3
		new GameStage();
		//4
		new WheelStage();
		
//		getHandler();
	}
	
	public static <T extends Stage> boolean is(Class<T> clazz) {
		return currentStage().getClass() == clazz;
	}
	
	public static boolean inGame() {
		return !(currentStage() instanceof Countdown);
	}
	
	public static <T extends Stage> T get(int index) {
		int i = 0;
		for(Class<? extends Stage> clazz : stages_order) {
			if(index == i)return (T) Stage.get(clazz);
			i++;
		}
		return null;
	}
	
	public static <T extends Stage> T get(Class<T> clazz) {
		return (T) stages.get(clazz);
	}
	
	public static Stage currentStage() {
		return get(currentStage);
	}
	
	public static <T extends Stage> T jump(Class<T> clazz) {
		T stage = (T) stages.get(clazz);
		Main.printf("JUMP-STAGE", currentStage()+" jump to "+stage);
		if(currentStage>=0) {
			currentStage().stop();
		}
		int index = 0;
		for(Class<? extends Stage> c : stages_order) {
			Main.printf("JUMP", c.getSimpleName()+" "+index);
			if(c == clazz)break;
			index++;
		}
		
		currentStage=index;
		stage.start();
		Main.printf("JUMP-STAGE", "Start "+stage+" stage:"+index);
		
		return stage;
	}
	
	public static Stage next() {
		if(currentStage == 0) {
			for(User u : User.getAllStats().keySet()) {
				if(u.getState() == State.DASHBOARD_PAGE) {
					u.setState(State.INGAME);
				}
			}
		}
		
		if(currentStage>=0) {
			currentStage().stop();
		} 
		
		if(currentStage == (stages.size()-1)) {
			currentStage=-1;
			for(User u : User.getAllStats().keySet()) {
				if(u.getState() == State.INGAME) {
					u.setState(State.DASHBOARD_PAGE);
				}
			}
		}
		Stage old = currentStage();
		
		currentStage++;
		Stage s = currentStage();
		
		s.start();
		Main.printf("STAGE NEXT", currentStage+"("+get((currentStage-1))+") old:"+old+" new:"+s);
		return s;
	}
	
	public static void broadcast(Packet packet, List<User> blackList) {
		User.broadcast(packet, (currentStage == 0 ? State.DASHBOARD_PAGE : State.INGAME),blackList);
	}
	
	public static void broadcast(Packet packet) {
		User.broadcast(packet, (currentStage == 0 ? State.DASHBOARD_PAGE : State.INGAME));
	}
	
	public static User getUser2() {
		return Stage.get(PlayerChoose.class).u2;
	}
	
	public static User getUser1() {
		return Stage.get(PlayerChoose.class).u1;
	}
	
	@Getter
	private boolean active = false;
	private Thread thread;
	@Getter
	protected long timeout;
	protected long start_time;
	protected long end_time;
	protected String previousText;
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(!isActive())return;
		if(ev.getPacket() instanceof CountdownPacket) {
			ev.getUser().setTimeDifference(System.currentTimeMillis() - ev.getPacket(CountdownPacket.class).getTime());
			printf("Send Msg:"+this.previousText+" Time:"+(this.end_time-System.currentTimeMillis()));
			CountdownAckPacket ack = new CountdownAckPacket(this.end_time,this.previousText);
			ev.getUser().write(ack);
		}
	}
	
	public Stage(long timeout) {
		this.timeout=timeout;
		EventManager.register(this);
		put(this.getClass(),this);
	}
	
	public void setCountdown(User user) {
		CountdownAckPacket ack = new CountdownAckPacket(this.end_time,this.previousText);
		user.write(ack);
	}
	
	public void setCountdown(String text) {
		this.previousText=text;
		CountdownAckPacket packet = new CountdownAckPacket(this.end_time,text);
		broadcast(packet);
	}
	
	public void setTime(long time) {
		this.active=false;
		this.timeout = time;
		this.thread.interrupt();
		
		this.thread = new Thread(this);
		this.thread.setName(this.getClass().getSimpleName());
		this.active=true;
		this.thread.start();
	}

	
	public static final int NEXT_STAGE = 0;
	public static final int BREAK = 1;
	public static final int CONTINUE = 2;
	
	public void run() {

		try {
		this.start_time = System.currentTimeMillis();
		loop: while(this.active) {
			this.end_time = System.currentTimeMillis() + this.timeout;
			setCountdown(this.previousText != null ? this.previousText : "");
			Thread.sleep(this.timeout);
			if(this.active) {
				int b = this.running();
				printf("time over do running "+(b==NEXT_STAGE ? "NEXT_STAGE" : (b==1?"BREAK":"CONTINUE")));
				switch(b){
				case NEXT_STAGE:
					Stage.next();
					break loop;
				case BREAK:
					break loop;
				}
			}
		}
		this.active=false;
		printf("stop!");

		} catch (InterruptedException e) {
			printf("Interupt "+getClass().getSimpleName()+" Thread");
		}
	}
	
	public abstract int running();
	
	public void stop() {
		this.active=false;
		this.thread.interrupt();
		this.thread=null;
	}
	
	public void start() {
		printf("start...");
		this.start_time = System.currentTimeMillis();
		this.end_time = System.currentTimeMillis() + this.timeout;
		this.active=true;
		this.thread = new Thread(this);
		this.thread.setName(this.getClass().getSimpleName());
		this.thread.start();
	}
	
	public void printf(String msg) {
		Main.printf("Stage-"+this.getClass().getSimpleName()+"-"+Stage.currentStage, msg);
	}
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	public String toString() {
		return "Stage-"+this.getClass().getSimpleName()+" active:"+this.isActive();
	}
}
