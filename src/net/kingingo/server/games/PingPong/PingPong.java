package net.kingingo.server.games.PingPong;

import java.util.Arrays;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.games.Game;
import net.kingingo.server.packets.client.pingpong.PingPongGoalPacket;
import net.kingingo.server.packets.client.pingpong.PingPongUserPacket;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Callback;

public class PingPong extends Game{
	
	public PingPong(Callback<User[]> endCallback) {
		super(endCallback);
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(isActive() && (ev.getUser().equals(getUser1()) || ev.getUser().equals(getUser2()))) {
			if(ev.getPacket() instanceof PingPongUserPacket) {
				User.broadcast(ev.getPacket(), State.INGAME, Arrays.asList(ev.getUser()));
			}else if(ev.getPacket() instanceof PingPongGoalPacket) {
				PingPongGoalPacket packet = ev.getPacket(PingPongGoalPacket.class);

				int old_score = 0;
				if(ev.getUser().equalsUUID(packet.getUuid()) && getUser1().getUuid().equals(packet.getUuid())) {
					old_score = this.user1_score;
					this.user1_score = packet.getScore();

					Main.debug("Score update from "+ev.getUser().getName()+" for "+User.getUser(packet.getUuid()).getName()+" old score "+old_score+" => new score "+packet.getScore());
					if(this.user1_score >= 8) {
						end();
					}
				}else if(ev.getUser().equalsUUID(packet.getUuid()) && getUser2().getUuid().equals(packet.getUuid())) {
					old_score = this.user2_score;
					this.user2_score = packet.getScore();
					Main.debug("Score update from "+ev.getUser().getName()+" for "+User.getUser(packet.getUuid()).getName()+" old score "+old_score+" => new score "+packet.getScore());
					if(this.user2_score >= 8) {
						end();
					}
				}
			}
		}
	}
}
