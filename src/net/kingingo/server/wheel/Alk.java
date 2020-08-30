package net.kingingo.server.wheel;

import java.io.DataOutputStream;
import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Alk {
	private String name;
	private int value;
	private String path;
	
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(this.name);
		out.writeInt(this.value);
		out.writeUTF(this.path);
	}
}
