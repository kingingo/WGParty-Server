package net.kingingo.server.music;

public class MusicHandler {

	private static MusicHandler instance;
	
	public static MusicHandler instance() {
		if(instance==null)instance=new MusicHandler();
		return instance;
	}
	
	private MusicHandler() {
		
	}
}
