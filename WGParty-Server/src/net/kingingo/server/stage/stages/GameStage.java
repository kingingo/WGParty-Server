package net.kingingo.server.stage.stages;

import java.util.ArrayList;

import lombok.Getter;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.event.events.StateChangeEvent;
import net.kingingo.server.games.Game;
import net.kingingo.server.games.HigherLower.HigherLower;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Callback;
import net.kingingo.server.utils.TimeSpan;
import net.kingingo.server.utils.Utils;

public class GameStage extends Stage{
	
	private ArrayList<Game> games = new ArrayList<>();
	public Game current;
	@Getter
	public User win;
	@Getter
	public User lose;
	
	public GameStage() {
		super(TimeSpan.SECOND*60);
		
		this.games.add(new HigherLower(new Callback<User[]>() {
			
			@Override
			public void run(User[] list) {
				GameStage game_stage = Stage.get(GameStage.class);
				
				if(list!=null)
				for(User u : list)System.out.println("GAMESTAGE END: "+u.getName());
				
				//Falls list == NULL -> UNENTSCHIEDEN
				if(list==null) {
					System.out.println("NULL GAMEEND");
					game_stage.win = null;
					game_stage.lose = null;
				} else {
					game_stage.win = list[0];
					game_stage.lose = list[1];
				}
				Stage.next();
			}
		}));
	}
	
	public Game randomGame() {
		return this.games.get(Utils.randInt(0, this.games.size()-1));
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(!isActive())return;
		
	}
	
	public boolean running() {
		printf("Game Timeout reached");
		this.current.end();
		return false;
	}
	
	@Override
	public void start() {
		super.start();
		this.win = null;
		this.lose = null;
		this.current = randomGame();
		this.current.start(getUser1(),getUser2());
		
		setCountdown("game ends in");
		printf("start Game!");
		//START GAME
	}
}
