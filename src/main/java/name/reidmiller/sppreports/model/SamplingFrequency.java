package name.reidmiller.sppreports.model;

/**
 * Reports are dissemination with varying frequency. This enum provides a
 * convenient means to relate all possible frequencies with the term used in
 * report URLs.
 */
public enum SamplingFrequency {
	FIVE_MINUTES("5Minute"), HOURLY("Hourly");
	private String urlPart;

	SamplingFrequency(String urlPart) {
		this.urlPart = urlPart;
	}

	public String getUrlPart() {
		return this.urlPart;
	}
}
