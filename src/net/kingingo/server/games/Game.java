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
	public Callback<User[]> endCallback;
	protected int user1_score = 0;
	protected int user2_score = 0;
	
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
	
	public void resend(User user) {
		user.write(new GameStartPacket(getName().toLowerCase()));
	}
	
	public void start(User u1, User u2) {
		reset();
		this.active=true;
		this.user1=u1;
		this.user2=u2;
		
		GameStartPacket packet = new GameStartPacket(getName().toLowerCase());
		Stage.broadcast(packet);
	}

	public void reset() {
		this.user1_score=0;
		this.user2_score=0;
	}
	
	public void end() {
		if(!this.active)return;
		this.active=false;
		User win = null;
		User lose = null;
		
		if(this.user1_score > this.user2_score) {
			win = this.getUser1();
			lose = this.getUser2();
		}else if(this.user1_score < this.user2_score){
			win = this.getUser2();
			lose = this.getUser1();
		}else{
			win = null;
			lose = null;
		}
		print("END -> win:"+win+" lose:"+lose);
		
		end(win,lose,true);
	}
	
	public void end(User win, User lose) {
		end(win,lose,false);
	}
	
	public void end(User win, User lose,boolean force) {
		if(!this.active && !force)return;
		this.active=false;
		this.user1_done=false;
		this.user2_done=false;
		if(this.endCallback!=null)this.endCallback.run((win==null&&lose==null ? null : new User[] {win,lose}));
		reset();
	}

	public User getOther(User u) {
		return u.equals(this.getUser1()) ? this.getUser2() : this.getUser1();
	}
	
	@EventHandler
	public void change(StateChangeEvent ev) {
		if(!isActive())return;
		if(ev.getNewState() == State.OFFLINE) {
			if(ev.getUser().equals(getUser1())) {
				end(getUser2(),getUser1());
			}else if( ev.getUser().equals(getUser2())) {
				end(getUser1(),getUser2());
			}
		}
	}
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	public void print(String msg) {
		Main.printf("d",getName(), msg);
	}
	
	public Boolean[] isUser(User u) {
		return new Boolean[] { u.equals(getUser1()), u.equals(getUser2()) };
	}
}
