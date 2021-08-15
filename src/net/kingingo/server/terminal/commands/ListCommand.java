package net.kingingo.server.terminal.commands;

import net.kingingo.server.Main;
import net.kingingo.server.terminal.CommandExecutor;
import net.kingingo.server.user.User;

public class ListCommand implements CommandExecutor{

	@Override
	public void onCommand(String[] args) {
		int size = Main.getServer().getConnections().size();
		switch(size) {
		case 0:
			Main.printf("No Client connected.");
			break;
		case 1:
			Main.printf("One Client is connected.");
			break;
		default:
			Main.printf(size + " Clients are connected!");
		}
		User[] list = User.getAllStats().keySet().toArray(new User[0]);
		String users = "";
		for(int i = 0; i < list.length; i++) {
			if(list[i].isOnline())
				users+=list[i].getName() + (list[i].isTester() ? "(T)" : "") + ((i+1) == list.length ? "" : (i == (list.length-2) ? " and " : ","));
		}
		Main.printf(users);
	}

	@Override
	public String getDescription() {
		return "Shows how much clients are connected";
	}

}
