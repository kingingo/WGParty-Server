package net.kingingo.server.terminal.colors;

import net.md_5.bungee.api.ChatColor;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ColoredString {
	private ColoredChar[] characters = new ColoredChar[0];
	private int index = 0;

	public ColoredString() {
	}

	public ColoredString(String message) {
		add(message);
	}

	public void add(String message) {
		List<ColoredChar> chars = new ArrayList<>();
		ColoredChar last = characters.length == 0 ? new ColoredChar('A') : characters[characters.length-1];
		char[] c = message.toCharArray();
		for(int index = 0; index < c.length;index++){
			if(c[index] == '§'){
				char colorcode = c[++index];
				ChatColor color = ChatColor.getByChar(colorcode);
				if(color == null)
					color = ChatColor.RESET;
				last.applyChatColor(color);
				continue;
			}
			chars.add(last.copyStyle(c[index]));
		}
		enschureSpace(chars.size());
		for (ColoredChar ch : chars)
			characters[index++] = ch;
	}
	
	public void add(ColoredString message) {
		enschureSpace(message.getSize());
		for(ColoredChar chars : message.characters)
			characters[index++] = chars;
	}

	public void add(ColoredChar character) {
		enschureSpace(1);
		characters[index++] = character;
	}

	public void set(int index, ColoredChar character) {
		characters[index] = character;
	}
	
	public ColoredChar getChar(int index){
		return characters[index];
	}
	
	public int getSize(){
		return characters.length;
	}

	private void enschureSpace(int size) {
		if (characters.length <= index + size) {
			ColoredChar[] oldCharacters = characters;
			characters = (ColoredChar[]) Array.newInstance(ColoredChar.class, index + size);
			System.arraycopy(oldCharacters, 0, characters, 0, oldCharacters.length);
		}
	}
	
	public ColoredString substring(int start,int end){
		ColoredChar[] chars = new ColoredChar[end-start];
		System.arraycopy(characters, start, chars, 0, chars.length);
		ColoredString string = new ColoredString();
		string.characters = chars;
		string.index = chars.length;
		return string;
	}
	
	@Override
	public String toString() {
		return toString(true);
	}
	
	public String toString(boolean colored) {
		StringBuilder out = new StringBuilder();
		for(ColoredChar characters : this.characters){
			if(!colored)
				out.append(characters.getCharacter());
			else
				out.append(characters.toString());
		}
		return out.toString()+(colored ? "§r" : "");
	}
	@Override
	public ColoredString clone(){
		ColoredString out = new ColoredString();
		out.characters = characters.clone();
		out.index = index;
		return out;
	}
}
