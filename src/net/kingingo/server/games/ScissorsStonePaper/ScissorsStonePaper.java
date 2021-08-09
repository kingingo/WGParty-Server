package net.kingingo.server.games.ScissorsStonePaper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

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
	public int time_in_sec = 20;
	private Choose[] chooses = new Choose[2];
	private Timer timer;
	
	public ScissorsStonePaper(Callback<User[]> endCallback) {
		super(endCallback);
	}

	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(isActive()) {
			if(ev.getPacket() instanceof SSPChoosePacket) {
				//0 => User1 1=>User2, von wem das Packet kam
				Boolean[] is = isUser(ev.getUser());
				
				for(int i = 0; i < 2; i++) {
					if(is[i]) {
						//Wenn die startzeit noch nicht abgelaufen ist wird es geändert
						if((System.currentTimeMillis()+TimeSpan.HALF_SECOND/4) < this.start) {
							chooses[i] = ev.getPacket(SSPChoosePacket.class).getChoose();
						}
						break;
					} 
				}
			}else if(ev.getPacket() instanceof GameStartAckPacket) {
				ev.getUser().write(new SSPSettingsPacket(this.start));
			}
		}
	}
	
	//Wird ausgeführt sobald der Timer abgelaufen ist.
	public void run() {
		if(isActive()) {
			//Sendet was ausgewählt wurde!
			for(int i = 0; i < 2; i++) {
				if(chooses[i]==null) {
					chooses[i] = Choose.values()[Utils.randInt(0, 2)];
				}
				
				User.broadcast(new SSPChoosePacket(chooses[i],(i==0?getUser1():getUser2())), State.INGAME);
			}
//			Main.debug
		}
	}
	
	public void end(User win, User lose) {
		super.end(win,lose);
		Stage.get(GameStage.class).next_game_drawnGame  = false;
		this.timer.cancel();
	}
	
	public void start(User u1, User u2) {
		super.start(u1,u2);
		this.start = System.currentTimeMillis() + TimeSpan.SECOND * time_in_sec;
		
		this.timer = new Timer();
		this.timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				Game game = Stage.get(GameStage.class).current;
				
				if(game instanceof ScissorsStonePaper) {
					((ScissorsStonePaper)game).run();
				}
			}
		}, this.start - (TimeSpan.MILLISECOND * 50));
	}
	
	public enum Choose{
		SCISSORS, STONE, PAPER;
	}
}
