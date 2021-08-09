package net.kingingo.server.terminal.graph;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.NonFinal;
import net.kingingo.server.terminal.colors.ColoredChar;
import net.kingingo.server.terminal.colors.ColoredString;
import net.md_5.bungee.api.ChatColor;
@SuppressWarnings("deprecated")
public class TerminalGraph {
	@AllArgsConstructor
	@Getter
	@ToString
	public static class Point {
		private double x;
		private double y;
	}
	
	private static class PointHigher extends Point{
		public PointHigher(double x) {
			super(x, -2);
		}
	}
	
	private static class PointLower extends Point{
		public PointLower(double x) {
			super(x, -3);
		}
	}
	
	//SplineInterpolator
	public static class Graph {
		private static final SplineInterpolator SMMOTH_LINE = new SplineInterpolator();
		private TreeSet<Point> points = new TreeSet<>(new Comparator<Point>() {
			@Override
			public int compare(Point o1, Point o2) {
				double x1 = o1.x;
				double x2 = o2.x;
				if(x1 == x2){
					if(o1 instanceof PointHigher && o2 instanceof PointHigher){
						System.err.println("Point higher is added to the list!");
						return 0;
					}
					if(o1 instanceof PointLower && o2 instanceof PointLower){
						System.err.println("Point lower is added to the list!");
						return 0;
					}
					return Boolean.compare(o1 instanceof PointHigher || (o2 instanceof PointLower), o2 instanceof PointHigher || (o1 instanceof PointLower));
				}
				return Double.compare(x1, x2);
			}
		});
		
		@Getter
		@Setter
		private ColoredChar character = new ColoredChar('#');
		
		@Getter
		@Setter
		private boolean fillBelowUp = true;
		
		@Getter
		@Setter
		private boolean returnZeroByNoData = false;
		
		public void addPoint(@NonFinal Point point){
			Point old = getPointExact(point.x);
			if(old != null)
				points.remove(old);
			points.add(point);
		}
		
		public void removePoint(double x){
			Point old = getPointExact(x);
			if(old != null)
				points.remove(old);
		}
		
		public Point getPoint(double x,boolean smoth){
			if(points.size() == 0)
				return new Point(x, 0); //All zero
			if(points.size() == 1)
				return new Point(x, points.first().y); //Linear
			if(smoth){
				double rangeMaxX = points.last().x;
				double rangeMinX = points.first().x;
				
				Point extraPoint = null;
				if(x > rangeMaxX || rangeMinX > x){
					if(returnZeroByNoData)
						return new Point(x, 0);
					extraPoint = getPoint(x, false);
					System.out.println("Addning extra point: "+extraPoint);
				}
				
				double[] xarray = new double[points.size()+(extraPoint != null ? 1 : 0)];
				double[] yarray = new double[points.size()+(extraPoint != null ? 1 : 0)];
				Iterator<Point> points = this.points.iterator();
				int index = 0;
				
				if(extraPoint != null && rangeMinX > x ){
					xarray[index] = extraPoint.getX();
					yarray[index] = extraPoint.getY();
					index++;
				}
				
				while (points.hasNext()) {
					Point point = points.next();
					xarray[index] = point.getX();
					yarray[index] = point.getY();
					index++;
				}
				if(extraPoint != null && x > rangeMaxX ){
					xarray[index] = extraPoint.getX();
					yarray[index] = extraPoint.getY();
				}
				return new Point(x, SMMOTH_LINE.interpolate(xarray, yarray).value(x));
			}
				//throw new RuntimeException(); //TODO
			Point last = points.floor(new Point(x, -1));
			
			Point next = points.ceiling(new Point(x, -1));
			if(last == null){
				if(returnZeroByNoData)
					return new Point(x, 0);
				last = next;
				next = points.ceiling(new PointHigher(next.x));
			}
			if(next == null){
				if(returnZeroByNoData)
					return new Point(x, 0);
				next = last;
				last = points.floor(new PointLower(last.x));
			}
			if(last.x == next.x)
				return new Point(x, last.y);
			BigDecimal pitch = (new BigDecimal(next.y).subtract(new BigDecimal(last.y))).divide((new BigDecimal(next.x).subtract(new BigDecimal(last.x))), 17, BigDecimal.ROUND_CEILING);
			Point out = new Point(x, new BigDecimal(last.y).add(pitch.multiply(new BigDecimal(x).subtract(new BigDecimal(last.x)))).doubleValue());
			return out;
		}
		
		public Point getPointExact(double x){
			for(Point point : points)
				if(point.x == x)
					return point;
			return null;
		}
	}
	
	private List<Graph> graths = new ArrayList<>();
	@Getter
	@Setter
	private int startX = 0;
	@Getter
	@Setter
	private int endX = 20;
	@Getter
	@Setter
	private int startY = 0;
	@Getter
	@Setter
	private int endY = 20;
	@Getter
	@Setter
	private double stepX = 3;
	
	@Getter
	@Setter
	private ColoredString xAxisName = new ColoredString("X-Achse");
	@Getter
	@Setter
	private ColoredString yAxisName = new ColoredString("Y-Achse");
	
	public void addGraph(Graph g){
		graths.add(g);
	}
	
	
	public ColoredString[] buildLines(int sizeX,int sizeY,boolean smooth){
		if(startX == 0 || startY>=endY)
			throw new RuntimeException("Invalid parameters!");
		ColoredString[] lines = new ColoredString[sizeY];
		for (int i = 0; i < lines.length; i++) {
			lines[i] = new ColoredString();
		}
		int zeroSpace = buildYRow(lines, sizeY);
		buildGraph(zeroSpace ,lines, sizeX-zeroSpace-1 ,smooth);
		ColoredString[] xrow = new ColoredString[3];
		buildXRow(new ColoredString(StringUtils.center("", zeroSpace)),xrow, sizeX-zeroSpace-1);
		return ArrayUtils.addAll(lines, xrow);
	}
	
	private void buildGraph(int startIndex, ColoredString[] lines,int rowFill,boolean smooth){
		for(int i = 0;i<lines.length;i++)
			while (lines[i].getSize() < startIndex+rowFill) {
				lines[i].add(" ");
			}
		BigDecimal deltaPerLine = new BigDecimal(endY-startY).divide(new BigDecimal(lines.length), 2, BigDecimal.ROUND_CEILING);
		BigDecimal stepsPerColumn = new BigDecimal(endX-startX).divide(new BigDecimal(rowFill), 2, BigDecimal.ROUND_CEILING);
		BigDecimal count = new BigDecimal(startX);
		for(int i = 1;i<rowFill;i++){
			for(Graph graph : graths){
				double y = graph.getPoint(count.doubleValue(), smooth).y; //TODO
				if(graph.fillBelowUp){
					if(y >= startY){
						for(int line = 0;line<lines.length;line++){
							if(y > (new BigDecimal(startY).add(deltaPerLine.multiply(new BigDecimal(line)))).doubleValue()){
								lines[lines.length-line-1].set(startIndex+i, graph.getCharacter());
							}
							//else
							//lines[lines.length-line-1] = replaceChar(lines[lines.length-line-1], startIndex+i, ' ');
						}
					} //TODO dont draw
				}
				else
				{
					if(y >= startY && y <= endY){
						for(int line = 0;line<lines.length;line++){
							if(y > (new BigDecimal(startY).add(deltaPerLine.multiply(new BigDecimal(line)))).doubleValue() && (new BigDecimal(startY).add(deltaPerLine.multiply(new BigDecimal(line+1)))).doubleValue() > y){
								lines[lines.length-line-1].set(startIndex+i, graph.getCharacter());
								break;
							}
						}
					}
				}
			}
			count=count.add(stepsPerColumn);
		}
	}
	
	private int buildYRow(ColoredString[] lines, int sizeY){
		BigDecimal deltaPerLine = new BigDecimal(endY-startY).divide(new BigDecimal(sizeY), 2, BigDecimal.ROUND_CEILING);
		int numberBounds = Math.max(new BigDecimal(startY).add(deltaPerLine.multiply(new BigDecimal(sizeY))).toString().length(), yAxisName.getSize()+1);
		for(int i = 0;i<sizeY;i++){
			if(i == 0){
				lines[i].add(" "+StringUtils.leftPad(yAxisName.toString(), numberBounds)+"  ");
				continue;
			}
			lines[i].add(" "+StringUtils.leftPad(new BigDecimal(startY).add(deltaPerLine.multiply(new BigDecimal(sizeY-i))).toString(), numberBounds)+" |");
		}
		return numberBounds+2;
	}
	
	private void buildXRow(ColoredString prefix, ColoredString[] lineBuffer, int sizeX){
		for (int i = 0; i < lineBuffer.length; i++) {
			lineBuffer[i] = prefix.clone();
		}
		int lastUsed = 0;
		BigDecimal stepsPerColumn = new BigDecimal(endX-startX).divide(new BigDecimal(sizeX), 2, BigDecimal.ROUND_CEILING);
		BigDecimal count = new BigDecimal(startX);
		int sectionCount = calculateXSection(sizeX);
		for(int i = 0;i<sizeX;i++){
			boolean number =  i % sectionCount == 0 && i != 0;
			lineBuffer[0].add(number || i == 0 ? "+":"-");
			numberdisplay:
			if(number){
				int step = 1;
				while (true) {
					if(step >= lineBuffer.length){
						break numberdisplay;
					}
					ColoredString message = lineBuffer[step++];
					if(message.getSize() < i+prefix.getSize()){
					step--;
					break;
					}
				}
				while (lineBuffer[step].getSize() < i+prefix.getSize()) {
					lineBuffer[step].add(" ");
				}
				lineBuffer[step].add(count+" ");
				if(step > lastUsed)
					lastUsed = step;
			}
			count = count.add(stepsPerColumn);
		}
		if(lastUsed+1<lineBuffer.length)
			lastUsed+=1;
		ColoredString string = 	lineBuffer[lastUsed];
		while (string.getSize()+xAxisName.getSize()<sizeX+prefix.getSize()) {
			string.add(" ");
		}
		if(string.getSize()+xAxisName.getSize()>=sizeX+prefix.getSize()){
			string = string.substring(0, sizeX-xAxisName.getSize()+prefix.getSize());
		}
		lineBuffer[lastUsed] = string;
		lineBuffer[lastUsed].add(xAxisName);
	}
	
	private int calculateXSection(int sizeX){
		double stepsPerColumn = (endX-startX)/stepX;
		return (int) Math.floor(sizeX/stepsPerColumn);
	}
	
	public static void main(String[] args) {
		Graph grath = new Graph();
		grath.addPoint(new Point(0, 0));
		grath.addPoint(new Point(5, 3));
		grath.addPoint(new Point(7, 4));
		grath.addPoint(new Point(10, 10));
		grath.addPoint(new Point(15, 3));
		grath.addPoint(new Point(20, 20));
		grath.character = new ColoredChar('?');
		grath.character.setColor(ChatColor.GREEN);
		grath.fillBelowUp = true;
		
		Graph grath2 = new Graph();
		grath2.addPoint(new Point(10, 0));
		grath2.addPoint(new Point(0, 10));
		
		TerminalGraph display = new TerminalGraph();
		display.graths.add(grath);
		display.startX = 20;
		display.endX = 0;
		display.startY = 0;
		display.endY = 20;
		//display.graths.add(grath2);
		for(ColoredString line : display.buildLines(200, 60, false))
			System.out.println(ChatColor.stripColor(line.toString()));
	}
}
