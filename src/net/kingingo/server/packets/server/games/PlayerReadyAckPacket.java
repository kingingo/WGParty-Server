package net.kingingo.server.packets.server.games;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import net.kingingo.server.Main;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.user.User;

public class PlayerReadyAckPacket extends Packet{
	
	private UUID uuid;
	private boolean force;
	
	public PlayerReadyAckPacket() {}
	
	public PlayerReadyAckPacket(UUID uuid) {
		this(uuid,false);
	}
	
	public PlayerReadyAckPacket(UUID uuid,boolean force) {
		this.uuid=uuid;
		this.force=force;
	}
	
	public PlayerReadyAckPacket(User user) {
		this(user.getUuid(),false);
	}
	
	public PlayerReadyAckPacket(User user,boolean force) {
		this(user.getUuid(),force);
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		Main.printf("PLAYER READY ACK PACKET SEND UUID "+this.uuid.toString());
		out.writeUTF(this.uuid.toString());
		out.writeBoolean(this.force);
	}
	
	public String toString() {
		return this.getPacketName();
	}
}
