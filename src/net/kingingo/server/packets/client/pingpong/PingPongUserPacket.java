package net.kingingo.server.packets.client.pingpong;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import net.kingingo.server.packets.Packet;

public class PingPongUserPacket extends Packet{
	
	public UUID uuid;
	public double user_y;
	
	public PingPongUserPacket() {}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.uuid = UUID.fromString(in.readUTF());
		this.user_y = in.readDouble();
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(this.uuid.toString());
		out.writeDouble(this.user_y);
	}
	
	public String toString() {
		return this.getPacketName()+"user_y: "+this.user_y;
	}
}
