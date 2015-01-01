package adsScraper.mongo.dao;

import java.util.Date;

import org.bson.types.ObjectId;

import adsScraper.mongo.entities.ScrapingSession;

public class ScrapingSessionDao extends RealEstateAdsBasicDao<ScrapingSession, ObjectId> {

	public ScrapingSessionDao() {
		super(ScrapingSession.class);
	}

	public Date getLastScrapingDate() {
		Date lastRunDate = null;
		ScrapingSession scrapingSession = createQuery().order("-scrapingDate").limit(1).get();
		if (scrapingSession != null) {
			System.out.println(scrapingSession.getApartments().size());
			lastRunDate = scrapingSession.getScrapingDate();
		}
		return lastRunDate;
	}
}
