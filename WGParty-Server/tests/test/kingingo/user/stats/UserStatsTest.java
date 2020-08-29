package test.kingingo.user.stats;

import org.junit.Before;
import org.junit.jupiter.api.Test;

import net.kingingo.server.user.User;
import net.kingingo.server.user.UserStats;
import test.kingingo.Factory;

public class UserStatsTest {

	private User leon;
	
	@Before
	public void setUp() {
		Factory.connectMySQL();
		leon = Factory.create();
	}
	
	@Test
	public void test() {
		UserStats stats = leon.getStats();
		
		int wins = stats.getInt("wins");
		int loses = stats.getInt("loses");
		
		
	}
	
}
