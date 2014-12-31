package adsScraper.mongo.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity("scraping_session")
public class ScrapingSession {

	@Id
	private ObjectId id;
	private Date scrapingDate;
	@Reference
	List<Apartment> apartments = new ArrayList<>();

	public ScrapingSession() {

	}

	public ScrapingSession(Date scrapingDate) {
		this.scrapingDate = scrapingDate;
	}

	public ObjectId getId() {
		return id;
	}

	public Date getScrapingDate() {
		return scrapingDate;
	}
	public void setScrapingDate(Date scrapingDate) {
		this.scrapingDate = scrapingDate;
	}

	public List<Apartment> getApartments() {
		return apartments;
	}

	public void setApartments(List<Apartment> apartments) {
		this.apartments = apartments;
	}

	@Override
	public String toString() {
		return "ScrapingSession [id=" + id + ", scrapingDate=" + scrapingDate + "]";
	}

}
