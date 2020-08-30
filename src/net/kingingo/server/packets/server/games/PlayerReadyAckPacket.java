package net.kingingo.server.packets.server.games;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.packets.Packet;
import net.kingingo.server.user.User;

public class PlayerReadyAckPacket extends Packet{
	
	private User user;
	
	public PlayerReadyAckPacket() {}
	
	public PlayerReadyAckPacket(User user) {
		this.user=user;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(this.user.getUuid().toString());
	}
	
	public String toString() {
		return this.getPacketName();
	}
}
