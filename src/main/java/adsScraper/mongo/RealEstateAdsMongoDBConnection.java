package adsScraper.mongo;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

public class RealEstateAdsMongoDBConnection {
	private static final RealEstateAdsMongoDBConnection INSTANCE = new RealEstateAdsMongoDBConnection();

	public static final String DB_NAME = "real_estate_ads";
	public static final String HOST = "127.0.0.1";
	public static final int PORT = 27017;

	private final Datastore datastore;

	private RealEstateAdsMongoDBConnection() {
		try {
			MongoClient mongo = new MongoClient(HOST, PORT);
			datastore = new Morphia().mapPackage("adsScraper.mongo.entities").createDatastore(mongo, DB_NAME);
			datastore.ensureIndexes();
		} catch (Exception e) {
			throw new RuntimeException("Error initializing RealEstateAdsMongoDBConnection", e);
		}
	}

	public static RealEstateAdsMongoDBConnection instance() {
		return INSTANCE;
	}

	public Datastore getDatabase() {
		return datastore;
	}
}
