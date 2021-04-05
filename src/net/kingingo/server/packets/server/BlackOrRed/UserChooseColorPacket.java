package net.kingingo.server.packets.server.BlackOrRed;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import lombok.Getter;
import net.kingingo.server.packets.Packet;

@Getter
public class UserChooseColorPacket extends Packet{
	
	private UUID uuid;
	private int deck_card;
	private Color color;
	
	public UserChooseColorPacket() {}
	
	public UserChooseColorPacket(UUID uuid,int deck_card,Color color) {
		this.uuid = uuid;
		this.deck_card=deck_card;
		this.color=color;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.uuid = UUID.fromString(in.readUTF());
		this.color = Color.byId(in.readInt());
		this.deck_card = in.readInt();
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(this.uuid.toString());
		out.writeInt(this.color.getId());
		out.writeInt(this.deck_card);
	}
	
	public String toString() {
		return this.getPacketName()+" UUID:"+(this.uuid==null ? "null" : this.uuid+" deck_card:"+this.deck_card+" color:"+this.color);
	}
	
	public enum Color{
		UNKNOWN(0),
		RED(1),
		BLACK(2);
		@Getter
		private int id;
		private Color(int id) {
			this.id=id;
		}
		
		public static Color byId(int id) {
			return id == 1 ? RED : (id == 2) ? BLACK : UNKNOWN;
		}
	}
}