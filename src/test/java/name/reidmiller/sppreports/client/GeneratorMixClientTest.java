package name.reidmiller.sppreports.client;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeSet;

import name.reidmiller.sppreports.model.GeneratorMix;
import name.reidmiller.sppreports.model.SamplingFrequency;

import org.junit.Test;

public class GeneratorMixClientTest {

	/**
	 * This test will work so long as it's 10 minutes into the current year.
	 */
	@Test
	public void testDefaultFiveMinuteGeneratorMixes() {
		GeneratorMixClient genMixClient = new GeneratorMixClient();
		List<GeneratorMix> generatorMixes = genMixClient
				.getDefaultGeneratorMixes(SamplingFrequency.FIVE_MINUTES);
		assertNotNull("List of five-minute " + GeneratorMix.class
				+ " objects is null", generatorMixes);

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(
				GeneratorMixClient.GENERATOR_MIX_REPORT_DATE_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("America/Chicago"));

		try {
			Date midnight = sdf
					.parse("1/1/" + cal.get(Calendar.YEAR) + " 0:00");
			assertEquals("First date in five-minute " + GeneratorMix.class
					+ " list is not midnight", midnight, generatorMixes.get(0)
					.getDate());

			Date twelveOFive = sdf.parse("1/1/" + cal.get(Calendar.YEAR)
					+ " 0:05");
			assertEquals("Second date in five-minute " + GeneratorMix.class
					+ " list is not 1:05am", twelveOFive, generatorMixes.get(1)
					.getDate());
		} catch (ParseException e) {
			fail("Could not parse date in unit test");
		}
	}

	/**
	 * This test will work so long as it's 2 hours into the current year.
	 */
	@Test
	public void testDefaultHourlyGeneratorMixes() {
		GeneratorMixClient genMixClient = new GeneratorMixClient();
		List<GeneratorMix> generatorMixes = genMixClient
				.getDefaultGeneratorMixes(SamplingFrequency.HOURLY);
		assertNotNull("List of hourly " + GeneratorMix.class
				+ " objects is null", generatorMixes);

		try {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(
					GeneratorMixClient.GENERATOR_MIX_REPORT_DATE_FORMAT);
			sdf.setTimeZone(TimeZone.getTimeZone("America/Chicago"));
			Date midnight = sdf
					.parse("1/1/" + cal.get(Calendar.YEAR) + " 0:00");
			assertEquals("First date in hourly " + GeneratorMix.class
					+ " list is not midnight", midnight, generatorMixes.get(0)
					.getDate());

			Date oneOClock = sdf.parse("1/1/" + cal.get(Calendar.YEAR)
					+ " 1:00");
			assertEquals("Second date in hourly " + GeneratorMix.class
					+ " list is not 1:00am", oneOClock, generatorMixes.get(1)
					.getDate());
		} catch (ParseException e) {
			fail("Could not parse date in unit test");
		}
	}

	@Test
	public void testGetFiveMinuteGeneratorMixesInRange() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(
					GeneratorMixClient.GENERATOR_MIX_REPORT_DATE_FORMAT
							+ " zzzz");
			sdf.setTimeZone(TimeZone.getTimeZone("America/Chicago"));

			Date startDate = sdf.parse("7/15/2012 12:34 Central Daylight Time");
			Date endDate = sdf.parse("12/15/2013 6:00 Central Standard Time");

			GeneratorMixClient genMixClient = new GeneratorMixClient();
			List<GeneratorMix> generatorMixes = genMixClient
					.getGeneratorMixesInRange(SamplingFrequency.FIVE_MINUTES,
							startDate, endDate);

			assertNotNull("List of five-minute " + GeneratorMix.class
					+ " objects in specified date range is null",
					generatorMixes);

			Date cdtStart = sdf.parse("3/10/2013 3:00 Central Daylight Time");
			Date cdtEnd = sdf.parse("11/3/2013 1:55 Central Daylight Time");
			Date cstStart = sdf.parse("11/3/2013 1:00  Central Standard Time");
			
			// TODO Daylight savings error, parses 1:00-1:59 CST twice
			int i = 0;
			for (GeneratorMix generatorMix : generatorMixes) {
				if (i == 68429 || i == 136960 || i == 136961) {
					System.out.println(i + "= " + generatorMix.toString());
				}
				i++;
			}

			assertTrue("First date in ranged five-minute " + GeneratorMix.class
					+ " list is not after start parameter",
					generatorMixes.get(0).getDate().after(startDate));
			assertEquals("3:00 CDT start not at expected index", cdtStart,
					generatorMixes.get(68429).getDate());
			assertEquals("1:55 CDT not at expected index", cdtEnd,
					generatorMixes.get(136960).getDate());
			assertEquals("1:00 CST start not at expected index", cstStart,
					generatorMixes.get(136961).getDate());
			assertEquals("Last date in ranged five-minute "
					+ GeneratorMix.class + " list is equal to end parameter",
					endDate, generatorMixes.get(generatorMixes.size() - 1)
							.getDate());

		} catch (ParseException e) {
			fail("Could not parse date in unit test");
		}
	}

	@Test
	public void testGetUrlString() {
		GeneratorMixClient genMixClient = new GeneratorMixClient();
		String urlString = genMixClient.getUrlString(2014,
				SamplingFrequency.FIVE_MINUTES);
		assertEquals("Generator URL String not created properly.",
				"http://www.spp.org/GenerationMix/2014_5Minute_GenMix.csv",
				urlString);
	}
}
