package net.kingingo.server.terminal.commands;

import net.kingingo.server.Main;
import net.kingingo.server.music.spotifyHandler;
import net.kingingo.server.terminal.CommandExecutor;

public class SpotifyCommand implements CommandExecutor{

	@Override
	public void onCommand(String[] args) {
		if(args.length >= 2) {
			String cmd = args[0];
			String name = args[1];
			spotifyHandler handler = spotifyHandler.get(name);
			boolean found = handler!=null;
			
			switch(cmd.toLowerCase()) {
			case "load":
				new spotifyHandler(name);
				return;
			case "register":
				if(args.length >= 4 ){
					String clientId = args[2];
					String clientSecret = args[3];
					
					if(found) {
						Main.printf("Unter den Namen '"+name+"' ist schon ein Spotify Handler regestriert!");
					}else {
						new spotifyHandler(name, clientId, clientSecret);
						Main.printf("erstelle handle... "+name+" "+clientId+":"+clientSecret);
					}
					return;
				}
				break;
			case "setcode":
				if(args.length >= 3) {
					String code = args[2];
					
					if(found) {
						handler.setCode(code);
						Main.printf("Der Spotify Code wurde unter "+name+" abgespeichert!");
					} else {
						Main.printf("spotifyHandler "+name+" nicht gefunden...");
					}
					return;
				}
				break;
			}
		}
		
		Main.printf("spotify setcode [name] [code]");
		Main.printf("spotify register [name] [clientId] [clientSecret]");
	}

	@Override
	public String getDescription() {
		return "get spotify code to reach access";
	}
}
