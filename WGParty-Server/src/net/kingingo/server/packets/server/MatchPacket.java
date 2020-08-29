package net.kingingo.server.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.kingingo.server.packets.Packet;
import net.kingingo.server.user.User;
import net.kingingo.server.wheel.Alk;

public class MatchPacket extends Packet{

	private User winner;
	private User loser;
	private boolean drawn;
	private ArrayList<Alk> alk;
	
	public MatchPacket() {}
	
	public MatchPacket(User winner, User loser, ArrayList<Alk> alk) {
		this.winner = winner;
		this.loser = loser;
		this.drawn = (this.winner==null&&this.loser==null?true:false);
		this.alk=alk;
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeBoolean(this.drawn);
		if(!this.drawn) {
			out.writeUTF(this.winner.getName());
			out.writeUTF(this.winner.getUuid().toString());
			
			out.writeUTF(this.loser.getName());
			out.writeUTF(this.loser.getUuid().toString());
		
		
			out.writeInt(this.alk.size());
			for(Alk a : this.alk)a.writeToOutput(out);
		}
	}

	public String toString() {
		return "Unentschieden:"+this.drawn+" Winner:"+this.winner+" Loser:"+this.loser+" Alk:"+this.alk.size();
	}
}
