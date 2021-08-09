package net.kingingo.server.packets.server.pingpong2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.games.PingPong.PingPong;
import net.kingingo.server.games.PingPong.PingPong.Player;
import net.kingingo.server.packets.Packet;

public class PingPongPlayerPacket extends Packet{
	
	public Player player;
	
	public PingPongPlayerPacket() {}
	
	public PingPongPlayerPacket(PingPong.Player player) {
		this.player=player;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.player=Player.parseFromInput(in);
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		this.player.writeToOutput(out,true);
	}
	
	public String toString() {
		return this.getPacketName();
	}
}
