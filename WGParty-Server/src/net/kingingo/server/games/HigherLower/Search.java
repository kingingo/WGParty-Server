package net.kingingo.server.games.HigherLower;

import java.io.DataOutputStream;
import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Search {
	public String request;
	public String imagePath;
	public int amount;
	
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(request);
		out.writeUTF(imagePath);
		out.writeInt(amount);
	}
}
