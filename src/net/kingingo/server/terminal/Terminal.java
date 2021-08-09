package net.kingingo.server.terminal;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jline.console.ConsoleReader;
import jline.console.CursorBuffer;
import jline.internal.Ansi;

import org.fusesource.jansi.AnsiConsole;
import org.reflections.Reflections;

import net.kingingo.server.Main;
import net.kingingo.server.terminal.colors.ChatColor;

public class Terminal implements Runnable{
	private static Terminal instance;
	private static ArrayList<CommandExecutor> commands = new ArrayList<>();
	
	public static Terminal getInstance() {
		if(instance==null)instance=new Terminal();
		return instance;
	}
	
	public static ArrayList<CommandExecutor> getCommands(){
		return commands;
	}
	
	public static void register(CommandExecutor cmd) {
		if(!commands.contains(cmd)) {
			commands.add(cmd);
		}
	}
	
	private Thread thread;
	private ConsoleReader console;
	private boolean active = true;
	private String message;
	
	private Terminal() {
		AnsiConsole.systemInstall();
		System.setOut(new CostumSystemPrintStream());
		System.setErr(new CostumSystemPrintStream(){
			@Override
			public void write(String message) {
				Terminal.this.write("�c[ERROR] �6"+message);
			}
		});
		try {
			this.console = new ConsoleReader(System.in, AnsiConsole.out());
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.thread=new Thread(this);
		this.thread.setName("Terminal");
		this.thread.start();
	}
	
	@SuppressWarnings("deprecation")
	public void stop() {
		this.active=false;
		this.console.shutdown();
		this.thread.interrupt();
		AnsiConsole.systemUninstall();
	}
	
	public static void loadCommands() {
		Reflections reflections = new Reflections( "net.kingingo.server.terminal.commands" );
		List<Class<? extends CommandExecutor>> moduleClasses = new ArrayList<>( reflections.getSubTypesOf( CommandExecutor.class ) );

		Collections.sort(moduleClasses, new Comparator<Class<? extends CommandExecutor>>() {
		    @Override
		    public int compare(Class<? extends CommandExecutor> o1, Class<? extends CommandExecutor> o2) {
		        return o1.getSimpleName().compareTo(o2.getSimpleName());
		    }
		});
		
		for ( Class<? extends CommandExecutor> clazz : moduleClasses ){
			try {
				register(clazz.getDeclaredConstructor().newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		
		for(CommandExecutor cmd : commands)Main.printf(cmd.getCommand() + " registered");
	}
	
	protected synchronized void write(String message) {
		if(message == null || message.length() == 0)
			return;
		if(message.split("\n").length > 1){
			for(String s : message.split("\n"))
				write(s);
			return;
		}
		
		try {
			String promt = "";
			String input_message = "";
			int cursor = 0;
			if (!active) {
				promt = ChatColor.toAnsiFormat(this.message);
				cursor = promt.length();
			} else {
				input_message = console.getCursorBuffer().toString();
				promt = "\r" + getPromt();
				cursor = console.getCursorBuffer().cursor;
			}
			while (Ansi.stripAnsi(ChatColor.stripColor(message)).length() < input_message.length()+Ansi.stripAnsi(ChatColor.stripColor(promt)).length()) {
				message = message + " ";
			}
			AnsiConsole.out.println("\r"+ChatColor.toAnsiFormat(message));
			console.resetPromptLine(ChatColor.toAnsiFormat(promt), Ansi.stripAnsi(input_message), cursor);
		} catch (Exception e) {
		}
	}

	private String getPromt() {
		String prefix = "";
		prefix += "§a> §o";
		return prefix;
	}
	
	public void lock(String message) {
		active = false;
		if (message == null)
			message = "";
		try {
			console.killLine();
			this.message = ChatColor.toAnsiFormat(message);
			console.resetPromptLine(this.message, "", this.message.length());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void unlock() {
		active = true;
		try {
			console.killLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean onCommand(String line) {
		if(line.equalsIgnoreCase(""))return false;
		
		String[] splitted = line.split(" ");
		if(splitted.length > 0) {
			String command = splitted[0];
			String[] args = new String[splitted.length-1];
			
			for(int i = 1; i < splitted.length; i++)args[i-1]=splitted[i];
			
			for(CommandExecutor cmd : commands) {
				if(cmd.getCommand().equalsIgnoreCase(command) || cmd.isAlias(command)) {
					try { cmd.onCommand(args); }catch(Exception e) {e.printStackTrace();}
					return true;
				}
			}
			return false;
		}
		return false;
	}

	@Override
	public void run() {
		
		while(this.active) {
			try{
				if(console.getCursorBuffer() == null){}
				StringBuilder b = new StringBuilder();
				b.append(console.getCursorBuffer());
				b = null;
			}catch(Exception e){
				try{
					console.getCursorBuffer().buffer.delete(0, console.getCursorBuffer().buffer.length());
				}catch(Exception ex){
					try{
						CursorBuffer buffer = console.getCursorBuffer();
						buffer.getClass().getField("buffer").setAccessible(true);
						buffer.getClass().getField("buffer").set(buffer, new StringBuilder());
						buffer.cursor = 0;
					}catch(Exception exx){
						exx.printStackTrace();
					}
					write("§cHard buffer reset!");
				}
			}
			
			try {
				String line = console.readLine();
				
				if(!this.onCommand(line)) {
					Main.printf("Command not found "+line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
