package adsScraper.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.jboss.ejb3.annotation.TransactionTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adsScraper.dto.MinimumAdsDetailDto;
import adsScraper.email.EmailSender;
import adsScraper.mongo.dao.ScrapingSessionDao;
import adsScraper.mongo.entities.ScrapingSession;
import adsScraper.olx.OlxUrlBuilder.RealEstateType;

public abstract class AdvertismentScraper {

	private static final int ONE_HOUR_IN_SECONDS = 3600;

	private static final Logger LOG = LoggerFactory.getLogger(AdvertismentScraper.class);

	@Inject
	private ScrapingSessionDao scrapingSessionDao;

	@Inject
	private EmailSender emailSender;

	private Date currentDate;

	public AdvertismentScraper() {

	}

	@TransactionTimeout(ONE_HOUR_IN_SECONDS)
	public List<MinimumAdsDetailDto> scrap() {
		List<MinimumAdsDetailDto> allAdvertisments = new ArrayList<>();

		currentDate = new Date();
		Date lastScrapingDate = getLastScrapingDate();
		ScrapingSession scrapingSession = createScrapingSession();

		List<MinimumAdsDetailDto> olxAdvertisments = scrapOlx(lastScrapingDate, scrapingSession);
		allAdvertisments.addAll(olxAdvertisments);

		// TODO scrap other sites
		scrapingSessionDao.save(scrapingSession);
		emailSender.sendEmail(allAdvertisments);

		logScrapedRecordsSize(scrapingSession);
		return allAdvertisments;
	}

	abstract void logScrapedRecordsSize(ScrapingSession scrapingSession);

	abstract RealEstateType getScrapingSessionType();

	abstract List<MinimumAdsDetailDto> scrapOlx(Date lastScrapingDate, ScrapingSession scrapingSession);

	Date getLastScrapingDate() {
		Date lastScrapingDate = scrapingSessionDao.getLastScrapingDate(getScrapingSessionType());
		if (lastScrapingDate == null) {
			lastScrapingDate = getYesterdaysDate();
		}
		LOG.debug("lastScrapingDate: {}", lastScrapingDate);
		return lastScrapingDate;
	}

	Date getYesterdaysDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.DATE, -3);
		// calendar.add(Calendar.HOUR, -12);
		return calendar.getTime();
	}

	public ScrapingSession createScrapingSession() {
		ScrapingSession scrapingSession = new ScrapingSession(currentDate, getScrapingSessionType());
		scrapingSessionDao.save(scrapingSession);
		return scrapingSession;
	}

}
