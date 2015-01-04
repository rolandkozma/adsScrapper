package adsScraper.olx;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import adsScraper.mongo.dao.LandDao;
import adsScraper.mongo.entities.Advertisment;
import adsScraper.mongo.entities.Land;
import adsScraper.mongo.entities.ScrapingSession;
import adsScraper.olx.OlxUrlBuilder.RealEstateType;
import adsScraper.util.ParserUtil;

@Stateless
public class OlxLandScraper extends OlxScraper {

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
	public void addToScrapingSession(ScrapingSession scrapingSession, Advertisment advertisment) {
		advertisment.setScrapingSession(scrapingSession);
		scrapingSession.getLands().add((Land) advertisment);
	}

	@Override
	public void saveAdvertisment(Advertisment advertisment) {
		landDao.save((Land) advertisment);
	}

	@Override
	public Advertisment createAdvertisment() {
		return new Land();
	}

	@Override
	public void setAdvertismentAditionalDetails(Advertisment advertisment, Elements detailElements, OlxUrlBuilder olxUrlBuilder) {
		Land land = (Land) advertisment;

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
