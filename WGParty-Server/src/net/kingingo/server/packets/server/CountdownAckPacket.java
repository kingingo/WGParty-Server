package net.kingingo.server.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.packets.Packet;

public class CountdownAckPacket extends Packet{
	public long time;
	public String text;
	
	public CountdownAckPacket() {}
	
	public CountdownAckPacket(long time) {
		this(time,"next game in");
	}
	
	public CountdownAckPacket(long time,String text) {
		this.time=time;
		this.text=text;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeDouble(((double)this.time));
		out.writeUTF(this.text);
	}
	
	public String toString() {
		return this.getPacketName()+" time:"+this.time+" text:"+this.text;
	}
	
	public CountdownAckPacket clone() {
		return new CountdownAckPacket(time, text);
	}
}
