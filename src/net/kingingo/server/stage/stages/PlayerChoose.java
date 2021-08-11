package net.kingingo.server.stage.stages;

import java.util.ArrayList;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.UserLoggedInEvent;
import net.kingingo.server.packets.server.StartMatchPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.TimeSpan;
import net.kingingo.server.utils.Utils;

public class PlayerChoose extends Stage{
	public static User getUser1() {
		return Stage.get(PlayerChoose.class).u1;
	}
	
	public static User getUser2() {
		return Stage.get(PlayerChoose.class).u2;
	}
	
	public static void restart(boolean u1_ready,boolean u2_ready) {
		PlayerChoose stage = Stage.get(PlayerChoose.class);
		if(!u1_ready) {
			stage.printf("Player1 "+stage.u1.getName()+" wasn't ready.");
			stage.users.remove(stage.u1);
			stage.u1 = null;
		}
		if(!u2_ready) {
			stage.printf("Player2 "+stage.u2.getName()+" wasn't ready.");
			stage.users.remove(stage.u2);
			stage.u2 = null;
		}

		start_u1 = stage.u1;
		start_u2 = stage.u2;
		
		Stage.jump(PlayerChoose.class);
	}
	
	public static User start_u1 = null;
	public static User start_u2 = null;
	
	public User u1;
	public User u2;
	public ArrayList<User> users;
	
	@EventHandler
	public void connect(UserLoggedInEvent ev) {
		if(!isActive())return;
		ev.getUser().write(new StartMatchPacket(this.u1,this.u2,users,(this.end_time - System.currentTimeMillis()) >= 5, 3));
	}
	
	public PlayerChoose() {
		super(TimeSpan.SECOND * 20);
	}
	
	public int running() {
		return Stage.NEXT_STAGE;
	}
	
	private User pickUser() throws NullPointerException{
		@SuppressWarnings("unchecked") ArrayList<User> list = (ArrayList<User>) this.users.clone();
		list.removeIf( u -> (u.isTester()));
		
		if(list.isEmpty())throw new ArrayIndexOutOfBoundsException("List ist empty?!");
		return list.get(Utils.randInt(0, list.size()-1));
	}
	
	public void start(User u1, User u2) {
		setCountdown("game starts in");
		
		User[] users = User.getAllStats().keySet().toArray(new User[0]);
		this.users = new ArrayList<User>();
		for(int i = 0; i < users.length; i++) {
			if(!users[i].isSpectate())
				this.users.add(users[i]);
		}
		
		try{
			this.u1 = (u1 == null ? pickUser() : u1);
			this.u2 = (u2 == null ? pickUser() : u2);
		
			StartMatchPacket start = new StartMatchPacket(this.u1,this.u2,this.users);
			broadcast(start);
			printf("Choose Player with "+this.u1.getName()+" "+this.u2.getName());
		}catch(ArrayIndexOutOfBoundsException e){
			Main.error("Not enough User for a round");
			Stage.jump(Countdown.class);
		}
	}
	
	@Override
	public void start() {
		super.start();
		start(PlayerChoose.start_u1,PlayerChoose.start_u2);
		PlayerChoose.start_u1=null;
		PlayerChoose.start_u2=null;
	}
}
