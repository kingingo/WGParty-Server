package net.kingingo.server.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import net.kingingo.server.packets.Packet;
import net.kingingo.server.user.User;

public class SetMatchPacket extends Packet{
	public User user1;
	public User user2;
	
	public SetMatchPacket() {}
	
	public SetMatchPacket(User u1, User u2) {
		this.user1=u1;
		this.user2=u2;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(this.user1.getName());
		out.writeUTF(this.user1.getUuid().toString());
		out.writeUTF(this.user2.getName());
		out.writeUTF(this.user2.getUuid().toString());
	}
	
	public String toString() {
		return this.getPacketName()+" User1:"+this.user1.getName()+" User2:"+this.user2.getName();
	}
}
