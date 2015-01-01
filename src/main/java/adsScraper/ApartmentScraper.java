package adsScraper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adsScraper.dto.MinimumAdsDetailDto;
import adsScraper.email.EmailSender;
import adsScraper.mongo.dao.ScrapingSessionDao;
import adsScraper.mongo.entities.ScrapingSession;
import adsScraper.olx.OlxScraper;
import adsScraper.olx.OlxUrlBuilder;
import adsScraper.olx.OlxUrlBuilder.Business;
import adsScraper.olx.OlxUrlBuilder.City;
import adsScraper.olx.OlxUrlBuilder.HouseType;
import adsScraper.olx.OlxUrlBuilder.Order;

@Stateless
public class ApartmentScraper {

	private static final Logger LOG = LoggerFactory.getLogger(ApartmentScraper.class);
	private static final Locale LOCALE = new Locale("RO");

	@Inject
	private OlxScraper olxScraper;

	@Inject
	private EmailSender mailSender;

	@Inject
	private ScrapingSessionDao scrapingSessionDao;

	private Date currentDate;

	private static final List<String> WANTED_KEY_WORDS = Arrays.asList("babes", "bisericii ortodoxe", "botanica", "brd", "centru", "central ",
			"central.", "central,", "central;", "central!", "cipariu", "dorobantilor", "garibaldi", "godeanu", "mihai viteazu", "nasaud",
			"observator", "opera", "parc ", "parc,", "parc.", "parc;", "parc!", "persoana fizica", "p f", "p. f.", "pf ", "plopilor", "recuperare",
			"regionala", "republicii", "scortarilor", "sigma", "sporturilor", "teatru", "titulescu", "vlahuta", "zorilor", "13 septembrie");

	private static final List<String> UNWANTED_KEY_WORDS = Arrays.asList("manastur", "floresti", "gilau", "baciu", "bulgaria", "iris", "apahid",
			"turda", "someseni");

	// @Schedule(hour = "*/4", persistent = true)
	public List<MinimumAdsDetailDto> scrap() {
		List<MinimumAdsDetailDto> allApartments = new ArrayList<>();

		currentDate = new Date();
		Date lastScrapingDate = getLastScrapingDate();
		ScrapingSession scrapingSession = createScrapingSession();

		List<MinimumAdsDetailDto> olxApartments = scrapOlx(lastScrapingDate, scrapingSession);
		allApartments.addAll(olxApartments);

		// TODO scrap apartments from other sites
		scrapingSessionDao.save(scrapingSession);
		mailSender.sendEmail(allApartments);

		LOG.info("This scraping session has scraped {} records!", scrapingSession.getApartments().size());
		return allApartments;
	}

	public ScrapingSession createScrapingSession() {
		ScrapingSession scrapingSession = new ScrapingSession(currentDate, HouseType.APARTMENT.name().toLowerCase());
		scrapingSessionDao.save(scrapingSession);
		return scrapingSession;
	}

	private List<MinimumAdsDetailDto> scrapOlx(Date lastScrapingDate, ScrapingSession scrapingSession) {
		List<MinimumAdsDetailDto> allFoundApartments = new ArrayList<MinimumAdsDetailDto>();

		OlxUrlBuilder olxUrlBuilder = new OlxUrlBuilder().city(City.CLUJ_NAPOCA).business(Business.PRIVATE).houseType(HouseType.APARTMENT)
				.orderBy(Order.DATE);

		List<MinimumAdsDetailDto> found2RoomApartments = olxScraper.scrap(olxUrlBuilder.rooms(2), WANTED_KEY_WORDS, UNWANTED_KEY_WORDS,
				lastScrapingDate, scrapingSession);
		allFoundApartments.addAll(found2RoomApartments);

		List<MinimumAdsDetailDto> found3RoomApartments = olxScraper.scrap(olxUrlBuilder.rooms(3), WANTED_KEY_WORDS, UNWANTED_KEY_WORDS,
				lastScrapingDate, scrapingSession);
		allFoundApartments.addAll(found3RoomApartments);

		List<MinimumAdsDetailDto> found4RoomApartments = olxScraper.scrap(olxUrlBuilder.rooms(4), WANTED_KEY_WORDS, UNWANTED_KEY_WORDS,
				lastScrapingDate, scrapingSession);
		allFoundApartments.addAll(found4RoomApartments);

		return allFoundApartments;
	}

	private Date getLastScrapingDate() {
		Date lastScrapingDate = scrapingSessionDao.getLastScrapingDate();
		if (lastScrapingDate == null) {
			lastScrapingDate = getYesterdaysDate();
		}
		LOG.debug("lastScrapingDate: {}", lastScrapingDate);
		return lastScrapingDate;
	}

	private Date getYesterdaysDate() {
		Calendar calendar = Calendar.getInstance(LOCALE);
		calendar.setTime(currentDate);
		// calendar.add(Calendar.DATE, -1);
		calendar.add(Calendar.HOUR, -2);
		return calendar.getTime();
	}

}
