package adsScraper.dto;

import adsScraper.mongo.entities.Advertisment;

public class MinimumAdsDetailDto {
	private String title;
	private Integer price;
	private String url;
	private String keyWord;

	public MinimumAdsDetailDto() {
	}

	public MinimumAdsDetailDto(Advertisment advertisment) {
		title = advertisment.getTitle();
		price = advertisment.getPrice();
		url = advertisment.getUrl();
		keyWord = advertisment.getKeyWord();
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
