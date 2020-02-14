package net.kingingo.server.packets;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UnknownPacket extends Packet {
	
	private int _id;

	public UnknownPacket(int id,byte[] data){
		this.id=0;
		this._id=id;
		setData(data);
	}
	
	public int getId() {
		return this._id;
	}

	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		
	}
	
	protected void setId(int id) {
		this._id = id;
	}

	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		
	}
}
