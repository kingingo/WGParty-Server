package net.kingingo.server.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lombok.Setter;
import net.kingingo.server.packets.Packet;
@Setter
public class HandshakeAckPacket extends Packet{

	private String name;
	private boolean accepted;
	
	public HandshakeAckPacket() {}
	
	public HandshakeAckPacket(boolean accept) {
		this.accepted=accept;
	}
	
	public HandshakeAckPacket(String name, boolean accepted) {
		this.name=name;
		this.accepted=accepted;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeInt((this.accepted?1:0));
		if(this.accepted)out.writeUTF(this.name);
	}

	public String toString() {
		return this.getPacketName() + " accepted:"+this.accepted+", name:"+this.name;
	}
}

