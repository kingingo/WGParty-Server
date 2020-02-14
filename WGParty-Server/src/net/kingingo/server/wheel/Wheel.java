package net.kingingo.server.wheel;

import java.util.ArrayList;

import net.kingingo.server.utils.Utils;

public class Wheel {
	private static Wheel wheel;
	
	public static Wheel getInstance() {
		if(wheel==null)wheel=new Wheel();
		return wheel;
	}	
	
	private ArrayList<Alk> alk = new ArrayList<Alk>();
	
	private Wheel() {
		alk.add(new Alk("Vodka",1,"vodka.png"));
		alk.add(new Alk("Jägermeister",1,"jaegermeister.png"));
		alk.add(new Alk("Tequila",1,"tequila.png"));
		alk.add(new Alk("Whiskey",1,"whiskey.png"));
		alk.add(new Alk("Berliner Luft",1,"berliner_luft.png"));
		alk.add(new Alk("Bergman Bier",1,"bergmann.png"));
		alk.add(new Alk("Astra Rakete",1,"astra.png"));
		alk.add(new Alk("Gin",1,"gin.png"));
		alk.add(new Alk("Rum",1,"rum.png"));
		alk.add(new Alk("Glühwein",1,"gluehwein.png"));
	}
	
	public Alk randomAlk() {
		return alk.get(Utils.randInt(0, alk.size()-1));
	}
}
