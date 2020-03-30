package net.kingingo.server.games;

import java.io.File;

import lombok.Getter;
import lombok.Setter;
import net.kingingo.server.Main;
import net.kingingo.server.event.EventListener;
import net.kingingo.server.event.EventManager;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.packets.server.games.GameStartPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.User;

public abstract class Game implements EventListener{
	public static final String IMG_PATH = "games";
	@Getter
	private User user1;
	@Getter
	private User user2;
	@Getter
	private boolean active = false;
	
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
	
	public Game() {
		EventManager.register(this);
	}
	
	public void start(User u1, User u2) {
		this.active=true;
		this.user1=u1;
		this.user2=u2;
		
		GameStartPacket packet = new GameStartPacket(getName().toLowerCase());
		Stage.broadcast(packet);
	}
	
	public void end() {
		this.active=false;
	}
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	public void print(String msg) {
		Main.printf(getName(), msg);
	}
}
