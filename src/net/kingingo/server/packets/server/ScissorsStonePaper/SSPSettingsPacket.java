package net.kingingo.server.packets.server.ScissorsStonePaper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.packets.Packet;

public class SSPSettingsPacket extends Packet{

	private long start;
	private boolean loop_start;
	
	public SSPSettingsPacket() {}
	
	public SSPSettingsPacket(long start) {
		this(start,false);
	}

	public SSPSettingsPacket(long start, boolean loop_start) {
		this.start = start;
		this.loop_start = loop_start;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeDouble( ((double)this.start) );
		out.writeBoolean( this.loop_start );
	}

}
