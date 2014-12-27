package adsScraper.olx;

import java.util.Date;

public class Advertisment {

	private String title;
	private String absUrl;
	private Date publishingDate;
	private String providedBy;
	private String compartimentalization;
	private Integer surface;
	private String constructionPeriod;
	private String endowments;
	private String description;
	private Integer price;
	private String phoneNumber;
	private String userName;
	private Integer referenceNumber;

	public Advertisment() {

	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbsUrl() {
		return absUrl;
	}

	public void setAbsUrl(String absUrl) {
		this.absUrl = absUrl;
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

	public String getCompartimentalization() {
		return compartimentalization;
	}

	public void setCompartimentalization(String compartimentalization) {
		this.compartimentalization = compartimentalization;
	}

	public Integer getSurface() {
		return surface;
	}

	public void setSurface(Integer surface) {
		this.surface = surface;
	}

	public String getConstructionPeriod() {
		return constructionPeriod;
	}

	public void setConstructionPeriod(String constructionPeriod) {
		this.constructionPeriod = constructionPeriod;
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

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
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

	public String getEndowments() {
		return endowments;
	}

	public void setEndowments(String endowments) {
		this.endowments = endowments;
	}

	@Override
	public String toString() {
		return "Advertisment [title=" + title + ",\n absUrl=" + absUrl + ",\n publishingDate=" + publishingDate + ",\n providedBy=" + providedBy
				+ ",\n compartimentalization=" + compartimentalization + ",\n surface=" + surface + ",\n constructionPeriod=" + constructionPeriod
				+ ",\n endowments=" + endowments + ",\n description=" + description + ",\n price=" + price + ",\n phoneNumber=" + phoneNumber
				+ ",\n userName=" + userName + ",\n referenceNumber=" + referenceNumber + "]";
	}

}
