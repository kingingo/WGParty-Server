package net.kingingo.server.packets.server.ScissorsStonePaper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.packets.Packet;

public class SSPSettingsPacket extends Packet{

	private long start;
	
	public SSPSettingsPacket() {}
	
	public SSPSettingsPacket(long start) {
		this.start=start;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeDouble( ((double)this.start) );
	}

}
