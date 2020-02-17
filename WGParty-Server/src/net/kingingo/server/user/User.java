package net.kingingo.server.user;

import java.awt.Image;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.java_websocket.WebSocket;

import lombok.Getter;
import lombok.Setter;
import net.kingingo.server.Main;
import net.kingingo.server.event.EventManager;
import net.kingingo.server.event.events.StateChangeEvent;
import net.kingingo.server.mysql.MySQL;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.packets.client.HandshakePacket;
import net.kingingo.server.packets.server.HandshakeAckPacket;
import net.kingingo.server.utils.Callback;
import net.kingingo.server.utils.Utils;

public class User {
	public static final double ALPHA = 0.125;
	@Getter
	private static HashMap<WebSocket, User> users = new HashMap<WebSocket, User>();
	private static HashMap<UUID, User> uuids = new HashMap<UUID, User>();
	private static HashMap<User, Stats> stats = new HashMap<User, Stats>();
	
	public static void createTestUsers() {
		createTestUser("Moritz",8,3);
		createTestUser("Oskar",7,4);
		createTestUser("Henrik",6,5);
		createTestUser("Jonathan",5,6);
		createTestUser("Jonas",4,7);
		createTestUser("Leon",10,0);
	}
	
	private static void createTestUser(String name,int wins,int loses) {
		User u = new User(null);
		u.uuid = UUID.nameUUIDFromBytes(name.getBytes());
		u.setName(name);
		stats.put(u, new Stats(u));
		u.getStats().setLoses(loses);
		u.getStats().setWins(wins);
	}
	
	public static HashMap<User, Stats> getAllStats(){
		return stats;
	}

	public static User getUser(UUID uuid) {
		return uuids.get(uuid);
	}

	public static User getUser(WebSocket webSocket) {
		return getUsers().get(webSocket);
	}

	@Getter
	@Setter
	private long timeDifference = 0;
	
	@Getter
	private WebSocket socket;
	
	@Setter
	@Getter
	private String name;
	@Getter
	private UUID uuid;
	@Getter
	private byte[] profilImage;
	
	@Getter
	private State state = State.HANDSHAKE;
	@Getter
	@Setter
	private long offline=0;
	
	private long SampleRTT=0; //Round Trip Time
	private long estimatedRTT=0;
	
	public User(WebSocket webSocket) {
		this.socket = webSocket;
		User.getUsers().put(webSocket, this);
	} 
	
	public void setState(State state) {
		StateChangeEvent ev = new StateChangeEvent(this, this.state, state);
		this.state=state;
		EventManager.callEvent(ev);
	}
	
	public boolean isOnline() {
		return this.state != State.OFFLINE;
	}	
	
	public long getRTT() {
		return this.SampleRTT;
	}
	
	public void RTT() {
		if(!isOnline())return;
		if(estimatedRTT==0) {
			this.estimatedRTT = System.currentTimeMillis();
		} else {
			this.estimatedRTT = System.currentTimeMillis() - this.estimatedRTT;
//			Main.debug("Old SampleRTT:"+this.SampleRTT + " estimated RTT:"+this.estimatedRTT);
			this.SampleRTT=(this.SampleRTT == 0 ? this.estimatedRTT : (long) ((1-ALPHA)*this.estimatedRTT+ALPHA*this.SampleRTT));
			this.estimatedRTT=0;
//			Main.debug("new SampleRTT:"+this.SampleRTT);
		}
	}
	
	public Stats getStats() {
		return stats.get(this);
	}

	public void init(String name) {
		this.name = name;
	}

	public boolean isTester() {
		return this.getSocket()==null;
	}
	
	public UUID register(String name,byte[] arr_img) {
		this.uuid = UUID.randomUUID();
		this.name = name;
		setState(State.REGISTER_PAGE);
		try {
			Utils.createDirectorie(getPath());
			Utils.toFile(getPath(), arr_img);
			this.profilImage = arr_img;
			Main.printf("UUID:"+uuid.toString()+"("+uuid.toString().length()+") "+name);
			MySQL.Update("INSERT INTO users (uuid,name) VALUES ('" + uuid.toString() + "','" + name + "');");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return uuid;
	}

	public String getPath() {
		return File.separatorChar + "images" + File.separatorChar + uuid.toString() + File.separatorChar + "img.png";
	}

	public void write(Packet packet) {
		if(isTester())return;
		if(!isOnline() && !(packet instanceof HandshakeAckPacket))return;
		if(!getSocket().isOpen() || getSocket().isClosed())return;
		Main.getServer().write(this, packet);
	}
	
	public void setSocket(WebSocket socket) {
		User.users.remove(this.socket);
		this.socket=socket;
		User.users.put(this.socket,this);
	}

	public User load(HandshakePacket packet) {
		UUID uuid = packet.getUuid();
		User found = User.getUser(uuid);
		
		if(found!=null) {
			Main.debug("User already loaded "+found.toString());
			remove();
			
			found.setSocket(this.socket);
			found.write(new HandshakeAckPacket(found.getName(),new byte[0], true));
			found.setState(packet.getState());
			return found;
		}
		stats.put(this, new Stats(this));
		User.uuids.put(uuid, this);
		this.uuid = uuid;
		try {
			this.profilImage = Files.readAllBytes(new File(getPath()).toPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Main.debug("Loading User "+toString());

		final User user = this;
		MySQL.asyncQuery("SELECT * FROM users WHERE uuid='" + this.uuid.toString() + "';", new Callback<ResultSet>() {

			@Override
			public void run(ResultSet rs) {
				try {
					int count = 0;
					while(rs.next()) {
						count++;
						user.setName(rs.getString("name"));
						user.getStats().loadFromResultSet(rs);
					}
					Main.debug("User: "+user.toString()+" Loaded:"+count+" -> "+(count==1 ? "accepted" : "not accepted"));
					
					if (count == 1 && user.getName() != null) {
						byte[] img_arr = Utils.toBytes(user.getPath());
						user.write(new HandshakeAckPacket(user.getName(),img_arr, true));
					} else {
						user.write(new HandshakeAckPacket(false));
					}
					user.setState(packet.getState());
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		return user;
	}
	
	public void remove() {
		if(this.uuid!=null)User.uuids.remove(this.uuid);
		User.stats.remove(this);
		User.users.remove(this.socket);
	}

	public String getDetails() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n"+(isTester() ? "Tester" : "User") + " " +getName()+" - "+getUuid().toString()+"\n");
		if(!isTester()) {
			builder.append("	state: " + getState().name() + (isOnline()?"":" since "+getOffline()+"ms")+"\n");
			builder.append("	time difference: "+this.timeDifference+"\n");
			builder.append("	RTT: "+getRTT()+"\n");
		}
		builder.append("	"+getStats().toString()+"\n");
		
		return builder.toString();
	}
	
	public String toString() {
		return (name == null ? (this.uuid == null ? "UNKOWN" : this.uuid.toString()) : name.toUpperCase());
	}
}
