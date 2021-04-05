package net.kingingo.server.packets.client.ladder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import lombok.Getter;
import net.kingingo.server.packets.Packet;

@Getter
public class LadderClickPacket extends Packet{
	
	private UUID uuid;
	private int pos;
	private int start;
	private int range;
	private boolean up;
	private int tries;
	
	public LadderClickPacket() {}
	
	public LadderClickPacket(UUID uuid, int pos, int start, int range, boolean up, int tries) {
		this.uuid = uuid;
		this.pos = pos;
		this.start = start;
		this.range = range;
		this.up = up;
		this.tries = tries;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.uuid = UUID.fromString(in.readUTF());
		this.pos = in.readInt();
		this.start=in.readInt();
		this.range=in.readInt();
		this.up = in.readBoolean();
		this.tries = in.readInt();
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(this.uuid.toString());
		out.writeInt(this.pos);
		out.writeInt(this.start);
		out.writeInt(this.range);
		out.writeBoolean(this.up);
		out.writeInt(this.tries);
	}
	
	public String toString() {
		return this.getPacketName()+" UUID:"+(this.uuid==null ? "null" : this.uuid)+" pos:"+pos+" start:"+start+" range:"+range+" up:"+up+" tries:"+tries;
	}
}
