package net.kingingo.server.games.ScissorsStonePaper;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.games.Game;
import net.kingingo.server.packets.client.ScissorsStonePaper.SSPChoosePacket;
import net.kingingo.server.packets.client.games.GameStartAckPacket;
import net.kingingo.server.packets.server.ScissorsStonePaper.SSPSettingsPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.stage.stages.GameStage;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Callback;
import net.kingingo.server.utils.TimeSpan;
import net.kingingo.server.utils.Utils;

public class ScissorsStonePaper extends Game{
	private long start;
	public int time_in_sec = 8;
	private Choose[] chooses = new Choose[2];
	private Timer timer;
	
	public ScissorsStonePaper(Callback<User[]> endCallback) {
		super(endCallback);
		this.timer = new Timer();
	}

	public void resetTime(){
		resetTime(false);
	}

	private void resetTime(boolean loop_start){
		if(this.isActive()){
			this.start = System.currentTimeMillis() + TimeSpan.SECOND * time_in_sec;
			User.broadcast(new SSPSettingsPacket(this.start, loop_start), State.INGAME);
			if(loop_start)
				this.startTimer();
		}
	}

	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(isActive()) {
			if(ev.getPacket() instanceof SSPChoosePacket) {
				//0 => User1 1=>User2, von wem das Packet kam
				Boolean[] is = isUser(ev.getUser());
				
				for(int i = 0; i < 2; i++) {
					if(is[i]) {
						//Wenn die startzeit noch nicht abgelaufen ist wird es geaendert
						if((System.currentTimeMillis()+TimeSpan.HALF_SECOND/4) < this.start) {
							chooses[i] = ev.getPacket(SSPChoosePacket.class).getChoose();

							Main.debug("[SSP] "+ev.getUser().getName()+" chose "+chooses[i].name());
							//User.broadcast(new SSPChoosePacket(ev.getPacket(SSPChoosePacket.class).getChoose(), ev.getUser()), State.INGAME, Arrays.asList(ev.getUser()));
						}
						break;
					} 
				}
			}else if(ev.getPacket() instanceof GameStartAckPacket) {
				ev.getUser().write(new SSPSettingsPacket(this.start));
			}
		}
	}
	
	public void reset(){
		super.reset();
		this.chooses[0]=null;
		this.chooses[1]=null;
	}

	//Wird ausgefuehrt sobald der Timer abgelaufen ist.
	public void run() {
		if(isActive()) {
			for(int i = 0; i < chooses.length; i++){
				//Keine Hand gewählt
				if(chooses[i] == null){
					chooses[i] = Choose.values()[Utils.randInt(0, 2)];
				}
				User.broadcast(new SSPChoosePacket(chooses[i], (i == 0 ? getUser1() : getUser2())), State.INGAME);
			}

			User[] result = getResult();
			try {
				Thread.sleep(5 * TimeSpan.SECOND);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//Unentschieden
			if(result == null){
				Main.debug("[SSP] Unentschieden!");
				this.reset();
				this.resetTime(true);
			}else{
				this.end(result[0],result[1]);
			}
		}
	}

	/**
	 * Shows who wins and who loses
	 * null, if drawn
	 * @return
	 */
	public User[] getResult(){
		//0 => User1 1=>User2
		User win = null;
		User lose = null;

		if(chooses[0] == chooses[1])
			return null;

		switch(chooses[0]){
		case SCISSORS:
			if(chooses[1] == Choose.PAPER){
				win = getUser1();
				lose = getUser2();
			}else{
				win = getUser2();
				lose = getUser1();
			}
			break;
		case STONE:
			if(chooses[1] == Choose.SCISSORS){
				win = getUser1();
				lose = getUser2();
			}else{
				win = getUser2();
				lose = getUser1();
			}
			break;
		case PAPER:
			if(chooses[1] == Choose.STONE){
				win = getUser1();
				lose = getUser2();
			}else{
				win = getUser2();
				lose = getUser1();
			}
			break;
		}

		return new User[]{win,lose};
	}

	public void end() {
		super.end();
		Stage.get(GameStage.class).next_game_drawnGame  = false;
		Main.debug("[SSP] end drawn again!!!");
	}

	public void end(User win, User lose) {
		super.end(win,lose);
		Stage.get(GameStage.class).next_game_drawnGame  = false;
		Main.debug("[SSP] end win="+win.getName()+" lose="+lose.getName());
	}
	
	public void start(User u1, User u2) {
		super.start(u1,u2);
		this.start = System.currentTimeMillis() + TimeSpan.SECOND * time_in_sec;
		this.startTimer();
	}
	
	public void startTimer(){
		this.timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				Game game = Stage.get(GameStage.class).current;
				
				if(game instanceof ScissorsStonePaper) {
					((ScissorsStonePaper)game).run();
				}
			}
		}, this.start - System.currentTimeMillis());
	}

	public enum Choose{
		PAPER, SCISSORS, STONE, ;
	}
}
