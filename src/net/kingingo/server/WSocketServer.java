package net.kingingo.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import net.kingingo.server.event.EventManager;
import net.kingingo.server.event.events.ClientConnectEvent;
import net.kingingo.server.event.events.ClientDisonnectEvent;
import net.kingingo.server.event.events.ClientErrorEvent;
import net.kingingo.server.event.events.PacketReceiveEvent;
import net.kingingo.server.event.events.PacketSendEvent;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.user.User;

public class WSocketServer extends WebSocketServer{

	public WSocketServer(int port) {
		super(new InetSocketAddress(port));
	}
	
	public void broadcast(Packet packet) {
		for(WebSocket conn : getConnections()) {
			write(conn,packet);
		}
	}
	
	public boolean write(User user, Packet packet) {
		return write(user.getSocket(),packet);
	}
	
	public boolean write(WebSocket conn, Packet packet) {	
		PacketSendEvent ev = new PacketSendEvent(false, User.getUser(conn), packet);
		EventManager.callEvent(ev);
		
		if(!ev.isCancelled()) {
			packet = ev.getPacket();
			byte[] packetBytes = packet.toByteArray();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream( baos );
			try {
				out.writeInt(packetBytes.length);
				out.writeInt(packet.getId());
				out.write(packetBytes, 0, packetBytes.length);
				conn.send(baos.toByteArray());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return !ev.isCancelled();
	}
	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		ClientConnectEvent ev = new ClientConnectEvent(new User(conn),handshake);
		EventManager.callEvent(ev);
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		User user = User.getUser(conn);
		
		if(user!=null) {
			ClientDisonnectEvent ev = new ClientDisonnectEvent(User.getUser(conn),code,reason,remote);
			EventManager.callEvent(ev);
		}else Main.error("Disconnect User==null ");
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer buffer) {
		byte[] buf = buffer.array();
		
//		for(byte b : buf)System.out.print(b+",");
//		System.out.println();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		DataInputStream in = new DataInputStream(bais);
		
		try {
			int packetLength = in.readInt();
			int packetId = in.readInt();
			
//			Main.printf("LENGTH: "+packetLength);
//			Main.printf("ID:"+packetId);
//			Main.printf("Boolean: "+in.readBoolean());
			
			
			if(packetLength != in.available()) {
				Main.printf("The length doesn't suit to the packet("+packetId+")... "+in.available()+" != "+packetLength);
				return;
			}

			Packet packet = Packet.create(packetId, in);
			PacketReceiveEvent ev = new PacketReceiveEvent(User.getUser(conn), packet);
			EventManager.callEvent(ev);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		ClientErrorEvent ev = new ClientErrorEvent(User.getUser(conn), ex);
		EventManager.callEvent(ev);
	}

	@Override
	public void onStart() {
		System.out.println("Server started... listing on "+getPort());
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("Got a MESSAGE (STRING) "+conn.getResourceDescriptor()+":"+message);
	}
}
