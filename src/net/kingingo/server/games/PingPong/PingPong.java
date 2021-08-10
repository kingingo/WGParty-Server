package net.kingingo.server.games.PingPong;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.games.Game;
import net.kingingo.server.packets.client.games.GameStartAckPacket;
import net.kingingo.server.packets.server.pingpong2.PingPongBallPacket;
import net.kingingo.server.packets.server.pingpong2.PingPongGoalPacket;
import net.kingingo.server.packets.server.pingpong2.PingPongPlayerPacket;
import net.kingingo.server.packets.server.pingpong2.PingPongSettingsPacket;
import net.kingingo.server.packets.server.pingpong2.PingPongStartPacket;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Callback;
import net.kingingo.server.utils.TimeSpan;
/*
 * TO-DO
 * 
 * MOBILE:
 * 	PADDLE bewegt sich nicht: vermutlich wird die Y koordinate nicht richtig zu den clients §bertragen...
 *  nachrichten sind schon eingebaut -> ausf§hren und gucken 
 *  viel Spa§ FELIX :)
 * 
 * 
 */
public class PingPong extends Game implements Runnable{
	public static int WIN_SCORE = 3;
	private static final int UPDATES_PER_SECOND = 30;
	public static final int CANVAS_WIDTH = 500;
	public static final int CANVAS_HEIGHT = 300;
	
	public static final int PADDLE_WIDTH = 10;
	public static final int PADDLE_HEIGHT = 70;
	
	private Thread thread;
	private Ball ball;
	private Player[] players = new Player[2];
	private long start = -1;
	private long end = -1;
	
	public PingPong(Callback<User[]> endCallback) {
		super(endCallback);
		this.ball = new Ball();
		this.players[0] = new Player();
		this.players[1] = new Player();
	}
	
	public void end(User win, User lose,boolean force){
		super.end(win, lose, force);
		print("STOP PingPong!");
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(isActive()) {
			if(ev.getPacket() instanceof GameStartAckPacket) {
				//Send Settings packet for configuration
				ev.getUser().write(new PingPongSettingsPacket(this.start,this.players, this.ball));
				
				//Send Player information
				ev.getUser().write(new PingPongPlayerPacket(players[0]));
				ev.getUser().write(new PingPongPlayerPacket(players[1]));
				
				//Send Ball information
				ev.getUser().write(new PingPongBallPacket(this.ball));
			}else if(ev.getPacket() instanceof PingPongPlayerPacket) {
				PingPongPlayerPacket packet = ev.getPacket(PingPongPlayerPacket.class);
				Player player = this.players[packet.player.uid-1];
				
				if(!player.user.isMobile()) {
					player.upPressed = packet.player.upPressed;
					player.downPressed = packet.player.downPressed;
				}else {
					Main.printf("§e", "PINGPONG_PLAYER_PACKET", "MOBILE "+player.user.getName()+" newY="+packet.player.y);
					player.newY = packet.player.y;
				}
			}
		}
	}
	
	private void resetPaddles() {
		this.players[0].x = 10;
		this.players[0].y = CANVAS_HEIGHT / 2 - PADDLE_HEIGHT / 2;
		this.players[1].x = CANVAS_WIDTH - (PADDLE_WIDTH + 10);
		this.players[1].y = CANVAS_HEIGHT / 2 - PADDLE_HEIGHT / 2;
	}
	
	private void resetBall() {
		this.ball.vecX = -this.ball.vecX;
		this.ball.vecY = -this.ball.vecY;
		this.ball.speed = 7;
		this.ball.x = CANVAS_WIDTH / 2;
		this.ball.y = CANVAS_HEIGHT / 2;
	}
	
	private void endGame() {
		resetBallPos();
		this.end = System.currentTimeMillis() + TimeSpan.SECOND * 6;
		User.broadcast(new PingPongStartPacket(-2), State.INGAME);
		sendBall();
	}
	
	private void increaseScore(Player player) {
		//Check which User get Score
		if(player.uid==1)
			this.user1_score++;
		else 
			this.user2_score++;
		
		//Send Packet to Clients
		PingPongGoalPacket goal = new PingPongGoalPacket(player.uid, player.uid==1 ? this.user1_score : this.user2_score);
	
		User.broadcast(goal, State.INGAME);
		
		if(this.user1_score >= WIN_SCORE || this.user2_score >= WIN_SCORE) {
			endGame();
		} else {
			waitBall();
		}
	}
	
	private boolean is_starting() {
		return this.start == -1 || this.start > System.currentTimeMillis();
	}
	
	private void resetBallPos() {
		this.ball.x = CANVAS_WIDTH / 2;
		this.ball.y = CANVAS_HEIGHT / 2;
	}
	
	private void waitBall() {
		//Reset ball position
		this.ball.speed = 7;
		resetBallPos();
		this.start = System.currentTimeMillis() + TimeSpan.SECOND * 3;
		
		User.broadcast(new PingPongStartPacket(this.start), State.INGAME);
		sendBall();
	}
	
	private void updatePaddle(Player player) {
		if(player.user.isMobile()) {
			if(player.newY != -1) {
				Main.printf("§e", "updatePaddle", "MOBILE "+player.user.getName()+" Y="+player.y+" to "+player.newY);
				player.y = player.newY;
				player.newY=-1;
			}
		}else {
			if(player.upPressed && player.y > 0) {
				player.y -= 8;
			}else if(player.downPressed && (player.y < CANVAS_HEIGHT - PADDLE_HEIGHT)) {
				player.y += 8;
			}
		}
	}

	public void run() {
		//Go on until Games stops
		while(this.isActive()) {
			if(this.end != -1) {
				if(this.end <= System.currentTimeMillis()) {
					end();
				}
			} else {
				//Update ball
				updateBall();
			}
			
			try {
				Thread.sleep(TimeSpan.SECOND / UPDATES_PER_SECOND);
			} catch (InterruptedException e) {
				print("PingPing updateThread was interupted");
			}
		}
	}
	
	private void sendPlayers(){
		//Update Player Paddles
		for(Player player : this.players)
			User.broadcast(new PingPongPlayerPacket(player), State.INGAME);
	}	
	
	private void sendBall() {
		//Send ball to clients/players
		User.broadcast(new PingPongBallPacket(this.ball), State.INGAME);
	}
	
	private boolean hitTop() {
		return (this.ball.y - this.ball.radius) <= 0;
	}
	
	private boolean hitBottom() {
		return (this.ball.y + this.ball.radius) >= CANVAS_HEIGHT;
	}
	
	private void updateBall() {
		//Game is not running
		if(this.is_starting()) return;
		
		for(Player player : this.players)
			updatePaddle(player);
		sendPlayers();
		
		//Check ball hits top or bottom wall
		
		if(hitTop() || hitBottom()) {
			this.ball.vecY = -this.ball.vecY;
		}
		
		//Hit right Wall
		if((this.ball.x + this.ball.radius) >= CANVAS_WIDTH) {
			//User1 score +1
			increaseScore(this.players[0]);
			return;
		}
		
		//Hit left Wall
		if((this.ball.x - this.ball.radius) <= 0) {
			//User2 score +1
			increaseScore(this.players[1]);
			return;
		}
		
		//Move ball
		this.ball.x += this.ball.vecX;
		this.ball.y += this.ball.vecY;
		sendBall();
		
		boolean is_player_1 = this.ball.x < CANVAS_WIDTH/2;
		Player player = is_player_1 ? players[0] : players[1];
		//Player hit ball
		if(collisionDetect(player)) {
			double angle = 0;
			
			// if ball hit top of the paddle
			if(this.ball.y < (player.y + PADDLE_HEIGHT / 2)) {
				angle = -1 * Math.PI / 4;
			}else if(this.ball.y > (player.y + PADDLE_HEIGHT/2)) {
				//if the bottom
				angle = Math.PI / 4;
			}
			
			/* change velocity of this.ball according to on which paddle the this.ball hitted */
		    this.ball.vecX = (is_player_1 ? 1 : -1) * this.ball.speed * Math.cos(angle);
			this.ball.vecY = this.ball.speed * Math.sin(angle);
			
			//increase speed
			this.ball.speed += 0.2;
		}
	}
	
	/**
	 * Detect whether the ball was hitted by the player
	 * @param player
	 * @return
	 */
	private boolean collisionDetect(Player player) {
		int top = player.y;
		int right = player.x + PADDLE_WIDTH;
		int bottom = player.y + PADDLE_HEIGHT;
		int left = player.x;
		
		int ball_top = ball.y - ball.radius;
		int ball_right = ball.x + ball.radius;
		int ball_bottom = ball.y + ball.radius;
		int ball_left = ball.x - ball.radius;
		
		return ball_left < right && ball_top < bottom && ball_right > left && ball_bottom > top ;
	}
	
	private void setPlayers(User u1, User u2) {
		this.players[0].uid = 1;
		this.players[0].user = u1;
		this.players[0].upPressed = false;
		this.players[0].downPressed = false;

		this.players[1].uid = 2;
		this.players[1].user = u2;
		this.players[1].upPressed = false;
		this.players[1].downPressed = false;
		this.resetPaddles();
	}
	
	public void start(User u1, User u2) {
		setPlayers(u1,u2);
		resetBall();
		//Start Time
		this.start = System.currentTimeMillis() + TimeSpan.SECOND * 8;
		this.end = -1;
		print("Start PingPong GAME!!");
		
		super.start(u1, u2);
		
		//Thread is not stopped?!
		if(this.thread != null) {
			//Interupt and set null
			this.thread.interrupt();
			this.thread = null;
		}
		
		this.thread = new Thread(this);
		this.thread.start();
	}
	
	public void resend(User user) {
		super.resend(user);
	}
	
	public static class Player{
		public int newY = -1;
		
		public boolean upPressed;
		public boolean downPressed;
		public int uid;
		public int x;
		public int y;
		public User user;
		
		public static Player parseFromInput(DataInputStream in) throws IOException {
			Player player = new Player();
			player.downPressed = in.readBoolean();
			player.upPressed = in.readBoolean();
			player.y = in.readInt();
			player.uid = in.readInt();
			return player;
		}

		public void writeToOutput(DataOutputStream out, boolean onlyPosition) throws IOException {
			if(!onlyPosition) {
				out.writeBoolean( this.downPressed );
				out.writeBoolean( this.upPressed );
			}
			
			out.writeInt( this.x );
			out.writeInt( this.y );
			out.writeInt( this.uid );
		}
	}

	public class Ball{
		public int x;
		public int y;
		public double speed;
		public int radius = 7;
		public double vecX = 5;
		public double vecY = 5;
		
		public void writeToOutput(DataOutputStream out, boolean onlyPosition) throws IOException {
			out.writeInt(this.x);
			out.writeInt(this.y);
			if(!onlyPosition) {
				out.writeInt(this.radius);
			}
		}
	}
}
