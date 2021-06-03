package net.kingingo.server.packets.server.pingpong2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.games.PingPong.PingPong;
import net.kingingo.server.games.PingPong.PingPong.Ball;
import net.kingingo.server.games.PingPong.PingPong.Player;
import net.kingingo.server.packets.Packet;

public class PingPongSettingsPacket extends Packet{
	
	public long start;
	public Player[] players;
	public Ball ball;
	
	public PingPongSettingsPacket() {}
	
	public PingPongSettingsPacket(long start, Player[] players, Ball ball) {
		this.start = start;
		this.players = players;
		this.ball = ball;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		for(Player player : this.players)
			player.writeToOutput(out,false);
		
		this.ball.writeToOutput(out,false);
		
		out.writeDouble(((double)this.start));
		out.writeInt(PingPong.CANVAS_WIDTH);
		out.writeInt(PingPong.CANVAS_HEIGHT);
		out.writeInt(PingPong.PADDLE_WIDTH);
		out.writeInt(PingPong.PADDLE_HEIGHT);
	}
	
	public String toString() {
		return this.getPacketName()+" start:"+this.start;
	}
}
