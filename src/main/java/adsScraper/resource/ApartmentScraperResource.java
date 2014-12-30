package adsScraper.resource;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import adsScraper.ApartmentScraper;
import adsScraper.dto.MinimumAdsDetailDto;
import adsScraper.dto.MinimumAdsDetailListDto;

@Path("apartments")
public class ApartmentScraperResource {

	@Inject
	ApartmentScraper apartmentScraper;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public MinimumAdsDetailListDto scrap() {
		List<MinimumAdsDetailDto> allFoundAds = apartmentScraper.scrap();
		return new MinimumAdsDetailListDto(allFoundAds);
	}

}
