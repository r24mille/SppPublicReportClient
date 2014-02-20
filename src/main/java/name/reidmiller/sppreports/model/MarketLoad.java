package name.reidmiller.sppreports.model;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MarketLoad implements Comparable<MarketLoad> {
	private Logger logger = LogManager.getLogger(this.getClass());
	private Date date;
	private double currentLoad;
	private double loadForecast;

	@Override
	public int compareTo(MarketLoad o) {
		if (this.date == null && o.getDate() != null) {
			logger.debug("null date used in MarketLoad comparison, returning 'less than'");
			return -1;
		} else if (this.date != null && o.getDate() == null) {
			logger.debug("null date used in MarketLoad comparison, returning 'less than'");
			return 1;
		} else {
			int dateComp = this.date.compareTo(o.getDate());
			if (dateComp == 0) {
				return Double.compare(this.currentLoad, o.getCurrentLoad());
			} else {
				return dateComp;
			}
		}
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public double getCurrentLoad() {
		return currentLoad;
	}

	public void setCurrentLoad(double currentLoad) {
		this.currentLoad = currentLoad;
	}

	public double getLoadForecast() {
		return loadForecast;
	}

	public void setLoadForecast(double loadForecast) {
		this.loadForecast = loadForecast;
	}
}
