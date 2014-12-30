package adsScraper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import adsScraper.dto.MinimumAdsDetailDto;
import adsScraper.email.EmailSender;
import adsScraper.olx.OlxScraper;
import adsScraper.olx.OlxUrlBuilder.Business;
import adsScraper.olx.OlxUrlBuilder.City;
import adsScraper.olx.OlxUrlBuilder.HouseType;

@Stateless
public class ApartmentScraper {

	@Inject
	private OlxScraper olxScraper;

	@Inject
	private EmailSender mailSender;

	private static final List<String> KEY_WORDS = Arrays.asList("babes", "bisericii ortodoxe", "botanica", "brd", "centru", "central ", "central.",
			"central,", "central;", "central!", "cipariu", "dorobantilor", "garibaldi", "godeanu", "mihai viteazu", "nasaud", "observator", "opera",
			"parc ", "parc,", "parc.", "parc;", "parc!", "persoana fizica", "p f", "p. f.", "pf ", "plopilor", "recuperare", "regionala",
			"republicii", "scortarilor", "sigma", "sporturilor", "teatru", "titulescu", "vlahuta", "zorilor", "13 septembrie");

	// @Schedule(hour = "*/4", persistent = true)
	public List<MinimumAdsDetailDto> scrap() {
		List<MinimumAdsDetailDto> allApartments = new ArrayList<>();

		List<MinimumAdsDetailDto> olxApartments = scrapOlx();
		allApartments.addAll(olxApartments);

		// TODO scrap apartments from other sites

		mailSender.sendEmail(allApartments);
		return allApartments;
	}

	private List<MinimumAdsDetailDto> scrapOlx() {
		List<MinimumAdsDetailDto> allFoundApartments = new ArrayList<MinimumAdsDetailDto>();

		List<MinimumAdsDetailDto> found2RoomApartments = olxScraper.scrap(City.CLUJ_NAPOCA, Business.PRIVATE, HouseType.APARTMENT, 2, KEY_WORDS);
		allFoundApartments.addAll(found2RoomApartments);

		List<MinimumAdsDetailDto> found3RoomApartments = olxScraper.scrap(City.CLUJ_NAPOCA, Business.PRIVATE, HouseType.APARTMENT, 3, KEY_WORDS);
		allFoundApartments.addAll(found3RoomApartments);

		List<MinimumAdsDetailDto> found4RoomApartments = olxScraper.scrap(City.CLUJ_NAPOCA, Business.PRIVATE, HouseType.APARTMENT, 4, KEY_WORDS);
		allFoundApartments.addAll(found4RoomApartments);

		return allFoundApartments;
	}

}
