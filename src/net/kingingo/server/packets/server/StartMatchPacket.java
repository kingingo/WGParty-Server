package net.kingingo.server.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import net.kingingo.server.packets.Packet;
import net.kingingo.server.user.User;

public class StartMatchPacket extends Packet{
	public int roulette_duration = 0;
	public boolean roulette = true;
	public User user1;
	public User user2;
	public ArrayList<User> loaded;
	
	public StartMatchPacket() {}
	
	public StartMatchPacket(User u1, User u2, ArrayList<User> loaded) {
		this(u1,u2,loaded,true);
	}
	
	public StartMatchPacket(User u1, User u2, ArrayList<User> loaded,boolean roulette, int roulette_duration) {
		this.user1=u1;
		this.user2=u2;
		this.loaded=loaded;
		this.roulette = roulette;
		this.roulette_duration=roulette_duration;
	}
	
	public StartMatchPacket(User u1, User u2, ArrayList<User> loaded,boolean roulette) {
		this(u1,u2,loaded,roulette,4);
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		if(!this.loaded.contains(user1))this.loaded.add(user1);
		if(!this.loaded.contains(user2))this.loaded.add(user2);
		Collections.shuffle(this.loaded);
		
		out.writeInt(this.roulette_duration);
		out.writeBoolean(this.roulette);
		
		out.writeInt(getIndex(user1));
		out.writeInt(getIndex(user2));
		
		out.writeUTF(user1.getName());
		out.writeUTF(user2.getName());
		
		out.writeInt(this.loaded.size());
		for(int i = 0; i < this.loaded.size(); i++)
			out.writeUTF(this.loaded.get(i).getUuid().toString());
	}
	
	private int getIndex(User u) {
		for(int i = 0; i < this.loaded.size(); i++) {
			if(this.loaded.get(i).getUuid().compareTo(u.getUuid()) == 0)return i;
		}
		return -1;
	}
	
	public String toString() {
		return this.getPacketName()+" User1:"+this.user1.getName()+" User2:"+this.user2.getName();
	}
}
