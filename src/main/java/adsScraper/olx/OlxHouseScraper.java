package adsScraper.olx;
import javax.inject.Inject;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import adsScraper.mongo.dao.HouseDao;
import adsScraper.mongo.entities.House;
import adsScraper.mongo.entities.ScrapingSession;
import adsScraper.olx.OlxUrlBuilder.RealEstateType;
import adsScraper.util.ParserUtil;

public class OlxHouseScraper extends OlxScraper<House> {

	private static final String ENDOWMENTS_TEXT = "locuinta";
	private static final String ROOMS_TEXT = "camere";

	@Inject
	private HouseDao houseDao;

	@Override
	public void setRealEstateType(OlxUrlBuilder olxUrlBuilder) {
		olxUrlBuilder.realEstateType(RealEstateType.HOUSE);
	}

	@Override
	public String getInfoLog(OlxUrlBuilder olxUrlBuilder) {
		return String.format("scraping %s", RealEstateType.HOUSE);
	}

	@Override
	public void addToScrapingSession(ScrapingSession scrapingSession, House house) {
		house.setScrapingSession(scrapingSession);
		scrapingSession.getHouses().add(house);
	}

	@Override
	public void saveAdvertisment(House house) {
		houseDao.save(house);
	}

	@Override
	public House createAdvertisment() {
		return new House();
	}

	@Override
	public void setAdvertismentAditionalDetails(House house, Elements detailElements, OlxUrlBuilder olxUrlBuilder) {
		for (Element element : detailElements) {
			String elementText = element.ownText().trim().toLowerCase();
			if (elementText.contains(ENDOWMENTS_TEXT)) {
				house.setEndowments(ParserUtil.getString(element, "a", "endowments"));
			} else if (elementText.contains(ROOMS_TEXT)) {
				house.setRooms(ParserUtil.getString(element, "a", "rooms"));
			}
		}
	}

	@Override
	public String getPageUrl(OlxUrlBuilder olxUrlBuilder) {
		return olxUrlBuilder.getHouseUrl();
	}

}
