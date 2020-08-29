package test.kingingo;

import net.kingingo.server.mysql.MySQL;
import net.kingingo.server.user.User;

public class Factory {

	public static boolean connectMySQL() {
		return MySQL.connect("root", "","localhost","test",3306);
	}
	
	public static User create() {
		return User.createTestUser("Leon",10,0);
	}
	
}
