package name.reidmiller.sppreports.client;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeSet;

import name.reidmiller.sppreports.model.MarketLoad;
import name.reidmiller.sppreports.model.SamplingFrequency;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class LoadForecastClientTest {
	Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * This test will work so long as it's 10 minutes into the current year.
	 */
	@Test
	public void testDefaultFiveMinuteMarketLoads() {
		LoadForecastClient loadForecastClient = new LoadForecastClient();
		List<MarketLoad> marketLoads = loadForecastClient
				.getDefaultMarketLoads(SamplingFrequency.FIVE_MINUTES);
		assertNotNull("List of five-minute " + MarketLoad.class
				+ " objects is null", marketLoads);

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(
				LoadForecastClient.LOAD_FORECAST_REPORT_DATE_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("America/Chicago"));

		try {
			Date midnight = sdf
					.parse("1/1/" + cal.get(Calendar.YEAR) + " 0:00");
			assertEquals("First date in five-minute " + MarketLoad.class
					+ " list is not midnight", midnight, marketLoads.get(0)
					.getDate());

			Date twelveOFive = sdf.parse("1/1/" + cal.get(Calendar.YEAR)
					+ " 0:05");
			assertEquals("Second date in five-minute " + MarketLoad.class
					+ " list is not 1:05am", twelveOFive, marketLoads.get(1)
					.getDate());
		} catch (ParseException e) {
			fail("Could not parse date in unit test");
		}
	}

	@Test
	public void testGetFiveMinuteMarketLoadsInRange() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(
					LoadForecastClient.LOAD_FORECAST_REPORT_DATE_FORMAT
							+ " zzzz");
			sdf.setTimeZone(TimeZone.getTimeZone("America/Chicago"));

			// Multi-year test range that runs DST correction code
			Date startDate = sdf.parse("7/15/2012 12:34 Central Daylight Time");
			Date endDate = sdf.parse("12/15/2013 6:00 Central Standard Time");

			LoadForecastClient loadForecastClient = new LoadForecastClient();
			List<MarketLoad> marketLoads = loadForecastClient
					.getMarketLoadsInRange(SamplingFrequency.FIVE_MINUTES,
							startDate, endDate);

			assertNotNull("List of five-minute " + MarketLoad.class
					+ " objects in specified date range is null",
					marketLoads);

			Date cdtStart = sdf.parse("3/10/2013 3:00 Central Daylight Time");
			Date cdtEnd = sdf.parse("11/3/2013 1:55 Central Daylight Time");
			Date cstStart = sdf.parse("11/3/2013 1:00  Central Standard Time");

			// Array indices for debugging DST error
			int i = 0;
			for (MarketLoad marketLoad : marketLoads) {
				if (i == 68430 || i == 136961 || i == 136962) {
					logger.debug(i + "= " + marketLoad.toString());
				}
				i++;
			}

			assertTrue("First date in ranged five-minute " + MarketLoad.class
					+ " list is not after start parameter",
					marketLoads.get(0).getDate().after(startDate));
			assertEquals("3:00 CDT start not at expected index", cdtStart,
					marketLoads.get(68430).getDate());
			assertEquals("1:55 CDT not at expected index", cdtEnd,
					marketLoads.get(136961).getDate());
			assertEquals("1:00 CST start not at expected index", cstStart,
					marketLoads.get(136962).getDate());
			assertEquals("Last date in ranged five-minute "
					+ MarketLoad.class + " list is equal to end parameter",
					endDate, marketLoads.get(marketLoads.size() - 1)
							.getDate());

		} catch (ParseException e) {
			fail("Could not parse date in unit test");
		}
	}

	@Test
	public void testGetUrlString() {
		LoadForecastClient loadForecastClient = new LoadForecastClient();
		String urlString = loadForecastClient.getUrlString(2014,
				SamplingFrequency.FIVE_MINUTES);
		assertEquals("Generator URL String not created properly.",
				"http://www.spp.org/LoadForecast/2014_5Minute_Load.csv",
				urlString);
	}
}
