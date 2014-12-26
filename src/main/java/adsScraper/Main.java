package adsScraper;

import adsScraper.olx.OlxScraper;
import adsScraper.olx.OlxUrlBuilder.Business;
import adsScraper.olx.OlxUrlBuilder.City;
import adsScraper.olx.OlxUrlBuilder.HouseType;

public class Main {

	public static void main(String[] args) {
		new OlxScraper().scrap(City.CLUJ_NAPOCA, Business.PRIVATE, HouseType.APARTMENT, 2);
	}

}
