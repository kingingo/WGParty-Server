package net.kingingo.server;

import java.io.File;
import lombok.Getter;
import net.kingingo.server.mysql.MySQL;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.ping.PingThread;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.terminal.Terminal;
import net.kingingo.server.user.User;
import net.kingingo.server.user.UserStats;
import net.kingingo.server.utils.TimeSpan;

/**
 * @author obena
 *
 * Powershell CMDS:
 * weinre --boundHost 192.168.178.110 --httpPort 8888
 * interface -> http://192.168.178.110:8888/client/#anonymous
 * add to HTML -> <script src="http://192.168.178.110:8888/target/target-script-min.js"></script>
 * TO-DO:
 * 
 * 
 */

public class Main {
	@Getter
	public static WSocketServer server;
	public static final String WEBSERVER_PATH = "C:"+File.separatorChar+"Users"+File.separatorChar+"obena"+File.separatorChar+"git-workplace"+File.separatorChar+"wgparty"+File.separatorChar+"src";
//	public static final String WEBSERVER_PATH = "C:"+File.separatorChar+"Users"+File.separatorChar+"darouser"+File.separatorChar+"git"+File.separatorChar+"wgparty"+File.separatorChar+"WGParty"+File.separatorChar+"src";
	public static final int DEFAULT_PORT = 8887;
	public static final int PING_TIME = 10; //secs
	public static UserListener listener = null;
	public static PingThread pingThread = null;
	
	public static void registerConsole() {
		Terminal.getInstance();
		printf("Loaded Terminal");
		printf("Loading Cmds:");
		Terminal.loadCommands();
	}

	public static void connectToMySQl() {
		//Not Connected?
		boolean con = false;
		do{
			printf("Loading MySQL...");
			con = MySQL.connect("root", "","localhost","test",3306);
			if(!con) {
				try {
					Main.error("Couldn't connect to MySQL...");
					Thread.sleep(TimeSpan.SECOND * 5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else{
				MySQL.Update("CREATE TABLE IF NOT EXISTS `users` (`uuid` VARCHAR(36),`name` VARCHAR(30));");
				UserStats.createTable();
			}
		}while(!con);
	}
	
	public static void main(String[] a) {
		registerConsole();
		connectToMySQl();
		User.createTestUsers();
		init();
	}
	
	public static void init() {
		Packet.loadPackets();

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
		System.err.println(msg);
	}
	
	public static void debug(String msg) {
		printf("6","DEBUG",msg);
	}
	

	public static void printf(String prefix, String msg) {
		printf("8",prefix,msg);
	}
	
	public static void printf(String color , String prefix, String msg) {
		System.out.println("§"+color+"["+prefix+"]:§f "+msg);
	}
	
	public static void printf(String msg) {
		System.out.println("§8"+msg);
	}
}
