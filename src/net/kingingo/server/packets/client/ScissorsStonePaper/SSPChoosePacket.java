package net.kingingo.server.packets.client.ScissorsStonePaper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lombok.Getter;
import net.kingingo.server.games.ScissorsStonePaper.ScissorsStonePaper;
import net.kingingo.server.packets.Packet;
import net.kingingo.server.user.User;

public class SSPChoosePacket extends Packet{
	@Getter
	private ScissorsStonePaper.Choose choose;
	private User user;
	
	public SSPChoosePacket() {}
	public SSPChoosePacket(ScissorsStonePaper.Choose choose, User user) {
		this.choose = choose;
		this.user = user;
	}

	//Client kann nur senden was er wählt
	@Override
	public void parseFromInput(DataInputStream in) throws IOException {
		this.choose = ScissorsStonePaper.Choose.values()[in.read()];
	}

	//Server sendet weiter was wer gewählt hat
	@Override
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.write(this.choose.ordinal());
		out.writeUTF(this.user.getUuid().toString());
	}
	
	
}
