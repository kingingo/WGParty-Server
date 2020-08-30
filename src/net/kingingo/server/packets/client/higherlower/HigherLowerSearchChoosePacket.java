package net.kingingo.server.packets.client.higherlower;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.packets.Packet;

public class HigherLowerSearchChoosePacket extends Packet{
	public boolean higher;
	public int leftIndex;
	public int rightIndex;
	
	public HigherLowerSearchChoosePacket() {}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.higher = in.readBoolean();
		this.leftIndex = in.readInt();
		this.rightIndex = in.readInt();
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {}
	
	public String toString() {
		return this.getPacketName()+" choose:"+(this.higher ? "higher" : "lower");
	}
}
