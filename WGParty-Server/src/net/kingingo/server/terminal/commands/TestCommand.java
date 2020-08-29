package net.kingingo.server.terminal.commands;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import net.kingingo.server.Main;
import net.kingingo.server.event.Event;
import net.kingingo.server.event.EventComp;
import net.kingingo.server.event.EventManager;
import net.kingingo.server.packets.server.MatchPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.stage.stages.Countdown;
import net.kingingo.server.stage.stages.GameStage;
import net.kingingo.server.stage.stages.PlayerChoose;
import net.kingingo.server.stage.stages.ReadyStage;
import net.kingingo.server.terminal.CommandExecutor;
import net.kingingo.server.user.State;
import net.kingingo.server.user.UserStats;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Utils;
import net.kingingo.server.wheel.Wheel;

public class TestCommand implements CommandExecutor{

	@Override
	public void onCommand(String[] args) {
		
		switch(args[0].toUpperCase()) {
		case "START":
			PlayerChoose.start_u1 = (args.length>=2 ? User.getUser(args[1]) : null);
			PlayerChoose.start_u2 = (args.length>=3 ? User.getUser(args[2]) : null);

			Stage.next();
			Main.printf("Stage NEXT!");
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
		case "READY":
			ReadyStage stage = Stage.get(ReadyStage.class);
			
			if(stage.isActive()) {
				stage.forceReady();
				Main.printf("Ready is forced");
			}else Main.printf("This stage is not activated");
			break;
		case "STAGE":
			Stage.next();
			Main.printf("Stage NEXT!");
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
