package adsScraper.mongo.entities;

import org.mongodb.morphia.annotations.Entity;

@Entity("land")
public class Land extends Advertisment {

	private String location;

	public Land() {

	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "Land [" + super.toString() + ",\n location=" + location + "]";
	}

}
