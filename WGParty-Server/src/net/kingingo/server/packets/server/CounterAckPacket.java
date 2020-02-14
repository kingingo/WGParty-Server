package net.kingingo.server.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.packets.Packet;

public class CounterAckPacket extends Packet{
	public long time;
	
	public CounterAckPacket() {}
	
	public CounterAckPacket(long time) {
		this.time=time;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeDouble(((double)this.time));
	}
	
	public String toString() {
		return this.getPacketName()+" time:"+this.time;
	}
}
