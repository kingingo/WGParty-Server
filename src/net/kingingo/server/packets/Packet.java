package net.kingingo.server.packets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.reflections.Reflections;

import lombok.Getter;
import net.kingingo.server.Main;
import net.kingingo.server.packets.server.IdsPacket;

public abstract class Packet implements IData{
	private static final HashMap<Integer,Class<? extends Packet>> packets = new HashMap<Integer, Class<? extends Packet>>();
	@Getter
	private static final HashMap<String, Integer> packet_ids = new HashMap<>();
	@Getter
	private static int packetIdMax =1;
	protected int id;
	
	public static void loadPackets() {		
		Reflections reflections = new Reflections( "net.kingingo.server.packets" );
		List<Class<? extends Packet>> moduleClasses = new ArrayList<>( reflections.getSubTypesOf( Packet.class ) );

		Collections.sort(moduleClasses, new Comparator<Class<? extends Packet>>() {
		    @Override
		    public int compare(Class<? extends Packet> o1, Class<? extends Packet> o2) {
		        return o1.getSimpleName().compareTo(o2.getSimpleName());
		    }
		});
		
		int id = 1;
		for ( Class<? extends Packet> clazz : moduleClasses ){
			if(clazz == UnknownPacket.class)continue;
			try {
				Packet instance = clazz.newInstance();
				packet_ids.put(instance.getPacketName(), (clazz == IdsPacket.class ? 0 : id));
				packets.put((clazz == IdsPacket.class ? 0 : id), clazz);
				id++;
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
					e.printStackTrace();
			}
			
		}
		packetIdMax=packets.size()-1;
		
		for(String packet : packet_ids.keySet()) {
			id = packet_ids.get(packet);
			Main.printf(packet+" -> "+ id + " " +(packets.containsKey(id)));
		}
	}
	
	public static void writeBytes(byte[] ar, DataOutputStream out) {
		try {
			out.writeInt(ar.length);
			out.write(ar);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static byte[] readBytes(DataInputStream in) throws IOException {
		int length = in.readInt();
		byte[] byteArray = new byte[length];
		in.readFully(byteArray, 0, length);
		return byteArray;
	}
	
	public static String getPacketName(Class<?> clazz) {
		String simpleName = clazz.getSimpleName();
		simpleName = simpleName.substring(0, simpleName.indexOf("Packet"));
		return simpleName.toUpperCase();
	}
	
	public static String getPacketName(int packetId) {
		if(packets.get(packetId)==null) {
			for(String packetName : packet_ids.keySet()) {
				if(packet_ids.get(packetName) == packetId) {
					Main.printf("Found packet "+packetId+" "+packetName);
					return packetName;
				}
			}
			
			throw new NullPointerException("Couldn't find packet!? ID:"+packetId+", packets_size="+packets.size());
		}
		
		return packets.get(packetId).getSimpleName();
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public static  <T extends Packet> boolean toEncrypt(int packetId) {
		if(packets.containsKey(packetId)) {
			Class<T> clazz = (Class<T>) packets.get(packetId);
			try {
				return clazz.newInstance().toEncrypt();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static int getId(Class<?> clazz) {
		return getId(getPacketName(clazz));
	}
	
	public static int getId(String packetName) {
		return packet_ids.get(packetName);
	}
	
	public static <T extends Packet> T create(Class<T> clazz, byte[] data) {
		return create(clazz, new DataInputStream(new ByteArrayInputStream(data)));
	}
	
	public static <T extends Packet> T create(Class<T> clazz, DataInputStream in) {
		try {
			T packet = (T) clazz.newInstance();
			packet.parseFromInput(in);
			
			return (T) packet;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new NullPointerException();
	}
	
	public static <T extends Packet> T create(int packetId, byte[] data) {
		return create(packetId, new DataInputStream(new ByteArrayInputStream(data)));
	}
	
	@SuppressWarnings({ "unchecked", "FinalStaticMethod" })
	public static <T extends Packet> T create(int packetId, DataInputStream in) {
		if(packets.containsKey(packetId)) {
			Class<T> clazz = (Class<T>) packets.get(packetId);
			return create(clazz,in);
		}
		try {
			return (T) (new UnknownPacket(packetId, in.readAllBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (T) (new UnknownPacket(packetId, null));
	}

	private byte[] data;
	
	public void setData(byte[] data) {
		this.data=data;
	}
	
	public boolean toEncrypt() {
		return true;
	}
	
	public byte[] getData() {
		return this.data;
	}
	
//	public void parseFromInput(byte[] bytes) throws IOException {
//		this.parseFromInput(new DataInputStream( new ByteArrayInputStream(bytes) ));
//	}
	
	public int getId() {
		return this.packet_ids.get(getPacketName());
	}
	
	public String getPacketName(){
		String simpleName = this.getClass().getSimpleName();
		simpleName = simpleName.substring(0, simpleName.indexOf("Packet"));
		return simpleName.toUpperCase();
	}
	
	public byte[] toByteArray(){
		if(this instanceof UnknownPacket)return ((UnknownPacket)this).getData();
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream( baos );
			writeToOutput(out);
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}
}
