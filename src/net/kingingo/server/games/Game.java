package net.kingingo.server.games;

import lombok.Getter;
import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.EventListener;
import net.kingingo.server.event.EventManager;
import net.kingingo.server.event.events.StateChangeEvent;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.packets.server.games.GameStartPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Callback;

public abstract class Game implements EventListener{
	@Getter
	private User user1;
	protected boolean user1_done=false;
	@Getter
	private User user2;
	protected boolean user2_done=false;
	@Getter
	private boolean active = false;
	private Callback<User[]> endCallback;
	
	public Game(Callback<User[]> endCallback) {
		EventManager.register(this);
		this.endCallback=endCallback;
	}
	
	public void broadcast(Packet packet) {
		writeU1(packet);
		writeU2(packet);
	}
	
	public void writeU1(Packet packet) {
		this.user1.write(packet);
	}
	
	public void writeU2(Packet packet) {
		this.user2.write(packet);
	}
	
	public void start(User u1, User u2) {
		this.active=true;
		this.user1=u1;
		this.user2=u2;
		
		GameStartPacket packet = new GameStartPacket(getName().toLowerCase());
		Stage.broadcast(packet);
	}
	
	public abstract void end();
	
	public void end(User win, User lose) {
		this.active=false;
		print("END -> SET USER1_DONE "+user1_done+" USER2_DONE "+user2_done);
		this.user1_done=false;
		this.user2_done=false;
		print("END -> SET1 USER1_DONE "+user1_done+" USER2_DONE "+user2_done);
		this.endCallback.run((win==null&&lose==null ? null : new User[] {win,lose}));
	}
	
	@EventHandler
	public void change(StateChangeEvent ev) {
		if(!isActive())return;
		if(ev.getNewState() == State.OFFLINE) {
			if(ev.getUser().equalsUUID(getUser1())) {
				end(getUser2(),getUser1());
			}else if( ev.getUser().equalsUUID(getUser2())) {
				end(getUser1(),getUser2());
			}
		}
	}
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	public void print(String msg) {
		Main.printf(getName(), msg);
	}
}
