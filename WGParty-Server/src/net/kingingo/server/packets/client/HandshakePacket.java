package net.kingingo.server.packets.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import lombok.Getter;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.user.State;

public class HandshakePacket extends Packet{
	@Getter
	public UUID uuid;
	@Getter
	public State state;
	
	public HandshakePacket() {}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.uuid=UUID.fromString(in.readUTF());
		this.state=State.values()[in.readInt()];
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {}
	
	public String toString() {
		return this.getPacketName() + " uuid:"+uuid+" state:"+state.name();
	}
}
