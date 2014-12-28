package adsScraper.mongo.dao;

import java.util.Date;

import adsScraper.mongo.entities.Advertisment;

public class AdvertismentDao extends RealEstateAdsBasicDao<Advertisment, String> {

	public AdvertismentDao() {
		super(Advertisment.class);
	}

	public Date getLastRunDate() {
		Date lastRunDate = null;
		Advertisment ad = createQuery().order("-scrapingDate").limit(1).get();
		if (ad != null) {
			lastRunDate = ad.getScrapingDate();
		}
		return lastRunDate;
	}
}
