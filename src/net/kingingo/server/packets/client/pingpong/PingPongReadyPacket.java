package net.kingingo.server.packets.client.pingpong;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import net.kingingo.server.packets.Packet;

public class PingPongReadyPacket extends Packet{
	
	public PingPongReadyPacket() {}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {}
	
	public String toString() {
		return this.getPacketName();
	}
}
