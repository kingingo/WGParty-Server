package net.kingingo.server.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import lombok.Setter;
import net.kingingo.server.packets.Packet;
@Setter
public class RegisterAckPacket extends Packet{

	private UUID uuid;
	
	public RegisterAckPacket() {}
	
	public RegisterAckPacket(UUID uuid) {
		this.uuid=uuid;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(uuid.toString());
	}

	public String toString() {
		return this.getPacketName() + " uuid:"+this.uuid;
	}
}

