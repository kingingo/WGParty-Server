package net.kingingo.server.games.Ladder;

import java.util.Arrays;

import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.games.Game;
import net.kingingo.server.packets.client.ladder.LadderClickPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Callback;

public class Ladder extends Game{
	
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
					
					if(packet.getTries() == 0) {
						if(ev.getUser().equals(getUser1())) {
							this.user1_done=true;
						}else if(ev.getUser().equals(getUser2())) {
							this.user2_done=true;
						}
						
						if(this.user1_done && this.user2_done) {
							end();
						}
					}
				}
			}
		}
	}
}
