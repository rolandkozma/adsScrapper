package adsScraper.dto;

import adsScraper.mongo.entities.Apartment;

public class MinimumAdsDetailDto {
	private String title;
	private Integer price;
	private String absUrl;
	private String keyWord;

	public MinimumAdsDetailDto() {
	}

	public MinimumAdsDetailDto(Apartment apartment) {
		title = apartment.getTitle();
		price = apartment.getPrice();
		absUrl = apartment.getAbsUrl();
		keyWord = apartment.getKeyWord();
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

	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	@Override
	public String toString() {
		return "MinimumAdsDetailDto [title=" + title + ", price=" + price + ", absUrl=" + absUrl + ", keyWord=" + keyWord + "]";
	}

}
