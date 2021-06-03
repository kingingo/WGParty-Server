package net.kingingo.server.packets.server.pingpong2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lombok.Getter;
import net.kingingo.server.Main;
import net.kingingo.server.packets.Packet;

@Getter
public class PingPongGoalPacket extends Packet{
	
	private int uid;
	private int score = -1;
	
	public PingPongGoalPacket() {}
	
	public PingPongGoalPacket(int uid, int score) {
		this.uid = uid;
		this.score = score;
		
		Main.printf("§b", getPacketName(), "ID:"+uid+" score:"+score);
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.uid = in.readInt();
		this.score = in.readInt();
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeInt(this.uid);
		out.writeInt(this.score);
	}
	
	public String toString() {
		return this.getPacketName()+" UID:"+this.uid+" score:"+score;
	}
}
