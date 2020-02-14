package net.kingingo.server.packets;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface IData {
	public void parseFromInput(DataInputStream in) throws IOException;
	public void writeToOutput(DataOutputStream out) throws IOException;
	public String toString();
}
