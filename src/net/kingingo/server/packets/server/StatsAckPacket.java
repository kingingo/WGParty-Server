package net.kingingo.server.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;
import net.kingingo.server.Main;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.user.User;
import net.kingingo.server.user.UserStats;
@Setter
public class StatsAckPacket extends Packet{

	@Getter
	private HashMap<User,UserStats> stats;
	private User user;
	private UserStats stat;
	
	public StatsAckPacket() {}
	
	public StatsAckPacket(User user, UserStats stat) {
		this.user=user;
		this.stat=stat;
	}
	
	public StatsAckPacket(HashMap<User, UserStats> stats) {
		this.stats=(HashMap<User, UserStats>)stats.clone();
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		if(this.stats==null) {
			out.writeInt(1);
			out.writeUTF(user.getName());
			out.writeUTF(user.getUuid().toString());
			this.stat.writeToOutput(out);
		}else {
			out.writeInt(this.stats.size());
			Main.debug("LENGTH: "+this.stats.size());
			for(User user : this.stats.keySet()) {
				Main.debug("STATSACK: "+user.getName()+" "+user.getUuid().toString());
				out.writeUTF(user.getName());
				out.writeUTF(user.getUuid().toString());
				this.stats.get(user).writeToOutput(out);
			}
		}
		
	}

	public String toString() {
		return this.getPacketName()+" stats:"+this.stats.size();
	}
}

