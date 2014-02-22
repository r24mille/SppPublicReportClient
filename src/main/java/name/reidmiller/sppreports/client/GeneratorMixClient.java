package name.reidmiller.sppreports.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import name.reidmiller.sppreports.model.GeneratorMix;
import name.reidmiller.sppreports.model.SamplingFrequency;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVReader;

public class GeneratorMixClient {
	public static final String GENERATOR_MIX_REPORT_DATE_FORMAT = "M/d/yyyy H:mm";
	private Logger logger = LogManager.getLogger(this.getClass());
	private DateTimeFormatter dateTimeFormat;

	GeneratorMixClient() {
		DateTimeFormatter localDateTimeFormat = DateTimeFormat
				.forPattern(GENERATOR_MIX_REPORT_DATE_FORMAT);
		this.dateTimeFormat = localDateTimeFormat.withZone(DateTimeZone
				.forID("America/Chicago"));
	}

	public List<GeneratorMix> getDefaultGeneratorMixes(
			SamplingFrequency samplingFrequency) {
		return this.getGenMixesForYear(Calendar.getInstance()
				.get(Calendar.YEAR), samplingFrequency);
	}

	public String getUrlString(int year, SamplingFrequency samplingFrequency) {
		String urlString = "http://www.spp.org/GenerationMix/" + year + "_"
				+ samplingFrequency.getUrlPart() + "_GenMix.csv";
		logger.debug("Parsing URL " + urlString);
		return urlString;
	}

	public List<GeneratorMix> getGeneratorMixesInRange(
			SamplingFrequency samplingFrequency, Date startDate, Date endDate) {
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(endDate);

		TreeSet<Integer> yearRange = new TreeSet<Integer>();
		for (int y = startCal.get(Calendar.YEAR); y <= endCal
				.get(Calendar.YEAR); y++) {
			yearRange.add(y);
		}

		List<GeneratorMix> generatorMixes = new ArrayList<GeneratorMix>();
		for (int year : yearRange) {
			List<GeneratorMix> yearGenMixes = this.getGenMixesForYear(year,
					samplingFrequency);

			if (year > startCal.get(Calendar.YEAR)
					&& year < endCal.get(Calendar.YEAR)) {
				generatorMixes.addAll(yearGenMixes);
			} else if (year == startCal.get(Calendar.YEAR)
					&& year == endCal.get(Calendar.YEAR)) {
				for (GeneratorMix genMix : yearGenMixes) {
					if (genMix.getDate().compareTo(startDate) >= 0
							&& genMix.getDate().compareTo(endDate) <= 0) {
						generatorMixes.add(genMix);
					}
				}
			} else if (year == startCal.get(Calendar.YEAR)) {
				for (GeneratorMix genMix : yearGenMixes) {
					if (genMix.getDate().compareTo(startDate) >= 0) {
						generatorMixes.add(genMix);
					}
				}
			} else if (year == endCal.get(Calendar.YEAR)) {
				for (GeneratorMix genMix : yearGenMixes) {
					if (genMix.getDate().compareTo(endDate) <= 0) {
						generatorMixes.add(genMix);
					}
				}
			}
		}

		return generatorMixes;
	}

	public List<GeneratorMix> getGenMixesForYear(int year,
			SamplingFrequency samplingFrequency) {
		DateTimeZone usCentral = DateTimeZone.forID("America/Chicago");		
		DateTime august1st = new DateTime(year, 8, 1, 0, 0, 0, 0, usCentral);
		Instant cdtToCst = new Instant(usCentral.nextTransition(august1st
				.toInstant().getMillis()));
		Instant lastCdt = null;
		boolean startCstFix = false;
		int numCstFixed = 0;
		int cstFixLimit = 0;
		switch (samplingFrequency) {
		case FIVE_MINUTES:
			lastCdt = cdtToCst.minus(Duration.standardMinutes(5));
			cstFixLimit = 12;
			break;
		case HOURLY:
			lastCdt = cdtToCst.minus(Duration.standardHours(1));
			cstFixLimit = 1;
			break;
		}
		logger.debug("Last sample of CDT is " + lastCdt.toDateTime(usCentral));

		List<GeneratorMix> generatorMixes = new ArrayList<GeneratorMix>();
		String urlString = this.getUrlString(year, samplingFrequency);
		try {
			URL url = new URL(urlString);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			CSVReader reader = new CSVReader(in);

			String[] csvLine = reader.readNext();
			for (int i = 0; csvLine != null; i++) {
				if (i > 0 && csvLine[0] != null && !csvLine[0].isEmpty()) {
					GeneratorMix generatorMix = new GeneratorMix();
					DateTime genMixDateTime = dateTimeFormat
							.parseDateTime(csvLine[0]);

					if (startCstFix && numCstFixed < cstFixLimit) {
						Instant incorrectCst = genMixDateTime.toInstant();
						Instant correctCdt = incorrectCst.plus(Duration.standardHours(1));
						logger.debug("Incorrect Date " + incorrectCst.toDateTime(usCentral) + " corrected to " + correctCdt.toDateTime(usCentral));
						generatorMix.setDate(correctCdt.toDate());
						numCstFixed++;
					} else {
						generatorMix.setDate(genMixDateTime.toDate());
						startCstFix = false;
					}
					
					if (genMixDateTime.isEqual(lastCdt)) {
						startCstFix = true;
					}

					generatorMix.setCoal(Double.parseDouble(csvLine[1]));
					generatorMix.setHydro(Double.parseDouble(csvLine[2]));
					generatorMix.setDieselFuelOil(Double
							.parseDouble(csvLine[3]));
					generatorMix.setNaturalGas(Double.parseDouble(csvLine[4]));
					generatorMix.setNuclear(Double.parseDouble(csvLine[5]));
					generatorMix.setWind(Double.parseDouble(csvLine[6]));
					generatorMix.setMarketLoad(Double.parseDouble(csvLine[7]));

					generatorMixes.add(generatorMix);
				}

				// Step to next line
				csvLine = reader.readNext();
			}

			reader.close();

		} catch (MalformedURLException e) {
			logger.warn("Could not create " + URL.class + " from \""
					+ urlString + "\"");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		return generatorMixes;
	}
}
