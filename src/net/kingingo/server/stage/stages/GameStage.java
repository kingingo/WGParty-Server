package net.kingingo.server.stage.stages;

import java.util.ArrayList;

import lombok.Getter;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.UserLoggedInEvent;
import net.kingingo.server.games.Game;
import net.kingingo.server.games.BlackOrRed.BlackOrRed;
import net.kingingo.server.games.HigherLower.HigherLower;
import net.kingingo.server.games.Ladder.Ladder;
import net.kingingo.server.games.PingPong.PingPong;
import net.kingingo.server.games.ScissorsStonePaper.ScissorsStonePaper;
import net.kingingo.server.packets.server.ToggleStagePacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Callback;
import net.kingingo.server.utils.TimeSpan;
import net.kingingo.server.utils.Utils;

public class GameStage extends Stage{
	
	private ArrayList<Game> games = new ArrayList<>();
	public ScissorsStonePaper drawnGame;
	public boolean next_game_drawnGame = false;
	public Game current;
	@Getter
	public User win;
	@Getter
	public User lose;
	
	@EventHandler
	public void login(UserLoggedInEvent ev) {
		if(!isActive())return;

		ev.getUser().write(new ToggleStagePacket("ingame"));
		ev.getUser().write(new ToggleStagePacket("stage2"));
		this.current.resend(ev.getUser());
		setCountdown(ev.getUser());
	}
	
//	@EventHandler
//	public void rec(PacketReceiveEvent ev) {
//		if(!isActive())return;
//		
//		if(ev.getPacket() instanceof GameStartAckPacket) {
//			
//		}
//	}
	
	public GameStage() {
		super(TimeSpan.MINUTE*5);
		Callback<User[]> end = new Callback<User[]>() {
			
			@Override
			public void run(User[] list) {
				GameStage game_stage = Stage.get(GameStage.class);
				
				if(list!=null)
					for(User u : list)System.out.println("GAMESTAGE END: "+u.getName());
				
				//Falls list == NULL -> UNENTSCHIEDEN
				if(list==null) {
					game_stage.win = null;
					game_stage.lose = null;
					printf("Nobody won the game so we start the drawn Game! ScissorsStonePaper... lets go");
					
					game_stage.next_game_drawnGame=true;
					Stage.jump(GameStage.class);
				} else {
					game_stage.win = list[0];
					game_stage.lose = list[1];
					Stage.next();
				}
			}
		};
		
		this.games.add(new HigherLower(end));
		this.games.add(new PingPong(end));
		this.games.add(new Ladder(end));
		this.games.add(new BlackOrRed(end));
		this.drawnGame = new ScissorsStonePaper(end);
	}
	
	public boolean drawn() {
		return this.win == null && this.lose == null;
	}
	
public Game randomGame() {
		return this.games.get(Utils.randInt(0, this.games.size()-1));
	}
	
	public int running() {
		printf("Game Timeout reached");
		this.current.end();
		return this.current instanceof ScissorsStonePaper ? Stage.NEXT_STAGE : Stage.BREAK;
	}
	
	public void stop() {
		super.stop();
		
		if(this.current != null) {
			this.current.end();
		}
	}
	
	public int i = 2;
	@Override
	public void start() {
		super.start();
		this.win = null;
		this.lose = null;
		
		printf("next_game_drawnGame: "+this.next_game_drawnGame);
		setCountdown("game ends in");
		if(this.next_game_drawnGame) {
			this.next_game_drawnGame = false;
			this.current = this.drawnGame;
			this.current.start(getUser1(), getUser2());
			printf("start Drawn Game!");
		} else {
			//this.current = randomGame();
			if(i >= this.games.size())i=0;
			
			this.current = this.games.get(i);
			i++;
			this.current.start(getUser1(),getUser2());
			printf("start Game!");
		}
	}
}
