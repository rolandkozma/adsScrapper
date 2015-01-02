package adsScraper.mongo.dao;

import org.bson.types.ObjectId;

import adsScraper.mongo.entities.House;

public class HouseDao extends RealEstateAdsBasicDao<House, ObjectId> {

	public HouseDao() {
		super(House.class);
	}

}
