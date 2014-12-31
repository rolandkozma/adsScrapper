package adsScraper.mongo.dao;

import org.bson.types.ObjectId;

import adsScraper.mongo.entities.Apartment;

public class ApartmentDao extends RealEstateAdsBasicDao<Apartment, ObjectId> {

	public ApartmentDao() {
		super(Apartment.class);
	}

}
