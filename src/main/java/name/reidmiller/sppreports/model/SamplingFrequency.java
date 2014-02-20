package name.reidmiller.sppreports.model;

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
