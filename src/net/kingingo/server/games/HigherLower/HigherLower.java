package net.kingingo.server.games.HigherLower;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import net.kingingo.server.Main;
import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.event.events.StateChangeEvent;
import net.kingingo.server.games.Game;
import net.kingingo.server.packets.client.games.GameEndPacket;
import net.kingingo.server.packets.client.games.GameStartAckPacket;
import net.kingingo.server.packets.client.higherlower.HigherLowerSearchChoosePacket;
import net.kingingo.server.packets.server.higherlower.HigherLowerAnsweredPacket;
import net.kingingo.server.packets.server.higherlower.HigherLowerSearchPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.State;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Callback;
import net.kingingo.server.utils.Utils;

public class HigherLower extends Game{

	private ArrayList<Search> searchs = new ArrayList<>();
	
	private int user1_win = 0;
	private int user2_win = 0;
	
	private Search[] search;
	
	public HigherLower(Callback<User[]> endCallback) {
		super(endCallback);
		loadSearch();
	}
	
	public void loadSearch() {
		File file = new File("higherlower.txt");
		
		if(file.exists()) {
			try {
				DataInputStream in = new DataInputStream(new FileInputStream(file));
				
				while(in.available() > 0) {
					this.searchs.add(new Search(in));
				}
				print("loaded "+this.searchs.size()+" searches...");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else print("higherlower.txt is missing");
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(isActive()) {
			if(ev.getPacket() instanceof HigherLowerSearchChoosePacket) {
				HigherLowerSearchChoosePacket packet = ev.getPacket(HigherLowerSearchChoosePacket.class);
				
				boolean win = (packet.higher == this.search[packet.leftIndex].amount < this.search[packet.rightIndex].amount);
				
				if(win) {
					if(ev.getUser() == getUser1()) {
						this.user1_win++;
					}else if(ev.getUser() == getUser2()){
						this.user2_win++;
					}
					print(ev.getUser().getName()+" +1");
					
					Stage.broadcast(new HigherLowerAnsweredPacket(ev.getUser().getUuid(), true));
				} else {
					print(ev.getUser().getName()+" lost");
					Stage.broadcast(new HigherLowerAnsweredPacket(ev.getUser().getUuid(), false));
				}
			}else if(ev.getPacket() instanceof GameStartAckPacket) {
				try {
					HigherLowerSearchPacket packet = new HigherLowerSearchPacket(this.search);
					ev.getUser().write(packet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(ev.getPacket() instanceof GameEndPacket) {
				print("GameEndPacket -> SET USER1_DONE "+this.user1_done+" USER2_DONE "+this.user2_done);
				if(ev.getUser() == getUser1()) {
					this.user1_done=true;
				}else if(ev.getUser() == getUser2()){
					this.user2_done=true;
				}
				print("GameEndPacket -> SET USER1_DONE "+this.user1_done+" USER2_DONE "+this.user2_done);
				
				if(this.user1_done && this.user2_done) {
					end();
				}
			}
		}
	}
	
	public Search[] randSearch(int size) {
		Search[] search = new Search[size];
		ArrayList<Search> clone = (ArrayList<Search>) searchs.clone();
		
		for(int i = 0; i < size; i++) {
			search[i] = clone.get(Utils.randInt(0, clone.size()-1));
			clone.remove(search[i]);
		}
		
		return search;
	}
	
	public void start(User u1, User u2) {
		super.start(u1, u2);
		reset();
		this.search = randSearch(6);
		
		for(int i = 0; i < this.search.length; i+=2) {
			print("\""+search[i].request + "\"("+search[i].amount+") and \"" + search[i+1].request+"\"("+search[i+1].amount+")");
		}
	}

	public void reset() {
		this.user1_win=0;
		this.user2_win=0;
	}

	public void end() {
		User win = null;
		User lose = null;
		
		if(this.user1_win > this.user2_win) {
			win = this.getUser1();
			lose = this.getUser2();
		}else if(this.user1_win < this.user2_win){
			win = this.getUser2();
			lose = this.getUser1();
		}else{
			win = null;
			lose = null;
		}
		print("END -> win:"+win+" lose:"+lose);
		
		end(win,lose);
	}
	
	public void end(User win, User lose) {
		super.end(win, lose);
		reset();
	}

}
