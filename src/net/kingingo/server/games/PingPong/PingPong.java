package net.kingingo.server.games.PingPong;

import java.util.Arrays;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.games.Game;
import net.kingingo.server.packets.client.games.GameStartAckPacket;
import net.kingingo.server.packets.client.pingpong.PingPongGoalPacket;
import net.kingingo.server.packets.client.pingpong.PingPongReadyPacket;
import net.kingingo.server.packets.client.pingpong.PingPongResetPacket;
import net.kingingo.server.packets.client.pingpong.PingPongUserPacket;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Callback;
import net.kingingo.server.utils.TimeSpan;

public class PingPong extends Game{
	public static int WIN_SCORE = 8;
	private PingPongUserPacket[] packets = new PingPongUserPacket[2];
	private Boolean[] ready = new Boolean[]{false,false};
	private long start = -1;
	
	public PingPong(Callback<User[]> endCallback) {
		super(endCallback);
	}
	
	public void start(User u1, User u2) {
		print("RESET START READY!!");
		ready = new Boolean[] {false,false};
		packets = new PingPongUserPacket[2];
		start = -1;
		super.start(u1, u2);
	}
	
	public void resend(User user) {
		super.resend(user);
	}
	
	private void allReady() {
		print("§aREADY_0:"+this.ready[0]+" READY_1:"+this.ready[1]);
		if(this.ready[0]&&this.ready[1]) {
			this.start = System.currentTimeMillis() + TimeSpan.SECOND * 5;
			print("§cBoth Users are ready SEND RESET TO START THE GAME!! "+this.start);
			User.broadcast(new PingPongResetPacket(this.start), State.INGAME);
		}
	}
	
	private int getIndex(User u) {
		return u.equals(getUser1()) ? 0 : (u.equals(getUser2()) ? 1 : -1);
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(isActive()) {
			boolean is_u1 = ev.getUser().equals(getUser1());
			boolean is_u2 = ev.getUser().equals(getUser2());
			
			if(is_u1 || is_u2) {
				if(ev.getPacket() instanceof PingPongUserPacket) {
					PingPongUserPacket packet = ev.getPacket(PingPongUserPacket.class);
					
					if(is_u1) {
						this.packets[0]=packet;
					}else if(is_u2) {
						this.packets[1]=packet;
					}
					
					User.broadcast(ev.getPacket(), State.INGAME, Arrays.asList(ev.getUser()));
				}else if(ev.getPacket() instanceof PingPongGoalPacket) {
					PingPongGoalPacket packet = ev.getPacket(PingPongGoalPacket.class);
					
					if(ev.getUser().equalsUUID(packet.getUuid())) {
						User.broadcast(packet, State.INGAME);
						this.start = System.currentTimeMillis() + TimeSpan.SECOND * 5;
						User.broadcast(new PingPongResetPacket(this.start), State.INGAME);
						
						int old_score = 0;
						if(is_u1) {
							old_score = this.user1_score;
							this.user1_score = packet.getScore();

							Main.debug("Score update from "+ev.getUser().getName()+" for "+User.getUser(packet.getUuid()).getName()+" old score "+old_score+" => new score "+packet.getScore());
							if(this.user1_score >= WIN_SCORE) {
								end();
							}
						}else if(is_u2) {
							old_score = this.user2_score;
							this.user2_score = packet.getScore();
							Main.debug("Score update from "+ev.getUser().getName()+" for "+User.getUser(packet.getUuid()).getName()+" old score "+old_score+" => new score "+packet.getScore());
							if(this.user2_score >= WIN_SCORE) {
								end();
							}
						}
					}
				}
			}
			
			if(ev.getPacket() instanceof GameStartAckPacket) {
				print("§aSEND JOIN PACKETS TO "+ev.getUser().getName());
				
				ev.getUser().write(new PingPongGoalPacket(this.getUser1().getUuid(), this.user1_score));
				ev.getUser().write(new PingPongGoalPacket(this.getUser2().getUuid(), this.user2_score));
				if(this.start != -1)ev.getUser().write(new PingPongResetPacket(this.start));
				for(int i = 0; i < this.packets.length; i++)
					if(this.packets[i]!=null)
						ev.getUser().write(this.packets[i]);
				
				int index = getIndex(ev.getUser());
				if(index!=-1) {
					this.ready[getIndex(ev.getUser())] = true;
					allReady();
				}
				print("§a -- END --");
			}
		}
	}
}
