package adsScraper.mongo.dao;

import java.util.Date;

import adsScraper.mongo.entities.Apartment;

public class ApartmentDao extends RealEstateAdsBasicDao<Apartment, String> {

	public ApartmentDao() {
		super(Apartment.class);
	}

	public Date getLastRunDate(Integer rooms, boolean isPromoted) {
		Date lastRunDate = null;
		Apartment ad = createQuery().field("rooms").equal(rooms).field("isPromoted").equal(isPromoted).order("-scrapingDate").limit(1).get();
		if (ad != null) {
			lastRunDate = ad.getScrapingDate();
		}
		return lastRunDate;
	}
}
