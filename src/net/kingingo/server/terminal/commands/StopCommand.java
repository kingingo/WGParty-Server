package net.kingingo.server.terminal.commands;

import java.util.Arrays;
import java.util.List;

import net.kingingo.server.Main;
import net.kingingo.server.mysql.MySQL;
import net.kingingo.server.terminal.CommandExecutor;
import net.kingingo.server.terminal.Terminal;
import net.kingingo.server.user.User;
import net.kingingo.server.user.UserStats;

public class StopCommand implements CommandExecutor{

	private List<String> aliases = Arrays.asList("end","exit");
	
	@Override
	public void onCommand(String[] args) {
		try {
			if(Main.server!=null)Main.server.stop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(UserStats stats : User.getAllStats().values()) {
			stats.save();
		}
		
		MySQL.close();
		Main.printf("Server has stopped!");
		Terminal.getInstance().stop();
		System.exit(0);
	}

	@Override
	public String getDescription() {
		return "Stops the Server";
	}

	@Override
	public boolean isAlias(String alias) {
		return aliases.contains(alias.toLowerCase());
	}
	
}
