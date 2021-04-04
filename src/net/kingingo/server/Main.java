package net.kingingo.server;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import lombok.Getter;
import net.kingingo.server.mysql.MySQL;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.ping.PingThread;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.terminal.Terminal;
import net.kingingo.server.user.User;
import net.kingingo.server.user.UserStats;
import net.kingingo.server.utils.TimeSpan;

public class Main {
	@Getter
	public static WSocketServer server;
	public static final String WEBSERVER_PATH = "C:"+File.separatorChar+"Users"+File.separatorChar+"obena"+File.separatorChar+"git"+File.separatorChar+"wgparty"+File.separatorChar+"src";
//	public static final String WEBSERVER_PATH = "C:"+File.separatorChar+"Users"+File.separatorChar+"darouser"+File.separatorChar+"git"+File.separatorChar+"wgparty"+File.separatorChar+"WGParty"+File.separatorChar+"src";
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
	
	public static boolean loadMySQL() {
		printf("Loading MySQL...");
		if(!MySQL.connect("root", "","localhost","test",3306))return false;
		MySQL.Update("CREATE TABLE IF NOT EXISTS `users` (`uuid` VARCHAR(36),`name` VARCHAR(30));");
		UserStats.createTable();
		return true;
	}
	
	public static void main(String[] a) {
		init();
		User.createTestUsers();
	}
	
	public static void init() {
		Packet.loadPackets();
		registerCommands();
		//Not Connected?
		boolean con = false;
		do{
			con = loadMySQL();
			if(!con) {
				try {
					Main.error("Couldn't connect to MySQL...");
					Thread.sleep(TimeSpan.SECOND * 5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}while(!con);
		
		Main.listener = new UserListener();
		Main.pingThread = new PingThread();
		Stage.init();
		Stage.next();
		
		Main.server = new WSocketServer(DEFAULT_PORT);
		Main.server.start();
	}
	
	public static void printf(User user,String msg) {
		printf(user.toString(), msg);
	}
	
	public static void error(String msg) {
		printf("ERROR",msg);
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
