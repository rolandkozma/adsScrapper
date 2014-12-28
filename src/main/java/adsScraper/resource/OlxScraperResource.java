package adsScraper.resource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import adsScraper.dto.MinimumAdsDetailListDto;
import adsScraper.olx.OlxScraper;
import adsScraper.olx.OlxUrlBuilder.Business;
import adsScraper.olx.OlxUrlBuilder.City;
import adsScraper.olx.OlxUrlBuilder.HouseType;

@Path("olx")
public class OlxScraperResource {

	@Inject
	private OlxScraper olxScraper;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("apartments")
	public MinimumAdsDetailListDto scrap() {
		return olxScraper.scrap(City.CLUJ_NAPOCA, Business.PRIVATE, HouseType.APARTMENT, 2, getkeyWords());
	}

	public List<String> getkeyWords() {
		List<String> keyWords = new ArrayList<>();
		keyWords.add("babes");
		keyWords.add("bisericii ortodoxe");
		keyWords.add("botanica");
		keyWords.add("brd");
		keyWords.add("centru");
		keyWords.add("central ");
		keyWords.add("central.");
		keyWords.add("central,");
		keyWords.add("central;");
		keyWords.add("central!");
		keyWords.add("cipariu");
		keyWords.add("dorobantilor");
		keyWords.add("garibaldi");
		keyWords.add("godeanu");
		keyWords.add("mihai viteazu");
		keyWords.add("nasaud");
		keyWords.add("observator");
		keyWords.add("parc ");
		keyWords.add("parc,");
		keyWords.add("parc.");
		keyWords.add("parc;");
		keyWords.add("parc!");
		keyWords.add("plopilor");
		keyWords.add("regionala");
		keyWords.add("republicii");
		keyWords.add("scortarilor");
		keyWords.add("sigma");
		keyWords.add("sporturilor");
		keyWords.add("teatru");
		keyWords.add("titulescu");
		keyWords.add("vlahuta");
		keyWords.add("zorilor");
		keyWords.add("13 septembrie");

		return keyWords;
	}

}
