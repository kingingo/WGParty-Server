package net.kingingo.server.packets.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lombok.Getter;
import net.kingingo.server.packets.Packet;

public class RegisterPacket extends Packet{

	@Getter
	public String name;
	@Getter
	public byte[] image;
	@Getter
	public String format;
	
	public RegisterPacket() {}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.name=in.readUTF();
		this.format = in.readUTF();
		this.image=Packet.readBytes(in);
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {}
	
	public String toString() {
		return this.getPacketName() + " name:"+this.name+", img="+this.image.length+" format:"+this.format;
	}
}
