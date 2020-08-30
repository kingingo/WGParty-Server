package net.kingingo.server.packets.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lombok.Getter;
import net.kingingo.server.packets.Packet;

public class StatsPacket extends Packet{
	@Getter
	private boolean update = false;
	
	public StatsPacket() {}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.update=(in.readInt() == 1 ? true : false);
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {}
	
	public String toString() {
		return this.getPacketName()+" update:"+this.update;
	}
}
