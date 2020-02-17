package net.kingingo.server.packets.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import lombok.Getter;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.user.State;

public class WheelSpinPacket extends Packet{
	@Getter
	public float rand;
	
	public WheelSpinPacket() {}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.rand=in.readFloat();
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeFloat(this.rand);
	}
	
	public String toString() {
		return this.getPacketName() + " rand:"+this.rand;
	}
}
