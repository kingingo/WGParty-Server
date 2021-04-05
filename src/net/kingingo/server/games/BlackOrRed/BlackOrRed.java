package net.kingingo.server.games.BlackOrRed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import net.kingingo.server.event.EventHandler;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.games.Game;
import net.kingingo.server.packets.client.BlackORRed.BlackOrRedCardsPacket;
import net.kingingo.server.packets.client.games.GameStartAckPacket;
import net.kingingo.server.packets.server.BlackOrRed.UserChooseColorPacket;
import net.kingingo.server.packets.server.BlackOrRed.UserChooseColorPacket.Color;
import net.kingingo.server.stage.Stage;
import net.kingingo.server.user.User;
import net.kingingo.server.utils.Callback;

public class BlackOrRed extends Game{
	
	private ArrayList<String> deck = new ArrayList<>();
	private String[] cards;
	private int[] user1_choose;
	private int[] user2_choose;
	
	public BlackOrRed(Callback<User[]> endCallback) {
		super(endCallback);
		loadDeck();
	}
	
	private void loadDeck() {
		this.deck.clear();
		String[] types = new String[] {"karo","pik","herz","kreuz"};
		
		for(String type : types) 
			for(int i = 1; i <= 13; i++) 
				this.deck.add(type+"/"+i+".png");
	}
	
	public void resend(User user) {
		super.resend(user);
	}
	
	public String[] randCards(int amount) {
		String[] cards = new String[amount];
		Collections.shuffle(this.deck);
		
		for(int i = 0; i < amount; i++) {
			cards[i] = this.deck.get(i);
		}
		return cards;
	}
	
	public void start(User u1, User u2) {
		super.start(u1, u2);
		this.cards = this.randCards(5);
		this.user1_choose=new int[3];
		this.user2_choose=new int[3];
		
		for(int i = 0; i < this.cards.length; i++) {
			print("i:"+i+" "+this.cards[i]);
		}
	}
	
	private Color getCardColor(int index) {
		return this.cards[index].contains("herz") || this.cards[index].contains("karo") ? Color.RED : Color.BLACK;
	}
	
	private boolean isDone() {
		for(int c : user1_choose)
			if(c==0)return false;

		for(int c : user2_choose)
			if(c==0)return false;
		return true;
	}
	
	@EventHandler
	public void rec(PacketReceiveEvent ev) {
		if(isActive()) {
			if(ev.getPacket() instanceof GameStartAckPacket) {
				try {
					BlackOrRedCardsPacket packet = new BlackOrRedCardsPacket(this.cards,3);
					ev.getUser().write(packet);
					
					for(int i = 0;i < this.user1_choose.length; i++) {
						if(this.user1_choose[i]!=0)
							ev.getUser().write(new UserChooseColorPacket(this.getUser1().getUuid(), i, Color.byId(this.user1_choose[i])));
					}
					
					for(int i = 0;i < this.user2_choose.length; i++) {
						if(this.user2_choose[i]!=0)
							ev.getUser().write(new UserChooseColorPacket(this.getUser2().getUuid(), i, Color.byId(this.user2_choose[i])));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(ev.getPacket() instanceof UserChooseColorPacket) {
				boolean is_1 = ev.getUser().equals(getUser1());
				boolean is_2 = ev.getUser().equals(getUser2());
				
				if(is_1 || is_2) {
					UserChooseColorPacket packet = ev.getPacket(UserChooseColorPacket.class);
					Stage.broadcast(packet, Arrays.asList(ev.getUser()));
					int[] choose = null;
					
					if(is_1) {
						choose = this.user1_choose;
					}else if(is_2) {
						choose = this.user2_choose;
					}
					
					choose[packet.getDeck_card()]=packet.getColor().getId();
					
					if(choose[packet.getDeck_card()] == getCardColor(0).getId()) {
						if(is_1) {
							this.user1_score+=1;
							print(ev.getUser().getName()+" "+packet.getDeck_card()+":"+this.user1_score);
						}else if(is_2) {
							this.user2_score+=1;
							print(ev.getUser().getName()+" "+packet.getDeck_card()+":"+this.user2_score);
						}
					}
					
					if(isDone()) {
						end();
					}
				}
			}
		}
	}
}
