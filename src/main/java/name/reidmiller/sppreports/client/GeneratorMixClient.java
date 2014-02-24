package name.reidmiller.sppreports.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
	public static final DateTimeZone US_CENTRAL_ZONE = DateTimeZone.forID("America/Chicago");
	private Logger logger = LogManager.getLogger(this.getClass());
	private DateTimeFormatter centralTimeFormat;

	/**
	 * GeneratorMixClient constructor sets {@link #centralTimeFormat} using
	 * {@value #GENERATOR_MIX_REPORT_DATE_FORMAT} and {@link #US_CENTRAL_ZONE}.
	 */
	GeneratorMixClient() {
		DateTimeFormatter localDateTimeFormat = DateTimeFormat
				.forPattern(GENERATOR_MIX_REPORT_DATE_FORMAT);
		this.centralTimeFormat = localDateTimeFormat.withZone(US_CENTRAL_ZONE);
	}

	/**
	 * @param samplingFrequency
	 *            Either five-minute or hourly report.
	 * @return List of {@link GeneratorMix} objects from the start of the
	 *         current year to the current date.
	 */
	public List<GeneratorMix> getDefaultGeneratorMixes(
			SamplingFrequency samplingFrequency) {
		return this.getGenMixesForYear(DateTime.now().getYear(),
				samplingFrequency);
	}

	/**
	 * @param year
	 *            Year of report to request.
	 * @param samplingFrequency
	 *            Switches the report URL String between five-minute and hourly
	 *            using {@link SamplingFrequency#getUrlPart()}.
	 * @return SPP GenerationMix report URL string stitched together based on
	 *         parameters passed to method.
	 */
	public String getUrlString(int year, SamplingFrequency samplingFrequency) {
		String urlString = "http://www.spp.org/GenerationMix/" + year + "_"
				+ samplingFrequency.getUrlPart() + "_GenMix.csv";
		logger.debug("Parsing URL " + urlString);
		return urlString;
	}

	/**
	 * Iterates over time range specified and repeatedly calls
	 * {@link #getGenMixesForYear(int, SamplingFrequency)} to build out the List
	 * of {@link GeneratorMix} objects. Items outside of date range are filtered
	 * out if necessary.
	 * 
	 * @param samplingFrequency
	 * 
	 * @param startDate
	 *            Lower bound of {@link GeneratorMix} objects in the List
	 *            returned.
	 * @param endDate
	 *            Upper bound of {@link GeneratorMix} objects in the List
	 *            returned.
	 * @return List of {@link GeneratorMix} objects in the specified date range.
	 */
	public List<GeneratorMix> getGeneratorMixesInRange(
			SamplingFrequency samplingFrequency, Date startDate, Date endDate) {
		DateTime startDateTime = new DateTime(startDate);
		DateTime endDateTime = new DateTime(endDate);

		TreeSet<Integer> yearRange = new TreeSet<Integer>();
		for (int y = startDateTime.getYear(); y <= endDateTime.getYear(); y++) {
			yearRange.add(y);
		}

		List<GeneratorMix> generatorMixes = new ArrayList<GeneratorMix>();
		for (int year : yearRange) {
			List<GeneratorMix> yearGenMixes = this.getGenMixesForYear(year,
					samplingFrequency);

			if (year > startDateTime.getYear() && year < endDateTime.getYear()) {
				generatorMixes.addAll(yearGenMixes);
			} else if (year == startDateTime.getYear()
					&& year == endDateTime.getYear()) {
				for (GeneratorMix genMix : yearGenMixes) {
					if (genMix.getDate().compareTo(startDate) >= 0
							&& genMix.getDate().compareTo(endDate) <= 0) {
						generatorMixes.add(genMix);
					}
				}
			} else if (year == startDateTime.getYear()) {
				for (GeneratorMix genMix : yearGenMixes) {
					if (genMix.getDate().compareTo(startDate) >= 0) {
						generatorMixes.add(genMix);
					}
				}
			} else if (year == endDateTime.getYear()) {
				for (GeneratorMix genMix : yearGenMixes) {
					if (genMix.getDate().compareTo(endDate) <= 0) {
						generatorMixes.add(genMix);
					}
				}
			}
		}

		return generatorMixes;
	}

	/**
	 * Method retrieves the GeneratorMix objects for a given year. Because the
	 * CSV provides times without offset or daylight savings information, this
	 * method does a bit of work to find and correct the error, creating
	 * {@link Date} objects with proper offset information.
	 * 
	 * @param year
	 *            Year of report.
	 * @param samplingFrequency
	 *            Five-minute or hourly sampling frequency controls which report
	 *            URL is created. Passed to
	 *            {@link #getUrlString(int, SamplingFrequency)} along with year.
	 * @return List of {@link GeneratorMix} objects for the year.
	 */
	public List<GeneratorMix> getGenMixesForYear(int year,
			SamplingFrequency samplingFrequency) {
		// An arbitrary date known to be in the middle of daylight savings.
		DateTime august1st = new DateTime(year, 8, 1, 0, 0, 0, 0, US_CENTRAL_ZONE);
		// Instant of transition from daylight savings to standard time.
		Instant cdtToCst = new Instant(US_CENTRAL_ZONE.nextTransition(august1st
				.toInstant().getMillis()));
		// Instant of last daylight savings sample in the report.
		Instant lastCdt = null;
		// When last sample in DST has been hit, trigger standard time fix.
		boolean startCstFix = false;
		// Number of records to fix changes for five-minute or hourly report.
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
		logger.debug("Last sample of CDT is "
				+ lastCdt.toDateTime(US_CENTRAL_ZONE));

		List<GeneratorMix> generatorMixes = new ArrayList<GeneratorMix>();
		String urlString = this.getUrlString(year, samplingFrequency);
		try {
			URL url = new URL(urlString);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			CSVReader reader = new CSVReader(in);

			// Loop over each row of CSV report
			String[] csvLine = reader.readNext();
			for (int i = 0; csvLine != null; i++) {
				// Skip first row and empty rows
				if (i > 0 && csvLine[0] != null && !csvLine[0].isEmpty()) {
					GeneratorMix generatorMix = new GeneratorMix();
					DateTime genMixDateTime = centralTimeFormat
							.parseDateTime(csvLine[0]);

					// If DST flag has been set and fix count is under limit
					if (startCstFix && numCstFixed < cstFixLimit) {
						Instant incorrectCst = genMixDateTime.toInstant();
						Instant correctCdt = incorrectCst.plus(Duration
								.standardHours(1));
						logger.debug("Incorrect Date "
								+ incorrectCst.toDateTime(US_CENTRAL_ZONE)
								+ " corrected to "
								+ correctCdt.toDateTime(US_CENTRAL_ZONE));
						generatorMix.setDate(correctCdt.toDate());
						numCstFixed++;
					} else {
						generatorMix.setDate(genMixDateTime.toDate());
					}

					// When last DST row hit, trigger fix to tart next iteration
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
