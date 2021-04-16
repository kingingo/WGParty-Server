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
	
	public StatsAckPacket() {}
	
	public StatsAckPacket(User user) {
		this(user, User.getAllStats().get(user));
	}
	
	public StatsAckPacket(User user, UserStats stat) {
		this.stats = new HashMap<User,UserStats>();
		this.stats.put(user, stat);
	}
	
	public StatsAckPacket(HashMap<User, UserStats> stats) {
		this.stats=(HashMap<User, UserStats>)stats.clone();
	}
	
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeInt(this.stats.size());
		for(User user : this.stats.keySet()) {
			out.writeUTF(user.getName());
			out.writeUTF(user.getUuid().toString());
			out.writeBoolean(user.isSpectate());
			this.stats.get(user).writeToOutput(out);
		}
	}

	public String toString() {
		return this.getPacketName()+" stats:"+this.stats.size();
	}
}

