package adsScraper.mongo.dao;

import adsScraper.mongo.entities.Advertisment;

public class AdvertismentDao extends RealEstateAdsBasicDao<Advertisment, String> {

	public AdvertismentDao() {
		super(Advertisment.class);
	}

}
