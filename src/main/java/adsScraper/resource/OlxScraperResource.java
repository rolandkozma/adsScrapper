package adsScraper.resource;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import adsScraper.olx.OlxScraper;
import adsScraper.olx.OlxUrlBuilder.Business;
import adsScraper.olx.OlxUrlBuilder.City;
import adsScraper.olx.OlxUrlBuilder.HouseType;

@Path("olx")
public class OlxScraperResource {

	@Inject
	private OlxScraper olxScraper;

	@GET
	public void scrap() {
		olxScraper.scrap(City.CLUJ_NAPOCA, Business.PRIVATE, HouseType.APARTMENT, 2);
	}

}
