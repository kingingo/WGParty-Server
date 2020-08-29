package net.kingingo.server.packets.server.higherlower;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import net.kingingo.server.packets.Packet;

public class HigherLowerAnsweredPacket extends Packet{
	public UUID uuid;
	public boolean right;
	
	public HigherLowerAnsweredPacket() {}
	
	public HigherLowerAnsweredPacket(UUID uuid,boolean right){
		this.uuid = uuid;
		this.right = right;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(this.uuid.toString());
		out.writeBoolean(this.right);
	}
	
	public String toString() {
		return this.getPacketName()+" "+uuid+" "+right;
	}
}
