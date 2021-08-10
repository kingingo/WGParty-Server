package net.kingingo.server.terminal.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.google.common.collect.Lists;

import net.kingingo.server.Main;
import net.kingingo.server.event.Event;
import net.kingingo.server.event.EventComp;
import net.kingingo.server.event.EventManager;
import net.kingingo.server.games.PingPong.PingPong;
import net.kingingo.server.packets.server.MatchPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.stage.stages.Countdown;
import net.kingingo.server.stage.stages.GameStage;
import net.kingingo.server.stage.stages.PlayerChoose;
import net.kingingo.server.stage.stages.ReadyStage;
import net.kingingo.server.terminal.CommandExecutor;
import net.kingingo.server.terminal.table.TerminalTable;
import net.kingingo.server.terminal.table.TerminalTable.Align;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.user.UserStats;
import net.kingingo.server.utils.TimeSpan;
import net.kingingo.server.utils.Utils;
import net.kingingo.server.wheel.Wheel;
import java.util.Timer;
import java.util.TimerTask;

public class TestCommand implements CommandExecutor{

	@Override
	public void onCommand(String[] args) {
		if(args.length == 0) {
			Main.printf("test ...");
			return;
		}
		switch(args[0].toUpperCase()) {
		case "TIMER_TEST":
			Timer timer = new Timer();
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					Main.printf("5 seconds are over...");
				}
				
			}, 5 * TimeSpan.SECOND);
			Main.printf("Message comes in 5 secs.");
		break;
		case "WIN_SCORE":
			if(args.length > 1) {
				int score = Integer.valueOf(args[1]);
				
				PingPong.WIN_SCORE = score;
			}
			Main.printf("PingPong WIN_SCORE:"+PingPong.WIN_SCORE);
			break;
		case "SETGAME":
			int i = Integer.valueOf(args[1]);

			if(i < 0){
				Stage.get(GameStage.class).next_game_drawnGame = true;
				Main.printf("Set Drawn Game SCISSORS STONE PAPER!!");
			}else{
				Stage.get(GameStage.class).i = i;
				Main.printf("Set Game Position "+i);
			}
			break;
		case "SETSSP":
			int ssp = Integer.valueOf(args[1]);
			Stage.get(GameStage.class).drawnGame.time_in_sec = ssp;
			Stage.get(GameStage.class).drawnGame.resetTime();
			Main.printf("SSP Time: "+ssp+"min");
			break;
		case "THREADS":
			Thread.getAllStackTraces().keySet().forEach((t) -> Main.printf(t.getName() + " Daemon:" + t.isDaemon() + " Alive:" + t.isAlive()));
			break;
		case "RESIZE":
			try {
				String root = "C:"
							+File.separator+"Users"
							+File.separator+"obena"
							+File.separator+"git"
							+File.separator+"WGParty"
							+File.separator+"src"
							+File.separator+"images"
							+File.separator+"profiles";
				
				String newPath = root + File.separator + "resize" + File.separator + "ae90b164-cfbf-475c-b8da-4e4cd641a5e2.jpeg";
				String OriginalPath = root + File.separator + "original" + File.separator + "ae90b164-cfbf-475c-b8da-4e4cd641a5e2.jpeg";
			
				Main.printf("Resize "+ OriginalPath + " to "+ newPath);
				Utils.resize(new File(OriginalPath), newPath,256,256);
				Main.printf("Resize DONE!!");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			break;
		case "SETTIME":
			long time = Integer.valueOf(args[1]) * TimeSpan.MINUTE;
			
			Stage.currentStage().setTime(time);
			Main.printf(Stage.currentStage().getClass().getSimpleName()+" set time to "+time);
			break;
		case "START":
			PlayerChoose.start_u1 = (args.length>=2 ? User.getUser(args[1]) : null);
			PlayerChoose.start_u2 = (args.length>=3 ? User.getUser(args[2]) : null);

			Stage.next();
			Main.printf("Stage PlayerChoose!");
			break;
		case "ADD":
			User user1 = User.getUser(args[1]);
			if(user1!=null) {
				user1.getStats().add(args[2], Integer.valueOf(args[3]));
				user1.getStats().update();
				user1.getStats().save();
			}else Main.debug("User not found "+args[1]);
			
			break;
		case "EVENTS":
			for(Class<? extends Event> clazz : EventManager.events.keySet()) {
				System.out.println("Clazz: "+clazz.getName());
				
				ArrayList<EventComp> comps = EventManager.events.get(clazz);
				for(EventComp comp : comps) {
					System.out.print(comp);
				}
			}
			break;
		case "STATS":
			int tester=0;
			ArrayList<User> users = Lists.newArrayList(User.getAllStats().keySet());
			users.removeIf(u -> !u.isTester() );
			
			
			User user = users.get(Utils.randInt(0, users.size()-1));
				if(user.isTester()) {
					int k = Utils.randInt(0, 5);
					if(Utils.randInt(0, 1)==1) {
						Main.printf(user.getName()+ " add "+k+" loses");
						user.getStats().add("loses", k);
					}else {
						Main.printf(user.getName()+ " add "+k+" wins");
						user.getStats().add("wins", k);
					}
					tester++;
				}
			
			for(UserStats stats : User.getAllStats().values()) {
				stats.update();
			}
			Main.printf("Test User "+tester);
			Main.printf("Normal User "+(User.getAllStats().size()-tester));
			break;
		case "TIME":
			Main.printf("Countdown: "+ Stage.get(Countdown.class).toString());
			break;
		case "NEXT":
			Stage.next();
			break;
		case "READY":
			ReadyStage stage = Stage.get(ReadyStage.class);
			
			if(stage.isActive()) {
				stage.forceReady();
				Main.printf("Ready is forced");
			}else Main.printf("This stage is not activated");
			break;
		case "STAGE":
			if(args.length == 1) {
				TerminalTable table = new TerminalTable(
						new TerminalTable.TerminalColumn[]{
								new TerminalTable.TerminalColumn("Stage", Align.CENTER),
								new TerminalTable.TerminalColumn("active", Align.CENTER)
						}
				);
				for(Stage st : Stage.getStages())
					table.addRow(st.getName(), st.isActive() ? "AKTIV" : "OFFLINE");
				
				table.printTable();
			} else {
				switch(args[1].toUpperCase()) {
				case "NEXT":
					Stage.next();
					Main.printf("Stage NEXT!");
					break;
				case "JUMP":
					if(args.length < 3) {
						return;
					}
					String classname = args[2];
					
					Stage st = null;
					ArrayList<Stage> stages = Stage.getStages();
					for(int j = 0; j < stages.size(); j++) {
						if(stages.get(j).getName().equalsIgnoreCase(classname)) {
							st = stages.get(j);
							break;
						}
					}
					
					if(st == null) {
						Main.printf("Die Stage "+classname+" wurde nicht gefunden!");
					} else {
						Stage.jump(st.getClass());
						Main.printf("Stage jump "+st.getName());
					}				
					break;
				}
			}
			break;
		case "USERS":
			for(User u : User.getUsers().values()) {
				if(!u.isTester())Main.printf(u.getDetails());
			}
			break;
		case "WIN":
			MatchPacket packet = new MatchPacket(User.getUser((args.length==2?args[1]:"Oskar")), User.getUser("Felix"), Wheel.getInstance().getAlk());
			int packetLength = packet.toByteArray().length;
			
			for(User u : User.getUsers().values()) {
				Main.printf(u.getName()+" "+u.isTester()+" "+u.getState().name());
				if(!u.isTester() && (u.getState() == State.DASHBOARD_PAGE)) {
					u.write(packet);
					Main.printf("Send MatchPacket to "+u.getName()+" "+packet.toString()+"("+packetLength+")");
				}
			}
			break;
		default:
			Main.printf("argument '"+args[0]+"' not found for command test...");
			break;
		}
	}

	@Override
	public String getDescription() {
		return "Shows how much clients are connected";
	}

}
