package net.kingingo.server.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lombok.Setter;
import net.kingingo.server.packets.Packet;

public class ToggleStagePacket extends Packet{
	@Setter
	public String stage;
	
	public ToggleStagePacket() {}
	
	public ToggleStagePacket(String stage) {
		this.stage=stage;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(this.stage);
	}
	
	public String toString() {
		return this.getPacketName()+" Stage:"+ stage;
	}
	
	public ToggleStagePacket clone() {
		return new ToggleStagePacket(stage);
	}
}
