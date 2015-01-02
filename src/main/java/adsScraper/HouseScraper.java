package adsScraper;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adsScraper.dto.MinimumAdsDetailDto;
import adsScraper.mongo.entities.ScrapingSession;
import adsScraper.olx.OlxHouseScraper;
import adsScraper.olx.OlxUrlBuilder;
import adsScraper.olx.OlxUrlBuilder.Business;
import adsScraper.olx.OlxUrlBuilder.City;
import adsScraper.olx.OlxUrlBuilder.Order;
import adsScraper.olx.OlxUrlBuilder.RealEstateType;

@Stateless
public class HouseScraper extends AdvertismentScraper {

	private static final Logger LOG = LoggerFactory.getLogger(HouseScraper.class);

	@Inject
	private OlxHouseScraper olxHouseScraper;

	private static final List<String> WANTED_KEY_WORDS = Arrays.asList();

	private static final List<String> UNWANTED_KEY_WORDS = Arrays.asList();

	@Override
	RealEstateType getScrapingSessionType() {
		return RealEstateType.HOUSE;
	}

	@Override
	void logScrapedRecordsSize(ScrapingSession scrapingSession) {
		LOG.info("This scraping session has scraped {} records!", scrapingSession.getHouses().size());
	}

	@Override
	List<MinimumAdsDetailDto> scrapOlx(Date lastScrapingDate, ScrapingSession scrapingSession) {

		OlxUrlBuilder olxUrlBuilder = new OlxUrlBuilder().city(City.CLUJ_NAPOCA).business(Business.PRIVATE).orderBy(Order.DATE);

		List<MinimumAdsDetailDto> foundHouses = olxHouseScraper.scrap(olxUrlBuilder, WANTED_KEY_WORDS, UNWANTED_KEY_WORDS, lastScrapingDate,
				scrapingSession);

		return foundHouses;
	}

}
