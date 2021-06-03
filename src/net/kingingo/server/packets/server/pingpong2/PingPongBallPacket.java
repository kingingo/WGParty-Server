package net.kingingo.server.packets.server.pingpong2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.games.PingPong.PingPong;
import net.kingingo.server.games.PingPong.PingPong.Ball;
import net.kingingo.server.packets.Packet;

public class PingPongBallPacket extends Packet{
	
	private Ball ball;
	
	public PingPongBallPacket() {}
	
	public PingPongBallPacket(PingPong.Ball ball) {
		this.ball = ball;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		ball.writeToOutput(out,true);
	}
	
	public String toString() {
		return this.getPacketName()+" x/y:"+this.ball.x+"/"+this.ball.y+" speed:"+this.ball.speed+" vecX/vecY:"+this.ball.vecX+"/"+this.ball.vecY+" radius:"+this.ball.radius;
	}
}
