package net.kingingo.server.terminal;

import com.sun.tools.javac.launcher.Main;

import net.kingingo.server.games.Game;
import net.kingingo.server.stage.Stage;

public class Debugger {

	public static String getLastCallerClass(){
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (StackTraceElement currunt : stack) {
			if (!currunt.getClassName().equalsIgnoreCase(Debugger.class.getName()) 
					&& !currunt.getClassName().equalsIgnoreCase(Game.class.getName())
					&& !currunt.getClassName().equalsIgnoreCase(Stage.class.getName())
					&& !currunt.getClassName().equalsIgnoreCase("java.lang.Thread") 
					&& !currunt.getClassName().equalsIgnoreCase("net.kingingo.server.Main")
					&& !currunt.getClassName().contains("net.kingingo.server.terminal.")) {
				
				try {
					return Class.forName(currunt.getClassName()).getSimpleName() + ":" + currunt.getLineNumber();
				} catch (ClassNotFoundException e) {
					return currunt.getClassName() + ":" + currunt.getLineNumber();
				}
			}
		}
		return "unknown:-1";
	}
}