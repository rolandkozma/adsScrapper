package adsScraper.olx;
import javax.inject.Inject;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import adsScraper.mongo.dao.LandDao;
import adsScraper.mongo.entities.Land;
import adsScraper.mongo.entities.ScrapingSession;
import adsScraper.olx.OlxUrlBuilder.RealEstateType;
import adsScraper.util.ParserUtil;

public class OlxLandScraper extends OlxScraper<Land> {

	private static final String LOCATION_TEXT = "extravilan";

	@Inject
	private LandDao landDao;

	@Override
	public void setRealEstateType(OlxUrlBuilder olxUrlBuilder) {
		olxUrlBuilder.realEstateType(RealEstateType.LAND);
	}

	@Override
	public String getInfoLog(OlxUrlBuilder olxUrlBuilder) {
		return String.format("scraping %s", RealEstateType.LAND);
	}

	@Override
	public void addToScrapingSession(ScrapingSession scrapingSession, Land land) {
		land.setScrapingSession(scrapingSession);
		scrapingSession.getLands().add(land);
	}

	@Override
	public void saveAdvertisment(Land land) {
		landDao.save(land);
	}

	@Override
	public Land createAdvertisment() {
		return new Land();
	}

	@Override
	public void setAdvertismentAditionalDetails(Land land, Elements detailElements, OlxUrlBuilder olxUrlBuilder) {
		for (Element element : detailElements) {
			String elementText = element.ownText().trim().toLowerCase();
			if (elementText.contains(LOCATION_TEXT)) {
				land.setLocation(ParserUtil.getString(element, "a", "location"));
			}
		}
	}

	@Override
	public String getPageUrl(OlxUrlBuilder olxUrlBuilder) {
		return olxUrlBuilder.getLandUrl();
	}

}
