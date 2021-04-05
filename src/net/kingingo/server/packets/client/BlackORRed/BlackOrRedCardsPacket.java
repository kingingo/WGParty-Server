package net.kingingo.server.packets.client.BlackORRed;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.packets.Packet;

public class BlackOrRedCardsPacket extends Packet{
	private int show_cards = -1;
	private String[] cards;
	
	public BlackOrRedCardsPacket() {}
	
	public BlackOrRedCardsPacket(String[] cards,int show_cards) {
		this.cards = cards;
		this.show_cards=show_cards;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.show_cards = in.readInt();
		this.cards = new String[in.readInt()];
		for(int i = 0; i < this.cards.length; i++)
			this.cards[i] = in.readUTF();
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeInt(this.show_cards);
		out.writeInt(this.cards.length);
		for(int i = 0; i < this.cards.length; i++)
			out.writeUTF(this.cards[i]);
	}
	
	public String toString() {
		return this.getPacketName()+" cards:"+(this.cards==null?"NULL":this.cards.length+" show_cards:"+this.show_cards);
	}
}