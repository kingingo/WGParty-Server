package net.kingingo.server.packets.server.games;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.packets.Packet;

public class GameStartPacket extends Packet{
	public String game;
	
	public GameStartPacket() {}
	
	public GameStartPacket(String game) {
		this.game=game;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(this.game);
	}
	
	public String toString() {
		return this.getPacketName()+" game:"+game;
	}
}
