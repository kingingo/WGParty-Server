package net.kingingo.server.user.stats;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import lombok.Getter;
import lombok.Setter;
import net.kingingo.server.mysql.MySQL;
import net.kingingo.server.user.User;
import net.kingingo.server.user.UserStats;
import net.kingingo.server.utils.Callback;

@Getter
@Setter
public class Stats<T> implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4813978999582047391L;
	private T value;
	private String key;
	
	public Stats(String key, T value) {
		this.key = key;
		this.value = value;
	}
	
	public String toString() {
		return "Stats: "+this.key+"->"+this.value;
	}
	
//	public void parseFromInput(DataInputStream in) throws IOException {
//		this.key = in.readUTF();
//		byte[] b = new byte[in.read()];
//		in.read(b, 0, b.length);
//		try {
//			this.value = (T) deserialize(b);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	public /*<T>*/ void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(this.key);
//		byte[] b = serialize(this.value);
//		out.write(b.length);
//		out.write(b, 0, b.length);
		if(this.value instanceof Integer) {
			out.writeByte(1);
			out.writeInt( (Integer) this.value );
		}else if(this.value instanceof Boolean) {
			out.writeByte(2);
			out.writeBoolean((Boolean) this.value);
		}
	}
	
	private static byte[] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

	private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                return o.readObject();
            }
        }
    }
	
	@SuppressWarnings("unchecked")
	public static <T> Stats<T> deserializeStat(byte[] bytes) throws ClassNotFoundException, IOException {
		return (Stats<T>) deserialize(bytes);
	}
	
	public static <T> byte[] serializeStat(Stats<T> s) throws IOException {
		return serialize(s);
	}
	
	public static void insert(Stats<?> stat, User user) {
		MySQL.Update("INSERT INTO "+UserStats.MYSQL_TABLE+" VALUES (?,?,?);", new Callback<PreparedStatement>() {
			
			@Override
			public void run(PreparedStatement stmt) {
				try {
					stmt.setString(1, user.getUuid().toString());
					stmt.setString(2, stat.getKey());
					stmt.setBytes(3, Stats.serializeStat(stat));
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
