package net.kingingo.server.games.HigherLower;

import java.io.File;
import java.util.ArrayList;

import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.games.Game;
import net.kingingo.server.packets.client.games.GameStartAckPacket;
import net.kingingo.server.packets.client.higherlower.HigherLowerSearchChoosePacket;
import net.kingingo.server.packets.server.higherlower.HigherLowerSearchPacket;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Utils;

public class HigherLower extends Game{
	public static final String IMG_PATH = Game.IMG_PATH + File.pathSeparator + "higherlower"+ File.pathSeparator+"images"+File.pathSeparator;

	private ArrayList<Search> searchs = new ArrayList<>();
	private int user1_win = 0;
	private int user2_win = 0;
	private Search[] search;
	
	public HigherLower() {
		super();
		
		this.searchs.add(new Search("Spa", IMG_PATH + "search"+File.pathSeparator+"spa.jpg", 1830000));
		this.searchs.add(new Search("Obesity", IMG_PATH + "search"+File.pathSeparator+"obesity.jpg", 201000));
		this.searchs.add(new Search("The Home Depot", IMG_PATH + "search"+File.pathSeparator+"hdt.jpg",24900000));
		this.searchs.add(new Search("Netflix", IMG_PATH + "search"+File.pathSeparator+"netflix.jpg",83100000));
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(isActive()) {
			if(ev.getPacket() instanceof HigherLowerSearchChoosePacket) {
				HigherLowerSearchChoosePacket packet = ev.getPacket(HigherLowerSearchChoosePacket.class);
				
				boolean win = packet.higher;
				
				if(win) {
					if(ev.getUser() == getUser1()) {
						this.user1_win++;
					}else if(ev.getUser() == getUser2()){
						this.user2_win++;
					}
				}
			}else if(ev.getPacket() instanceof GameStartAckPacket) {
				try {
					HigherLowerSearchPacket packet = new HigherLowerSearchPacket(this.search);
					ev.getUser().write(packet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public Search[] randSearch() {
		Search[] search = new Search[2];
		
		search[0] = this.searchs.get(Utils.randInt(0, this.searchs.size()-1));
		ArrayList<Search> clone = (ArrayList<Search>) searchs.clone();
		clone.remove(search[0]);
		search[1] = clone.get(Utils.randInt(0, clone.size()-1));
		
		return search;
	}
	
	public void start(User u1, User u2) {
		super.start(u1, u2);
		reset();
		this.search = randSearch();
		
		print("\""+search[0].request + "\"("+search[0].amount+") and \"" + search[1].request+"\"("+search[1].amount+")");
	}

	public void reset() {
		this.user1_win=0;
		this.user2_win=0;
	}
	
	public void end() {
		reset();
	}

}
