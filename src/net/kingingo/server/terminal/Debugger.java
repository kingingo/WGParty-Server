package net.kingingo.server.terminal;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.sun.tools.javac.launcher.Main;

import net.kingingo.server.games.Game;
import net.kingingo.server.stage.Stage;

public class Debugger {
	private static final SimpleDateFormat DATE_FORMAT_NOW = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

	public static String date() {
		return DATE_FORMAT_NOW.format(Calendar.getInstance().getTime());
	}

	public static String getLastCallerClass(){
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (StackTraceElement current : stack) {
			if (!current.getClassName().equalsIgnoreCase(Debugger.class.getName()) 
					&& !current.getClassName().equalsIgnoreCase(Game.class.getName())
					&& !current.getClassName().equalsIgnoreCase(Stage.class.getName())
					&& !current.getClassName().equalsIgnoreCase("java.lang.Thread") 
					&& !current.getClassName().equalsIgnoreCase("net.kingingo.server.Main")
					&& !current.getClassName().contains("net.kingingo.server.terminal.")) {
				
				try {
					return "§7["+date()+"|"+ Class.forName(current.getClassName()).getSimpleName() + ":" + current.getLineNumber()+"]";
				} catch (ClassNotFoundException e) {
					return "§7["+date()+"|"+ current.getClass().getSimpleName() + ":" + current.getLineNumber()+"]";
				}
			}
		}
		return "§7["+date()+"| unknown:-1]";
	}
}