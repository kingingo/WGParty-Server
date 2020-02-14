package net.kingingo.server.packets.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.packets.Packet;
import net.kingingo.server.user.User;

public class MatchPacket extends Packet{

	private User winner;
	private User loser;
	
	public MatchPacket() {}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(this.winner.getName());
		writeBytes(winner.getProfilImage(), out);
		out.writeUTF(this.loser.getName());
		writeBytes(loser.getProfilImage(), out);
		
	}

	public String toString() {
		return "";
	}
}
