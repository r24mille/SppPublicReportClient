package name.reidmiller.sppreports.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;

public class MarketLoadTest {

	@Test
	public void testCompareTo() {
		Calendar cal = Calendar.getInstance();
		Date today = cal.getTime();
		cal.add(Calendar.DATE, -1);
		Date yesterday = cal.getTime();

		MarketLoad todayMarketLoad = new MarketLoad();
		todayMarketLoad.setDate(today);
		MarketLoad yesterdayMarketLoad = new MarketLoad();
		yesterdayMarketLoad.setDate(yesterday);

		List<MarketLoad> marketLoads = new ArrayList<MarketLoad>();
		marketLoads.add(todayMarketLoad);
		marketLoads.add(yesterdayMarketLoad);

		assertTrue("First item (today) is not after second item (yesterday).",
				marketLoads.get(0).getDate().after(marketLoads.get(1).getDate()));
		
		Collections.sort(marketLoads);
		assertTrue("First item (yesterday) is not before second item (today).",
				marketLoads.get(0).getDate().before(marketLoads.get(1).getDate()));
	}

}
