package net.kingingo.server.user;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.Getter;
import lombok.Setter;
import net.kingingo.server.mysql.MySQL;
import net.kingingo.server.packets.server.StatsAckPacket;

@Getter
public class Stats {
	
	public static void broadcastUpdate() {
		StatsAckPacket packet = new StatsAckPacket(User.getAllStats());
		for(User user : User.getAllStats().keySet()) {
			user.write(packet);
		}
	}
	
	@Setter
	private int wins = 0;
	@Setter
	private int loses = 0;
	private User user;
	@Setter
	private boolean update = false;
	
	public Stats(User user) {
		this.user=user;
	}
	
	public void update() {
		StatsAckPacket packet = new StatsAckPacket(User.getAllStats());
		this.user.write(packet);
	}
	
	public void sendTo(User user) {
		user.write(new StatsAckPacket(this.user,this));
	}
	
	public void addWins(int w) {
		this.wins+=w;
	}
	
	public void addLoses(int l) {
		this.loses+=l;
	}
	
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeInt(this.wins);
		out.writeInt(this.loses);
	}
	
	public void save() {
		MySQL.Update("UPDATE FROM users SET wins='"+wins+"', loses='"+this.loses+"' WHERE uuid='"+this.user.getUuid().toString()+";");
	}
	
	public void loadFromResultSet(ResultSet rs) throws SQLException {
		this.wins = rs.getInt("wins");
		this.loses = rs.getInt("loses");
	}
	
	public String toString() {
		return "wins: "+this.wins+", loses: "+this.loses;
	}
}
