package net.kingingo.server.packets.client.pingpong;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.packets.Packet;

public class PingPongResetPacket extends Packet{
	
	public long start;
	
	public PingPongResetPacket() {}
	
	public PingPongResetPacket(long start) {
		this.start = start;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.start = (long) in.readDouble();
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeDouble(((double)this.start));
	}
	
	public String toString() {
		return this.getPacketName()+" start:"+this.start;
	}
}
