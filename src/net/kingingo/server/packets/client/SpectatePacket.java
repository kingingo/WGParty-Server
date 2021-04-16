package net.kingingo.server.packets.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lombok.Getter;
import net.kingingo.server.packets.Packet;

public class SpectatePacket extends Packet{
	@Getter
	private boolean spectate = false;
	
	public SpectatePacket() {}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.spectate=in.readBoolean();
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {}
	
	public String toString() {
		return this.getPacketName()+" spectate:"+this.spectate;
	}
}
