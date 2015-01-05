package adsScraper.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adsScraper.dto.MinimumAdsDetailDto;
import adsScraper.mongo.entities.ScrapingSession;
import adsScraper.olx.OlxApartmentScraper;
import adsScraper.olx.OlxUrlBuilder;
import adsScraper.olx.OlxUrlBuilder.Business;
import adsScraper.olx.OlxUrlBuilder.City;
import adsScraper.olx.OlxUrlBuilder.Order;
import adsScraper.olx.OlxUrlBuilder.RealEstateType;

@Stateless
public class ApartmentScraper extends AdvertismentScraper {

	private static final Logger LOG = LoggerFactory.getLogger(ApartmentScraper.class);

	@Inject
	private OlxApartmentScraper olxApartmentScraper;

	private static final List<String> WANTED_KEY_WORDS = Arrays.asList("babes", "bisericii ortodoxe", "botanica", "brd", "centru", "central ",
			"central.", "central,", "central;", "central!", "cipariu", "constructor", "dorobantilor", "dezvoltator", "garibaldi", "godeanu",
			"mihai viteazu", "nasaud", "observator", "opera", "parc ", "parc,", "parc.", "parc;", "parc!", "persoana fizica", "p f", "p. f.", "pf ",
			"plopilor", "recuperare", "regionala", "republicii", "scortarilor", "sigma", "sporturilor", "teatru", "titulescu", "vlahuta", "zorilor",
			"13 septembrie");

	private static final List<String> UNWANTED_KEY_WORDS = Arrays.asList("alunis", "apahid", "baumax", "borhanci", "baciu", "bulgaria", "campului",
			"demisol", "4 / 4", "4/4", "4 din 4", "fabricii", "floresti", "gara ", "gara.", "garii", "gilau", "gradina", "horea", "iclod", "ionescu",
			"iris", "mansarda", "manastur", "muresului", "parter", "polus", "semidecomandat", "someseni", "turda");

	@Override
	RealEstateType getScrapingSessionType() {
		return RealEstateType.APARTMENT;
	}

	@Override
	void logScrapedRecordsSize(ScrapingSession scrapingSession) {
		LOG.info("This scraping session has scraped {} records!", scrapingSession.getApartments().size());
	}

	@Override
	List<MinimumAdsDetailDto> scrapOlx(Date lastScrapingDate, ScrapingSession scrapingSession) {
		List<MinimumAdsDetailDto> allFoundApartments = new ArrayList<MinimumAdsDetailDto>();

		OlxUrlBuilder olxUrlBuilder = new OlxUrlBuilder().city(City.CLUJ_NAPOCA).business(Business.PRIVATE).orderBy(Order.DATE);

		List<MinimumAdsDetailDto> found2RoomApartments = olxApartmentScraper.scrap(olxUrlBuilder.rooms(2), WANTED_KEY_WORDS, UNWANTED_KEY_WORDS,
				lastScrapingDate, scrapingSession);
		allFoundApartments.addAll(found2RoomApartments);

		List<MinimumAdsDetailDto> found3RoomApartments = olxApartmentScraper.scrap(olxUrlBuilder.rooms(3), WANTED_KEY_WORDS, UNWANTED_KEY_WORDS,
				lastScrapingDate, scrapingSession);
		allFoundApartments.addAll(found3RoomApartments);

		List<MinimumAdsDetailDto> found4RoomApartments = olxApartmentScraper.scrap(olxUrlBuilder.rooms(4), WANTED_KEY_WORDS, UNWANTED_KEY_WORDS,
				lastScrapingDate, scrapingSession);
		allFoundApartments.addAll(found4RoomApartments);

		return allFoundApartments;
	}

}
