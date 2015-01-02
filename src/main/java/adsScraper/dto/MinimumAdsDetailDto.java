package adsScraper.dto;

import adsScraper.mongo.entities.Apartment;

public class MinimumAdsDetailDto {
	private String title;
	private Integer price;
	private String url;
	private String keyWord;

	public MinimumAdsDetailDto() {
	}

	public MinimumAdsDetailDto(Apartment apartment) {
		title = apartment.getTitle();
		price = apartment.getPrice();
		url = apartment.getUrl();
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	@Override
	public String toString() {
		return "MinimumAdsDetailDto [title=" + title + ", price=" + price + ", absUrl=" + url + ", keyWord=" + keyWord + "]";
	}

}
