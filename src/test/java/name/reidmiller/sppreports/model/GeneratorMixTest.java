package name.reidmiller.sppreports.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;

public class GeneratorMixTest {

	@Test
	public void testCompareTo() {
		Calendar cal = Calendar.getInstance();
		Date today = cal.getTime();
		cal.add(Calendar.DATE, -1);
		Date yesterday = cal.getTime();

		GeneratorMix todayGenMix = new GeneratorMix();
		todayGenMix.setDate(today);
		GeneratorMix yesterdayGenMix = new GeneratorMix();
		yesterdayGenMix.setDate(yesterday);

		List<GeneratorMix> genMixes = new ArrayList<GeneratorMix>();
		genMixes.add(todayGenMix);
		genMixes.add(yesterdayGenMix);

		assertTrue("First item (today) is not after second item (yesterday).",
				genMixes.get(0).getDate().after(genMixes.get(1).getDate()));
		
		Collections.sort(genMixes);
		assertTrue("First item (yesterday) is not before second item (today).",
				genMixes.get(0).getDate().before(genMixes.get(1).getDate()));
	}
}
