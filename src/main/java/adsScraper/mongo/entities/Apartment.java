package adsScraper.mongo.entities;

import org.mongodb.morphia.annotations.Entity;

@Entity("apartment")
public class Apartment extends Advertisment {

	private String compartimentalization;
	private String constructionPeriod;
	private Integer rooms;

	public Apartment() {

	}

	public String getCompartimentalization() {
		return compartimentalization;
	}

	public void setCompartimentalization(String compartimentalization) {
		this.compartimentalization = compartimentalization;
	}

	public String getConstructionPeriod() {
		return constructionPeriod;
	}

	public void setConstructionPeriod(String constructionPeriod) {
		this.constructionPeriod = constructionPeriod;
	}

	public Integer getRooms() {
		return rooms;
	}

	public void setRooms(Integer rooms) {
		this.rooms = rooms;
	}

	@Override
	public String toString() {
		return "Apartment [" + super.toString() + ",\n compartimentalization=" + compartimentalization + ",\n constructionPeriod="
				+ constructionPeriod + ",\n rooms=" + rooms + "]";
	}

}
