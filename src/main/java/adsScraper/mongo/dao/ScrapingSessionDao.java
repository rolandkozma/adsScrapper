package adsScraper.mongo.dao;

import java.util.Date;

import org.bson.types.ObjectId;

import adsScraper.mongo.entities.ScrapingSession;
import adsScraper.olx.OlxUrlBuilder.RealEstateType;

public class ScrapingSessionDao extends RealEstateAdsBasicDao<ScrapingSession, ObjectId> {

	public ScrapingSessionDao() {
		super(ScrapingSession.class);
	}

	public Date getLastScrapingDate(RealEstateType realEstateType) {
		Date lastScrapingDate = null;
		ScrapingSession scrapingSession = createQuery().field("type").equal(realEstateType.name().toLowerCase()).order("-scrapingDate").limit(1)
				.get();
		if (scrapingSession != null) {
			lastScrapingDate = scrapingSession.getScrapingDate();
		}
		return lastScrapingDate;
	}
}
