package name.reidmiller.sppreports.model;

import java.util.Date;

public class GeneratorMix implements Comparable<GeneratorMix> {
	private Date date;
	private double coal;
	private double hydro;
	private double dieselFuelOil;
	private double naturalGas;
	private double nuclear;
	private double wind;
	private double marketLoad;

	@Override
	public int compareTo(GeneratorMix o) {
		if (this.date == null && o.getDate() != null) {
			return -1;
		} else if (this.date != null && o.getDate() == null) {
			return 1;
		} else {
			int dateComp = this.date.compareTo(o.getDate());
			if (dateComp == 0) {
				return Double.compare(this.marketLoad, o.getMarketLoad());
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

	public double getCoal() {
		return coal;
	}

	public void setCoal(double coal) {
		this.coal = coal;
	}

	public double getHydro() {
		return hydro;
	}

	public void setHydro(double hydro) {
		this.hydro = hydro;
	}

	public double getDieselFuelOil() {
		return dieselFuelOil;
	}

	public void setDieselFuelOil(double dieselFuelOil) {
		this.dieselFuelOil = dieselFuelOil;
	}

	public double getNaturalGas() {
		return naturalGas;
	}

	public void setNaturalGas(double naturalGas) {
		this.naturalGas = naturalGas;
	}

	public double getNuclear() {
		return nuclear;
	}

	public void setNuclear(double nuclear) {
		this.nuclear = nuclear;
	}

	public double getWind() {
		return wind;
	}

	public void setWind(double wind) {
		this.wind = wind;
	}

	public double getMarketLoad() {
		return marketLoad;
	}

	public void setMarketLoad(double marketLoad) {
		this.marketLoad = marketLoad;
	}
}
