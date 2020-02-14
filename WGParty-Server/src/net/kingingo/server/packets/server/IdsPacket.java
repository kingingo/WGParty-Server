package net.kingingo.server.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import net.kingingo.server.packets.Packet;

public class IdsPacket extends Packet{
	
	public IdsPacket() {}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		HashMap<String, Integer> packet_ids = Packet.getPacket_ids();
		out.writeInt(packet_ids.size());
		for(String p : packet_ids.keySet()) {
			out.writeUTF(p);
			out.writeInt(packet_ids.get(p));
		}
	}
	
	public String toString() {
		return this.getPacketName()+" packets:"+Packet.getPacket_ids().size();
	}
}
