package net.kingingo.server.games.HigherLower;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.games.Game;
import net.kingingo.server.packets.client.games.GameEndPacket;
import net.kingingo.server.packets.client.games.GameStartAckPacket;
import net.kingingo.server.packets.client.higherlower.HigherLowerSearchChoosePacket;
import net.kingingo.server.packets.server.higherlower.HigherLowerAnsweredPacket;
import net.kingingo.server.packets.server.higherlower.HigherLowerSearchPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Callback;
import net.kingingo.server.utils.Utils;

public class HigherLower extends Game{

	private ArrayList<Search> searchs = new ArrayList<>();
	private Search[] search;
	private ArrayList<HigherLowerAnsweredPacket[]> answers = new ArrayList<>();
	
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
				
				HigherLowerAnsweredPacket[] answers = this.answers.size() > packet.leftIndex/2 ? this.answers.get(packet.leftIndex/2) : null;
				if(answers==null) {
					answers = new HigherLowerAnsweredPacket[2]; 
					this.answers.add(answers);
				}
				
				answers[ (ev.getUser() == getUser1() ? 0 : 1) ] = new HigherLowerAnsweredPacket(ev.getUser().getUuid(), packet.leftIndex, win, packet.higher);
				print("HigherLowerSearchChoosePacket:");
				print("	GOT FROM user:"+ev.getUser().getName());
				print("	win:"+win);
				print("		higher:"+packet.higher+" "+this.search[packet.leftIndex].amount+"<"+this.search[packet.rightIndex].amount);
				print("		leftIndex:"+packet.leftIndex);
				print("		rightIndex:"+packet.rightIndex);
				print("		left-amount:"+this.search[packet.leftIndex].amount);
				print("		right-amount:"+this.search[packet.rightIndex].amount);
				this.answers.set(packet.leftIndex/2, answers);
				
				if(win) {
					if(ev.getUser() == getUser1()) {
						this.user1_score++;
					}else if(ev.getUser() == getUser2()){
						this.user2_score++;
					}
					print(ev.getUser().getName()+" +1");
				} else {
					print(ev.getUser().getName()+" lost");
				}
				
				if(this.answers.get(packet.leftIndex/2)[0] != null)
					Stage.broadcast(this.answers.get(packet.leftIndex/2)[0]);
				if(this.answers.get(packet.leftIndex/2)[1] != null)
					Stage.broadcast(this.answers.get(packet.leftIndex/2)[1]);
			}else if(ev.getPacket() instanceof GameStartAckPacket) {
				try {
					HigherLowerSearchPacket packet = new HigherLowerSearchPacket(this.search);
					ev.getUser().write(packet);
					
					for(int i = 0; i < this.answers.size(); i++) {
						if(this.answers.get(i)[0]!=null)
							ev.getUser().write(this.answers.get(i)[0]);
						if(this.answers.get(i)[1]!=null)
							ev.getUser().write(this.answers.get(i)[1]);
					}
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
		@SuppressWarnings("unchecked") ArrayList<Search> clone = (ArrayList<Search>) this.searchs.clone();
		
		for(int i = 0; i < size; i++) {
			search[i] = clone.get(Utils.randInt(0, clone.size()-1));
			clone.remove(search[i]);
		}
		
		return search;
	}
	
	public void start(User u1, User u2) {
		super.start(u1, u2);
		this.answers.clear();
		this.search = randSearch(6);
		
		for(int i = 0; i < this.search.length; i+=2) {
			print("\""+search[i].request + "\"("+search[i].amount+") and \"" + search[i+1].request+"\"("+search[i+1].amount+")");
		}
	}
}
