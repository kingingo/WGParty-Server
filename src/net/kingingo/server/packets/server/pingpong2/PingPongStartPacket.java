package net.kingingo.server.packets.server.pingpong2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.games.PingPong.PingPong;
import net.kingingo.server.packets.Packet;

public class PingPongStartPacket extends Packet{
	
	public long start;
	
	public PingPongStartPacket() {}
	
	public PingPongStartPacket(long start) {
		this.start = start;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeDouble(((double)this.start));
	}
	
	public String toString() {
		return this.getPacketName()+" start:"+this.start;
	}
}
