package adsScraper.mongo.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import adsScraper.olx.OlxUrlBuilder.RealEstateType;

@Entity("scraping_session")
public class ScrapingSession {

	@Id
	private ObjectId id;
	private Date scrapingDate;
	private String type;
	@Reference
	List<Apartment> apartments = new ArrayList<>();
	@Reference
	List<House> houses = new ArrayList<>();
	@Reference
	List<Land> lands = new ArrayList<>();

	public ScrapingSession() {

	}

	public ScrapingSession(Date scrapingDate, RealEstateType realEstateType) {
		this.scrapingDate = scrapingDate;
		this.type = realEstateType.name().toLowerCase();
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Apartment> getApartments() {
		return apartments;
	}

	public void setApartments(List<Apartment> apartments) {
		this.apartments = apartments;
	}

	public List<House> getHouses() {
		return houses;
	}

	public void setHouses(List<House> houses) {
		this.houses = houses;
	}

	public List<Land> getLands() {
		return lands;
	}

	public void setLands(List<Land> lands) {
		this.lands = lands;
	}

	@Override
	public String toString() {
		return "ScrapingSession [id=" + id + ", scrapingDate=" + scrapingDate + ", type=" + type + "]";
	}

}
