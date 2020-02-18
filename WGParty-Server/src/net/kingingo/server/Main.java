package net.kingingo.server;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import lombok.Getter;
import net.kingingo.server.countdown.Countdown;
import net.kingingo.server.mysql.MySQL;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.ping.PingThread;
import net.kingingo.server.terminal.Terminal;
import net.kingingo.server.user.User;

public class Main {
	@Getter
	public static WSocketServer server;
//	public static final String WEBSERVER_PATH = "C:"+File.separatorChar+"Users"+File.separatorChar+"obena"+File.separatorChar+"git"+File.separatorChar+"wgparty"+File.separatorChar+"WGParty"+File.separatorChar+"src";
	public static final String WEBSERVER_PATH = "C:"+File.separatorChar+"Users"+File.separatorChar+"darouser"+File.separatorChar+"git"+File.separatorChar+"wgparty"+File.separatorChar+"WGParty"+File.separatorChar+"src";
	public static final int DEFAULT_PORT = 8887;
	public static final int PING_TIME = 10; //secs
	private static final SimpleDateFormat DATE_FORMAT_NOW = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static UserListener listener;
	private static PingThread pingThread;
	
	public static void registerCommands() {
		printf("Loading Terminal...");
		Terminal.getInstance();
		Terminal.loadCommands();
	}
	
	public static void loadMySQL() {
		printf("Loading MySQL...");
		MySQL.connect("root", "","localhost","test",3306);
		MySQL.Update("CREATE TABLE IF NOT EXISTS `users` (`uuid` VARCHAR(36),`name` VARCHAR(30),wins INT, loses INT);");
	}

	public static void main(String[] a) {
		init();
		User.createTestUsers();
	}
	
	public static void init() {
		Packet.loadPackets();
		registerCommands();
		loadMySQL();
		Main.listener = new UserListener();
		Main.pingThread = new PingThread();
		Countdown.getInstance().start(30);
		
		Main.server = new WSocketServer(DEFAULT_PORT);
		Main.server.start();
	}
	
	public static void printf(User user,String msg) {
		printf(user.toString(), msg);
	}
	
	public static void debug(String msg) {
		printf("Debug",msg);
	}
	
	public static void printf(String prefix, String msg) {
		System.out.println("["+prefix+"|"+date()+"]: "+msg);
	}
	
	public static void printf(String msg) {
		printf("Main", msg);
	}

	public static String date() {
		return DATE_FORMAT_NOW.format(Calendar.getInstance().getTime());
	}
}
