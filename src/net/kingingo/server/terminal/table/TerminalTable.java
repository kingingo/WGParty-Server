package net.kingingo.server.terminal.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kingingo.server.terminal.colors.ColoredChar;
import net.md_5.bungee.api.ChatColor;

@RequiredArgsConstructor
public class TerminalTable {
	public static interface RowSeperator {
		public static RowSeperator DEFAULT = new RowSeperator(){
			@Override
			public ColoredChar getSeperator(TerminalRow row, int rowIndex, int columnFrom, int columnTo) {
				return new ColoredChar('|');
			}
			@Override
			public ColoredChar getDefaultSeperator() {
				return new ColoredChar('|');
			}
			
		};
		
		ColoredChar getSeperator(TerminalRow row,int rowIndex,int columnFrom,int columnTo);
		ColoredChar getDefaultSeperator();
	}
	public static enum Align {
		LEFT,
		RIGHT,
		CENTER;
	}
	
	@AllArgsConstructor
	@Getter
	public static class TerminalColumn {
		private String name;
		private Align align;
	}
	
	@SuppressWarnings("unchecked")
	public static class TerminalRow {
		@Getter
		private List<String>[] columns;
		
		public TerminalRow(String...strings) {
			this(strings.length);
			for(int i = 0;i<strings.length;i++)
				setText(i, strings[i]);
		}
		
		public TerminalRow(int size) {
			columns = new List[size];
			for(int i = 0;i<size;i++)
				columns[i] = new ArrayList<>();
		}
		
		public void setText(int column,String message){
			columns[column].clear();
			for(String m : message.split("\n"))
				columns[column].add(m);
		}
		
		public void addText(int column,String message){
			columns[column].add(message);
		}
	}
	
	private final TerminalColumn[] column;
	private ArrayList<TerminalRow> rows = new ArrayList<>();
	@Getter
	@Setter
	private RowSeperator rowSeperator = RowSeperator.DEFAULT;
	
	public void addRow(String...strings){
		if(strings.length != column.length)
			throw new RuntimeException("Row size isnt equals to column size!");
		rows.add(new TerminalRow(strings));
	}
	
	public void addRow(TerminalRow row){
		if(row.columns.length != column.length)
			throw new RuntimeException("Row size isnt equals to column size!");
		rows.add(row);
	}
	
	public void printTable(){
		for(String s : buildLines())
			System.out.println(ChatColor.stripColor(s));
	}
	
	private static final String columnSeperator = "§r§7 %s §r";
	
	public List<String> buildLines(){
		ArrayList<String> lines = new ArrayList<>();
		
		int[] columnSize = new int[column.length];
		for(int column = 0;column < this.column.length; column++){
			columnSize[column] = getColumnMaxWith(column);	
		}
		
		/*
		int tableSize = 0;
		for(int i : columnSize)
			tableSize+=i+columnSeperator.length();
		tableSize-=columnSeperator.length();
		*/
		String line = "";
		
		String first = null;
		for(int index = 0;index < this.column.length; index++){
			String sep = String.format(columnSeperator, rowSeperator.getDefaultSeperator());
			if(first == null)
				first = sep;
			line += sep+formatString(column[index].name, column[index].align, columnSize[index]);
		}
		lines.add(line.substring(first.length()));
		
		first = null;
		line = "";
		for(int i : columnSize){
			String sep = String.format(columnSeperator, rowSeperator.getSeperator(null, -1, -1, -1));
			if(first == null)
				first = sep;
			line+=sep.replaceAll(" ", "-")+"§7"+StringUtils.leftPad("", i, '-');
		}
		lines.add(line.substring(first.length()));
		
		for(TerminalRow row : rows){
			line = "";
			int maxLines = 1;
			for(List<String> column : row.columns)
				if(column.size() > maxLines)
					maxLines = column.size();
			for(int i = 0;i<maxLines;i++){
				first = null;
				line = "";
				for(int index = 0;index < row.columns.length; index++){
					String sep = String.format(columnSeperator, rowSeperator.getSeperator(row, i, index-1, index));
					if(first == null)
						first = sep;
					line += sep+formatString(i < row.columns[index].size() ? row.columns[index].get(i) : "", column[index].align, columnSize[index]);
				}
				lines.add(line.substring(first.length()));
			}
		}
		return lines;
	}
	
	private String formatString(String in, Align align, int size){
		int absuluteSize = (in.length()-getStringLength(in))+size;
		switch (align) {
		case LEFT:
			return StringUtils.leftPad(in, absuluteSize);
		case RIGHT:
			return StringUtils.rightPad(in, absuluteSize);
		default:
			return StringUtils.center(in, absuluteSize);
		}
	}
	
	private int getColumnMaxWith(int column){
		int currentLength = 0;
		
		for(TerminalRow row : rows)
			for(String m : row.columns[column])
				if(getStringLength(m) > currentLength)
					currentLength = getStringLength(m);
		if(getStringLength(this.column[column].getName()) > currentLength)
			currentLength = getStringLength(this.column[column].getName());
		
		return currentLength;
	}
	
	private int getStringLength(String in){
		if(in == null)
			return 4;
		return ChatColor.stripColor(in).length();
	}
}
