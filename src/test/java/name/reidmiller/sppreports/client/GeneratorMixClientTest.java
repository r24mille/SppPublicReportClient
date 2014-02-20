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
			Date jan1st = sdf.parse("1/1/" + cal.get(Calendar.YEAR) + " 0:00");
			GeneratorMix firstGeneratorMix = generatorMixes.get(0);
			assertEquals(jan1st, firstGeneratorMix.getDate());
		} catch (ParseException e) {
			fail("Could not parse date in unit test");
		}

		// TODO Daylight savings error, parses 1:00-1:59 CST twice
//		for (GeneratorMix generatorMix : generatorMixes) {
//			Calendar derp = Calendar.getInstance();
//			derp.set(2013, Calendar.NOVEMBER, 3, 2, 59);
//
//			Calendar drup = Calendar.getInstance();
//			drup.set(2013, Calendar.NOVEMBER, 2, 23, 59);
//			if (generatorMix.getDate().before(derp.getTime())
//					&& generatorMix.getDate().after(drup.getTime())) {
//				System.out.println(generatorMix);
//			}
//		}
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
