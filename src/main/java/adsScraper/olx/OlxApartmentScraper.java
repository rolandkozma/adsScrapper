package adsScraper.olx;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import adsScraper.mongo.dao.ApartmentDao;
import adsScraper.mongo.entities.Advertisment;
import adsScraper.mongo.entities.Apartment;
import adsScraper.mongo.entities.ScrapingSession;
import adsScraper.olx.OlxUrlBuilder.RealEstateType;
import adsScraper.util.ParserUtil;

@Stateless
public class OlxApartmentScraper extends OlxScraper {

	private static final String COMPARTIMENTALIZATION_TEXT = "compartimentare";
	private static final String CONSTRUCTION_PERIOD_TEXT = "constructie";

	@Inject
	private ApartmentDao apartmentDao;

	@Override
	public void setRealEstateType(OlxUrlBuilder olxUrlBuilder) {
		olxUrlBuilder.realEstateType(RealEstateType.APARTMENT);
	}

	@Override
	public String getInfoLog(OlxUrlBuilder olxUrlBuilder) {
		return String.format("scraping %s %s rooms", RealEstateType.APARTMENT, olxUrlBuilder.getRooms());
	}

	@Override
	public void addToScrapingSession(ScrapingSession scrapingSession, Advertisment advertisment) {
		advertisment.setScrapingSession(scrapingSession);
		scrapingSession.getApartments().add((Apartment) advertisment);
	}

	@Override
	public void saveAdvertisment(Advertisment advertisment) {
		apartmentDao.save((Apartment) advertisment);
	}

	@Override
	public Advertisment createAdvertisment() {
		return new Apartment();
	}

	@Override
	public void setAdvertismentAditionalDetails(Advertisment advertisment, Elements detailElements, OlxUrlBuilder olxUrlBuilder) {
		Apartment apartment = (Apartment) advertisment;

		apartment.setRooms(olxUrlBuilder.getRooms());

		for (Element element : detailElements) {
			String elementText = element.ownText().trim().toLowerCase();
			if (elementText.contains(COMPARTIMENTALIZATION_TEXT)) {
				apartment.setCompartimentalization(ParserUtil.getString(element, "a", "compartimentalization"));
			} else if (elementText.contains(CONSTRUCTION_PERIOD_TEXT)) {
				apartment.setConstructionPeriod(ParserUtil.getString(element, "a", "constructionPeriod"));
			}
		}

	}

	@Override
	public String getPageUrl(OlxUrlBuilder olxUrlBuilder) {
		return olxUrlBuilder.getApartmentUrl();
	}

}
