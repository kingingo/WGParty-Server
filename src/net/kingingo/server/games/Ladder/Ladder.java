package net.kingingo.server.games.Ladder;

import java.util.Arrays;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.games.Game;
import net.kingingo.server.packets.client.ladder.LadderClickPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Callback;

public class Ladder extends Game{
	
	private LadderClickPacket user1_details;
	private LadderClickPacket user2_details;
	
	public Ladder(Callback<User[]> endCallback) {
		super(endCallback);
	}
	
	public void resend(User user) {
		super.resend(user);
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(isActive()) {
			if(ev.getUser().equals(getUser1()) || ev.getUser().equals(getUser2())) {
				if(ev.getPacket() instanceof LadderClickPacket) {
					Stage.broadcast(ev.getPacket(), Arrays.asList(ev.getUser()));
					LadderClickPacket packet = ev.getPacket(LadderClickPacket.class);
					boolean done = packet.getTries() == 0 || packet.getPos() == -1;
					
					if(ev.getUser().equals(getUser1())) {
						this.user1_details = packet;
						this.user1_score = this.user1_details.getTries();
						if(done)
							this.user1_done=true;
					}else if(ev.getUser().equals(getUser2())) {
						this.user2_details = packet;
						this.user2_score = this.user2_details.getTries();
						
						if(done)
							this.user2_done=true;
					}

					Main.printf("LadderClickPacket ="+packet);
					Main.printf("user1_done="+this.user1_done);
					Main.printf("user2_done="+this.user2_done);
					
					if(this.user1_done && this.user2_done) {
						end();
					}
				}
			}
		}
	}
}
