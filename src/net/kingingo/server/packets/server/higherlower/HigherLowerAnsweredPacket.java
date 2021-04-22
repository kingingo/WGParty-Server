package net.kingingo.server.packets.server.higherlower;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import net.kingingo.server.packets.Packet;

public class HigherLowerAnsweredPacket extends Packet{
	public UUID uuid;
	public int index;
	public boolean right;
	public boolean higher;
	
	public HigherLowerAnsweredPacket() {}
	
	public HigherLowerAnsweredPacket(UUID uuid, int index,boolean right,boolean higher){
		this.uuid = uuid;
		this.index = index;
		this.right = right;
		this.higher = higher;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(this.uuid.toString());
		out.writeInt(this.index);
		out.writeBoolean(this.right);
		out.writeBoolean(this.higher);
	}
	
	public String toString() {
		return this.getPacketName()+" uuid:"+uuid+" right:"+right+" index:"+index;
	}
}
