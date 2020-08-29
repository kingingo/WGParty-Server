package net.kingingo.server.packets.server.higherlower;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.games.HigherLower.Search;
import net.kingingo.server.packets.Packet;

public class HigherLowerSearchPacket extends Packet{
	public Search[] searchs;
	
	public HigherLowerSearchPacket() {}
	
	public HigherLowerSearchPacket(Search[] searchs){
		this.searchs=searchs;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeInt(this.searchs.length);
		for(Search s : searchs)s.writeToOutput(out);
	}
	
	public String toString() {
		return this.getPacketName()+" searchs:"+searchs.length;
	}
}
