package adsScraper.mongo.entities;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;

public abstract class Advertisment {
	@Id
	private ObjectId id;
	@Reference
	private ScrapingSession scrapingSession;
	private String title;
	private String url;
	private Date publishingDate;
	private String providedBy;
	private String description;
	private Integer price;
	private Integer surface;
	private String userName;
	private Integer referenceNumber;
	private String publishingSite;
	private String keyWord;
	private String business;
	@Transient
	private String phoneUrl;
	private String phone;

	public Advertisment() {

	}

	public ObjectId getId() {
		return id;
	}

	public ScrapingSession getScrapingSession() {
		return scrapingSession;
	}

	public void setScrapingSession(ScrapingSession scrapingSession) {
		this.scrapingSession = scrapingSession;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getPublishingDate() {
		return publishingDate;
	}

	public void setPublishingDate(Date publishingDate) {
		this.publishingDate = publishingDate;
	}

	public String getProvidedBy() {
		return providedBy;
	}

	public void setProvidedBy(String providedBy) {
		this.providedBy = providedBy;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public Integer getSurface() {
		return surface;
	}

	public void setSurface(Integer surface) {
		this.surface = surface;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Integer getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(Integer referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public String getPublishingSite() {
		return publishingSite;
	}

	public void setPublishingSite(String publishingSite) {
		this.publishingSite = publishingSite;
	}

	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	public String getBusiness() {
		return business;
	}

	public void setBusiness(String business) {
		this.business = business;
	}

	public String getPhoneUrl() {
		return phoneUrl;
	}

	public void setPhoneUrl(String phoneUrl) {
		this.phoneUrl = phoneUrl;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public enum Site {
		OLX("olx");

		private final String value;

		private Site(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}
	}

	@Override
	public String toString() {
		return "\n " + scrapingSession + "\n title=" + title + ",\n url=" + url + ",\n publishingDate=" + publishingDate + ",\n providedBy="
				+ providedBy + ",\n surface=" + surface + ",\n description=" + description + ",\n price=" + price + ",\n userName=" + userName
				+ ",\n referenceNumber=" + referenceNumber + ",\n publishingSite=" + publishingSite + ",\n keyWord=" + keyWord + ",\n business="
				+ business + ",\n phone=" + phone;
	}

}
