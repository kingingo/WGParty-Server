package net.kingingo.server.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lombok.Setter;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.stage.Stage;
@Setter
public class HandshakeAckPacket extends Packet{

	private String name;
	private boolean inGame;
	private boolean accepted;
	
	public HandshakeAckPacket() {}
	
	public HandshakeAckPacket(boolean accept) {
		this.accepted=accept;
	}
	
	public HandshakeAckPacket(String name, boolean accepted) {
		this.name=name;
		this.accepted=accepted;
		this.inGame=Stage.inGame();
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeBoolean(this.accepted);
		
		if(this.accepted) {
			out.writeUTF(this.name);
			out.writeBoolean(this.inGame);
		}
	}

	public String toString() {
		return this.getPacketName() + " accepted:"+this.accepted+", name:"+this.name+", inGame:"+this.inGame;
	}
}

