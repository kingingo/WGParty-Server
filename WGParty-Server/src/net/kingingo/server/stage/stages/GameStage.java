package net.kingingo.server.stage.stages;

import java.util.ArrayList;

import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.games.Game;
import net.kingingo.server.games.HigherLower.HigherLower;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.utils.TimeSpan;
import net.kingingo.server.utils.Utils;

public class GameStage extends Stage{
	
	private ArrayList<Game> games = new ArrayList<>();
	public Game current;
	
	public GameStage() {
		super(TimeSpan.SECOND*60);
		
		this.games.add(new HigherLower());
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
		return true;
	}
	
	@Override
	public void start() {
		super.start();
		this.current = randomGame();
		this.current.start(Stage.get(PlayerChoose.class).u1,Stage.get(PlayerChoose.class).u2);
		
		setCountdown("game ends in");
		printf("start Game!");
		//START GAME
	}
}
