package net.kingingo.server.packets.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lombok.Getter;
import net.kingingo.server.packets.Packet;

public class CounterPacket extends Packet{
	
	@Getter
	private long time;
	
	public CounterPacket() {}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.time = (long) in.readDouble();
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {}
	
	public String toString() {
		return this.getPacketName()+" time:"+this.time;
	}
}
