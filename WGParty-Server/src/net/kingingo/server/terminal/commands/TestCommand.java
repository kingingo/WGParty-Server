package net.kingingo.server.terminal.commands;

import java.util.ArrayList;

import net.kingingo.server.Main;
import net.kingingo.server.counter.Countdown;
import net.kingingo.server.terminal.CommandExecutor;
import net.kingingo.server.user.Stats;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Utils;

public class TestCommand implements CommandExecutor{

	@Override
	public void onCommand(String[] args) {
		
		switch(args[0].toUpperCase()) {
		case "STATS":
			int tester=0;
			for(User user : User.getAllStats().keySet()) {
				if(user.isTester()) {
					int k = Utils.randInt(0, 5);
					if(Utils.randInt(0, 1)==1) {
						Main.printf(user.getName()+ " add "+k+" loses");
						user.getStats().addLoses(k);
					}else {
						Main.printf(user.getName()+ " add "+k+" wins");
						user.getStats().addWins(k);
					}
					tester++;
					break;
				}
			}
			
			for(Stats stats : User.getAllStats().values()) {
				stats.update();
			}
			Main.printf("Test User "+tester);
			Main.printf("Normal User "+(User.getAllStats().size()-tester));
			break;
		case "TIME":
			Countdown c = Countdown.getInstance();
			Main.printf("Countdown: "+c.toString());
			break;
		case "USERS":
			for(User user : User.getUsers().values()) {
				if(!user.isTester())Main.printf(user.getDetails());
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
