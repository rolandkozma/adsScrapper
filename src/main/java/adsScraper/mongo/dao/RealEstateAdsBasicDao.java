package adsScraper.mongo.dao;

import org.mongodb.morphia.dao.BasicDAO;

import adsScraper.mongo.RealEstateAdsMongoDBConnection;

public class RealEstateAdsBasicDao<T, K> extends BasicDAO<T, K> {

	public RealEstateAdsBasicDao(Class<T> entityClass) {
		super(entityClass, RealEstateAdsMongoDBConnection.instance().getDatabase());
	}

}
