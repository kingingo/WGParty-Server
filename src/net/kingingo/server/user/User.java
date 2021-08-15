package net.kingingo.server.user;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.java_websocket.WebSocket;
import org.java_websocket.enums.ReadyState;

import lombok.Getter;
import lombok.Setter;
import net.kingingo.server.Main;
import net.kingingo.server.event.EventManager;
import net.kingingo.server.event.events.StateChangeEvent;
import net.kingingo.server.event.events.UserLoggedInEvent;
import net.kingingo.server.mysql.MySQL;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.packets.client.HandshakePacket;
import net.kingingo.server.packets.client.RegisterPacket;
import net.kingingo.server.packets.server.HandshakeAckPacket;
import net.kingingo.server.packets.server.StatsAckPacket;
import net.kingingo.server.utils.Callback;
import net.kingingo.server.utils.Utils;

public class User {

	private static int CONNECT_COUNTER = 0;
	public static final double ALPHA = 0.125;
	@Getter
	private static HashMap<WebSocket, User> users = new HashMap<WebSocket, User>();
	private static HashMap<UUID, User> uuids = new HashMap<UUID, User>();
	private static HashMap<User, UserStats> stats = new HashMap<User, UserStats>();
	
	public static void broadcast(Packet packet) {
		broadcast(packet, null);
	}

	public static void broadcast(Packet packet,State st) {
		broadcast(packet, st, null);
	}
	
	public static void broadcast(Packet packet,State st, List<User> blackList) {
		for(User u : users.values()) {
			if(st == null || st == u.getState()) {
				if(blackList == null || !blackList.contains(u)) {
					u.write(packet);
				}
			}
		}
	}

	public static String getPath() {
		return Main.WEBSERVER_PATH + File.separatorChar + "images"+File.separatorChar+"profiles"+File.separatorChar+"resize";
	}
	
	public static String getOriginalPath() {
		return Main.WEBSERVER_PATH + File.separatorChar + "images"+File.separatorChar+"profiles"+File.separatorChar+"original";
	}

	public static void createTestUsers() {
		createTestUser("Moritz",8,3);
		createTestUser("Oskar",7,4);
		createTestUser("Henrik",6,5);
		createTestUser("Jonathan",5,6);
		createTestUser("Jonas",4,7);
		createTestUser("Leon",10,0);
	}
	
	//Lego Profile Picture count, if no pic is there
	private static int lego_counter = 0;
	public static User createTestUser(String name,int wins,int loses) {
		User u = new User(null);
		u.uuid = UUID.nameUUIDFromBytes(name.getBytes());
		u.setName(name);

		String format = "png";
		//Checks whether an Profile Picture is already setted
		if(!u.hasProfileImage()){
			Utils.downloadProfile("https://api.randomuser.me/portraits/lego/"+lego_counter+".jpg",name,User.getOriginalPath());
			format = "jpg";
			lego_counter++;
		}

		stats.put(u, new UserStats(u));
		u.getStats().add("loses", loses);
		u.getStats().add("wins", wins);
		MySQL.Update("INSERT IGNORE INTO users (uuid,name) VALUES ('" + u.uuid.toString() + "','" + name + "');");
		
		for(ImgSize size : ImgSize.values())
			Utils.createDirectorie(u.getPath(size));
		
		try {
			String path = u.getOriginalPath(format);
			for(ImgSize size : ImgSize.values())
				Utils.resize(new File(path), u.getPath(size),size.getSize(),size.getSize());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return u;
	}

	/**
	 * @return The size of users who are not tester or spectate!
	 */
	public static int getPlayingUsers(){
		@SuppressWarnings("unchecked") ArrayList<User> list = (ArrayList<User>) stats.clone();
		list.removeIf( u -> (u.isTester() || u.isSpectate()));

		return list.size();
	}
	
	public static HashMap<User, UserStats> getAllStats(){
		return stats;
	}
	
	public static User getUser(String name) {
		for(User u : stats.keySet()) {
			if(u.getName().equalsIgnoreCase(name))return u;
		}
		return null;
	}

	public static User getUser(UUID uuid) {
		return uuids.get(uuid);
	}

	public static User getUser(WebSocket webSocket) {
		return getUsers().get(webSocket);
	}

	private long timeDifference = 0;
	
	@Getter
	private WebSocket socket;
	
	@Setter
	@Getter
	private String name;
	@Getter
	private UUID uuid;
	@Getter
	private int connectId=0;
	
	@Getter
	private State state = State.HANDSHAKE;
	@Getter
	@Setter
	private long offline=0;

	//Round Trip Time
	private long SampleRTT=0; 
	private long estimatedRTT=0;
	@Getter
	@Setter
	private boolean spectate = false;
	@Getter
	@Setter
	private boolean mobile = true;
	
	public User(WebSocket webSocket) {
		this.connectId = CONNECT_COUNTER++;
		this.socket = webSocket;
		if(webSocket!=null)User.getUsers().put(webSocket, this);
	} 
	
	/**
	 * @return Time difference between Client and Server (from the Clock) + RTT (=Round Trip Time)
	 */
	public long getTimeDifference() {
		return this.timeDifference+this.SampleRTT;
	}
	
	/**
	 * Sets a State
	 * @param state
	 */
	public void setState(State state) {
		StateChangeEvent ev = new StateChangeEvent(this, this.state, state);
		this.state=state;
		EventManager.callEvent(ev);
	}
	
	/**
	 * Is State not offline?
	 * @return
	 */
	public boolean isOnline() {
		return this.state != State.OFFLINE;
	}	
	
	/**
	 * Returns the RTT (=Round Trip Time)
	 * @return RTT
	 */
	public long getRTT() {
		return this.SampleRTT;
	}
	
<<<<<<< HEAD
	/**
	 * Sets Time Differece bewtween Client and Server!
	 * calc Alg: timeDiff = (1-ALPHA) * diff + ALPHA * timeDiff;
	 * Alpha = 0.125
	 * @param diff
	 */
	public void setTimeDifference(long diff) {
		diff += (getRTT()/2);
=======
	public void updateStats(){
		StatsAckPacket stats = new StatsAckPacket(this);
		State state;
		for(User user : User.users.values()) {
			state = user.getState();
			if(state == State.DASHBOARD_PAGE && user.getUuid() != this.uuid) {
				user.write(stats);
			}
		}
	}

	public void setTimeDifference(long time) {
		time -= - getRTT();
>>>>>>> a57bf6608841ba93d67524ef2a9b4f4140543ad3
		if(!isOnline())return;
		if(timeDifference == 0) {
			timeDifference = diff;
		}else {
			this.timeDifference = (long) ((1-ALPHA) * diff + ALPHA * this.timeDifference);
		}
	}
<<<<<<< HEAD
	
	/**
	 * Calculates the RTT (=Round Trip Time) 
	 * <p>(PING <-> PONG)
	 * <p>
	 * <p>calc Alg: 
	 * <p>
	 * estimatedRTT = System.currentTimeMillis - estimatedRTT
	 * RTT =  (1-ALPHA) * estimatedRTT + ALPHA * RTT
	 * estimatedRTT = 0;
	 */
	public void RTT(boolean start) {
=======

	public void RTT() {
>>>>>>> a57bf6608841ba93d67524ef2a9b4f4140543ad3
		if(!isOnline())return;
		if(estimatedRTT==0 && start) {
			this.estimatedRTT = System.currentTimeMillis();
		} else if(!start){
			this.estimatedRTT = System.currentTimeMillis() - this.estimatedRTT;
//			Main.debug("Old SampleRTT:"+this.SampleRTT + " estimated RTT:"+this.estimatedRTT);
			this.SampleRTT=(this.SampleRTT == 0 ? this.estimatedRTT : (long) ((1-ALPHA)*this.estimatedRTT+ALPHA*this.SampleRTT));
			this.estimatedRTT=0;
//			Main.debug("new SampleRTT:"+this.SampleRTT);
		}else{
			Main.error("RTT function something went really wrong! Check me out FELIX!");
		}
	}
	
	/**
	 * Returns the UserStats
	 */
	public UserStats getStats() {
		return stats.get(this);
	}

	/**
	 * Sets username
	 * @param name
	 */
	public void init(String name) {
		this.name = name;
	}
	
	/**
	 * The user uuid is null? Not registered?
	 */
	public boolean isUnknown() {
		return this.uuid == null;
	}

	/**
	 * Is Tester User?
	 * Socket null?
	 */
	public boolean isTester() {
		return this.getSocket()==null;
	}
	
	/**
	 * Registers a new connection to a blank User Object
	 */
	public UUID register(RegisterPacket packet) {
		this.uuid = UUID.randomUUID();
		this.name = packet.getName();
		setState(State.REGISTER_PAGE);
		User.stats.put(this, new UserStats(this));
		User.uuids.put(this.uuid, this);
		try {
			for(ImgSize size : ImgSize.values())
				Utils.createDirectorie(getPath(size));
			
			String path = getOriginalPath(packet.format);
			Utils.toFile(path, packet.getImage());
			for(ImgSize size : ImgSize.values())
				Utils.resize(new File(path), getPath(size),size.getSize(),size.getSize());
			
			Main.printf("UUID:"+uuid.toString()+"("+uuid.toString().length()+") "+name+" format:"+packet.getFormat());
			MySQL.Update("INSERT INTO users (uuid,name) VALUES ('" + uuid.toString() + "','" + name + "');");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
<<<<<<< HEAD
		this.updateStats();
		return uuid;
	}

	/**
	 * Updates the userstats by everyone who is on the DASHBOARD_PAGE
	 */
	private void updateStats(){
		StatsAckPacket stats = new StatsAckPacket(this);

		State state;
		for(User user : User.users.values()) {
			state = user.getState();
			if(state == State.DASHBOARD_PAGE && user.getUuid() != this.uuid) {
				user.write(stats);
			}
		}
	}

	/**
	 * Path of the original profile picture
	 */
=======
		updateStats();
		return uuid;
	}

	/**
	 * Checks whether the Profile Picture exists
	 * @return Boolean
	 */
	public boolean hasProfileImage(){
		String path = getOriginalPath("png");
		File file = new File(path);
		return file.exists();
	}

>>>>>>> a57bf6608841ba93d67524ef2a9b4f4140543ad3
	public String getOriginalPath(String format) {
		return User.getOriginalPath()+File.separatorChar+(isTester() ? getName() : getUuid().toString())+"."+format;
	}
	
	/**
	 * Path of the rezied profile picture
	 */
	public String getPath(ImgSize size) {
		return User.getPath()+File.separatorChar+getUuid().toString()+"_"+size.getSize()+"x"+size.getSize()+".jpg";
	}

	/**
	 * Writes an packet to the user
	 * @param packet
	 */
	public void write(Packet packet) {
		if(isTester())return;
		if(!isOnline() && !(packet instanceof HandshakeAckPacket))return;
		if(!getSocket().isOpen() || getSocket().isClosed())return;
		if(getSocket().getReadyState() != ReadyState.OPEN) {
			Main.debug("can't send "+toString()+" "+packet.getPacketName()+" ReadyStage:"+getSocket().getReadyState().toString());
		}else Main.getServer().write(this, packet);
	}
	
	/**
	 * Sets the TCP-Socket for the user
	 */
	public void setSocket(WebSocket socket) {
		User.users.remove(this.socket);
		this.socket=socket;
		User.users.put(this.socket,this);
	}

	/**
	 * Load User by Handshake
	 * @param packet
	 * @return
	 */
	public User load(HandshakePacket packet) {
		UUID uuid = packet.getUuid();
		User found = User.getUser(uuid);
		
		if(found!=null) {
			Main.debug("User already loaded "+found.toString());
			remove();
			found.setSpectate(true);
			found.setMobile(packet.isMobile());
			found.setSocket(this.socket);
			found.write(new HandshakeAckPacket(found.getName(), true));
			found.setState(packet.getState());
			found.updateStats();
			EventManager.callEvent(new UserLoggedInEvent(found));
			return found;
		}
		this.uuid = uuid;

		Main.debug("Loading User "+toString());

		final User user = this;
		user.setMobile(packet.isMobile());
		MySQL.asyncQuery("SELECT * FROM users WHERE uuid='" + this.uuid.toString() + "';", new Callback<ResultSet>() {

			@Override
			public void run(ResultSet rs) {
				try {
					int count = 0;
					while(rs.next()) {
						count++;
						user.setName(rs.getString("name"));
						User.stats.put(user, new UserStats(user));
					}
					boolean accept = count==1;
					
					if (accept && user.getName() != null) {
						User.uuids.put(user.uuid, user);
						user.write(new HandshakeAckPacket(user.getName(), true));
						user.setState(packet.getState());
						user.updateStats();
						EventManager.callEvent(new UserLoggedInEvent(user));
					} else {
						user.write(new HandshakeAckPacket(false));
					}
					Main.debug("User: "+user.toString()+" Loaded:"+count+" -> "+(accept ? "accepted" : "not accepted"));
				} catch (SQLException e) {
					e.printStackTrace();
				} 
			}
		});
		
		return user;
	}
	
	/**
	 * Removes User from the memory!
	 */
	public void remove() {
		if(this.uuid!=null)User.uuids.remove(this.uuid);
		User.stats.remove(this);
		User.users.remove(this.socket);
	}

	/**
	 * Gives a String with Details about the User
	 */
	public String getDetails() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n"+(isTester() ? "Tester" : "User") + " " +getName()+" - "+getUuid().toString()+"\n");
		if(!isTester()) {
			builder.append("	state: " + getState().name() + (isOnline()?"":" since "+getOffline()+"ms")+"\n");
			builder.append("	time difference: "+this.timeDifference+"\n");
			builder.append("	RTT: "+getRTT()+"\n");
			builder.append("	Spectate: "+isSpectate()+"\n");
			builder.append("	Mobile: "+isMobile()+"\n");
		}
		builder.append(getStats().toString()+"\n");
		
		return builder.toString();
	}
	
	/**
	 * Compare the user uuid with another
	 */
	public boolean equalsUUID(UUID uuid) {
		if(uuid==null)return false;
		return uuid.equals(getUuid());
	}
	
	/**
	 * Compare the user with another user
	 * @param u
	 * @return
	 */
	public boolean equals(User u) {
		if(u==null)return false;
		return equalsUUID(u.getUuid());
	}
	
	public String toString() {
		return (name == null ? (this.uuid == null ? "UNKOWN"+this.connectId : this.uuid.toString()) : name.toUpperCase());
	}
}
