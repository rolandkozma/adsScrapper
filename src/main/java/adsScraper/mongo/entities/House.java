package adsScraper.mongo.entities;

import org.mongodb.morphia.annotations.Entity;

@Entity("house")
public class House extends Advertisment {

	private String endowments;
	private String rooms;

	public House() {

	}

	public String getEndowments() {
		return endowments;
	}

	public void setEndowments(String endowments) {
		this.endowments = endowments;
	}

	public String getRooms() {
		return rooms;
	}

	public void setRooms(String rooms) {
		this.rooms = rooms;
	}

	@Override
	public String toString() {
		return "House [" + super.toString() + ",\n endowments=" + endowments + ",\n rooms=" + rooms + "]";
	}

}
