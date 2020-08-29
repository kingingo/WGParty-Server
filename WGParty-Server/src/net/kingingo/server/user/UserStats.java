package net.kingingo.server.user;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import net.kingingo.server.Main;
import net.kingingo.server.mysql.MySQL;
import net.kingingo.server.packets.server.StatsAckPacket;
import net.kingingo.server.user.stats.Stats;
import net.kingingo.server.utils.Callback;

/**
 * TO-DO
 * 
 * - STATS VON MYSQL LADEN!
 * - STATS IN MYSQL SPEICHERN!
 * - STATS EINZELNT IN DER TABELLE!
 * 
 * @author obena
 *
 */

@Getter
public class UserStats {
	public static final String MYSQL_TABLE = "user_stats";
	
	public static void broadcastUpdate() {
		StatsAckPacket packet = new StatsAckPacket(User.getAllStats());
		for(User user : User.getAllStats().keySet()) {
			user.write(packet);
		}
	}

	@Setter
	private boolean update = false;
	private ArrayList<Stats<?>> stats = new ArrayList<>();
	private User user;
	
	public UserStats(User user) {
		this.user=user;
		try {
			loadStats();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void init() {
		if(getStats("wins")==null) {
			Main.debug("INIT WINS for "+user.getName()+"("+user.getUuid()+")");
			add("wins",true, 0);
		}
		if(getStats("loses")==null) {
			Main.debug("INIT loses for "+user.getName()+"("+user.getUuid()+")");
			add("loses",true, 0);
		}
		if(getStats("spectate")==null) {
			Main.debug("INIT spectate for "+user.getName()+"("+user.getUuid()+")");
			set("spectate",true, false);
		}
	}
	
	public void update() {
		StatsAckPacket packet = new StatsAckPacket(User.getAllStats());
		this.user.write(packet);
	}
	
	public void sendTo(User user) {
		user.write(new StatsAckPacket(this.user,this));
	}
	
	public boolean getBoolean(String key) {
		Stats<Boolean> stat = (Stats<Boolean>) getStats(key);
		
		if(stat!=null)return stat.getValue();
		throw new NullPointerException("No entry found for "+key);
	}
	
	public Integer getInt(String key) {
		Stats<Integer> stat = (Stats<Integer>) getStats(key);
		
		if(stat!=null)return stat.getValue();
		throw new NullPointerException("No entry found for "+key);
	}
	
	private Stats<?> getStats(String key) {
		for(Stats<?> stat : this.stats) 
			if(stat.getKey().equalsIgnoreCase(key)) return stat;
		return null;
	}
	
	private void add(Stats<?> stat) {
		this.stats.add(stat);
	}
	
	public boolean set(String key, boolean a) {
		return set(key, false, a);
	}
	
	private boolean set(String key,boolean create, boolean a) {
		Stats<Boolean> stat = (Stats<Boolean>) getStats(key);
		
		if(stat!=null && !create) {
			stat.setValue(a);
			return true;
		} else if(create){
			stat = new Stats<Boolean>(key, a);
			add(stat);
			Stats.insert(stat, this.user);
			return true;
		}
		return false;
	} 
	
	public void add(String key,int a) {
		add(key,false,a);
	}
	
	public void add(String key,boolean create, int a) {
		Stats<Integer> stat = (Stats<Integer>) getStats(key);
		
		if(stat!=null) {
			int value = stat.getValue();
			value += a;
			stat.setValue(value);
		} else if(create){
			stat = new Stats<Integer>(key, a);
			add(stat);
			Stats.insert(stat, this.user);
		}
 	}
	
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeInt(this.stats.size());
		for(Stats<?> stat : this.stats) {
			stat.writeToOutput(out);
		}
	}
	
	public void save() {
		for(Stats<?> stat : this.stats) {
				
				MySQL.Update("UPDATE "+UserStats.MYSQL_TABLE+" SET stats=? WHERE uuid='"+this.user.getUuid()+"' AND skey='"+stat.getKey()+"';", new Callback<PreparedStatement>() {
					
					@Override
					public void run(PreparedStatement stmt) {
						try {
							stmt.setBytes(1, Stats.serializeStat(stat));
						} catch (SQLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				
//				MySQL.Update("UPDATE "+UserStats.MYSQL_TABLE+" SET stats='"+Stats.serializeStat(stat)+"' WHERE uuid='"+this.user.getUuid()+"' AND key='"+stat.getKey()+"';");
			
		}
	}
	
	public void loadStats() throws SQLException {
		
		UserStats stats = this;
		MySQL.asyncQuery("SELECT * FROM "+UserStats.MYSQL_TABLE+" WHERE uuid='"+this.user.getUuid()+"';", new Callback<ResultSet>() {
			
			@Override
			public void run(ResultSet rs) {
				try {
					
					while(rs.next()) {
						try {
							Stats<?> stat = (Stats<?>)Stats.deserializeStat(rs.getBytes("stats"));
							stats.add(stat);
						} catch (ClassNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}, new Callback<Object>() {

			@Override
			public void run(Object value) {
				init();
			}
		});
		
		
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(Stats<?> stat : this.stats) {
			builder.append("        "+stat.getKey()+" -> "+stat.getValue()+"\n");
		}
		
		return builder.toString();
	}
}
