package net.kingingo.server.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lombok.Setter;
import net.kingingo.server.packets.Packet;
@Setter
public class HandshakeAckPacket extends Packet{

	private String name;
	private byte[] img_arr;
	private boolean accepted;
	
	public HandshakeAckPacket() {}
	
	public HandshakeAckPacket(boolean accept) {
		this.accepted=accept;
	}
	
	public HandshakeAckPacket(String name, byte[] img_arr, boolean accepted) {
		this.name=name;
		this.img_arr=img_arr;
		this.accepted=accepted;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeInt((this.accepted?1:0));
		if(this.accepted)out.writeUTF(this.name);
		if(this.accepted) {
			for(byte b : img_arr)out.writeByte(b);
		}
	}

	public String toString() {
		return this.getPacketName() + " accepted:"+this.accepted+", name:"+this.name+", img="+this.img_arr.length;
	}
}

