package adsScraper.olx;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import adsScraper.mongo.dao.HouseDao;
import adsScraper.mongo.entities.Advertisment;
import adsScraper.mongo.entities.House;
import adsScraper.mongo.entities.ScrapingSession;
import adsScraper.olx.OlxUrlBuilder.RealEstateType;
import adsScraper.util.ParserUtil;

@Stateless
public class OlxHouseScraper extends OlxScraper {

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
	public void addToScrapingSession(ScrapingSession scrapingSession, Advertisment advertisment) {
		advertisment.setScrapingSession(scrapingSession);
		scrapingSession.getHouses().add((House) advertisment);
	}

	@Override
	public void saveAdvertisment(Advertisment advertisment) {
		houseDao.save((House) advertisment);
	}

	@Override
	public Advertisment createAdvertisment() {
		return new House();
	}

	@Override
	public void setAdvertismentAditionalDetails(Advertisment advertisment, Elements detailElements, OlxUrlBuilder olxUrlBuilder) {
		House house = (House) advertisment;

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
