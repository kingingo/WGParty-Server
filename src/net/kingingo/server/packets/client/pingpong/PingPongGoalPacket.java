package net.kingingo.server.packets.client.pingpong;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import lombok.Getter;
import net.kingingo.server.packets.Packet;

@Getter
public class PingPongGoalPacket extends Packet{
	
	private UUID uuid;
	private int score = -1;
	
	public PingPongGoalPacket() {}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.uuid = UUID.fromString(in.readUTF());
		this.score = in.readInt();
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(this.uuid.toString());
		out.writeInt(this.score);
	}
	
	public String toString() {
		return this.getPacketName()+" UUID:"+(this.uuid==null ? "null" : this.uuid)+" score:"+score;
	}
}
