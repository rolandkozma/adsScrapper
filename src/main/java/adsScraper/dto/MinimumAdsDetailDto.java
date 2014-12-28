package adsScraper.dto;

import adsScraper.mongo.entities.Advertisment;

public class MinimumAdsDetailDto {
	private String title;
	private Integer price;
	private String absUrl;

	public MinimumAdsDetailDto() {
	}

	public MinimumAdsDetailDto(Advertisment advertisment) {
		title = advertisment.getTitle();
		price = advertisment.getPrice();
		absUrl = advertisment.getAbsUrl();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public String getAbsUrl() {
		return absUrl;
	}

	public void setAbsUrl(String absUrl) {
		this.absUrl = absUrl;
	}

	@Override
	public String toString() {
		return "MinimumAdsDetailDto [title=" + title + ", price=" + price + ", absUrl=" + absUrl + "]";
	}

}
