package adsScraper.mongo.dao;

import org.bson.types.ObjectId;

import adsScraper.mongo.entities.Land;

public class LandDao extends RealEstateAdsBasicDao<Land, ObjectId> {

	public LandDao() {
		super(Land.class);
	}

}
