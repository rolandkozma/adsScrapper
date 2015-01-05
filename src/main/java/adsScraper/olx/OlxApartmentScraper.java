package adsScraper.olx;
import java.util.List;

import javax.inject.Inject;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adsScraper.mongo.dao.ApartmentDao;
import adsScraper.mongo.entities.Apartment;
import adsScraper.mongo.entities.ScrapingSession;
import adsScraper.olx.OlxUrlBuilder.RealEstateType;
import adsScraper.util.ParserUtil;

public class OlxApartmentScraper extends OlxScraper<Apartment> {
	private static final Logger LOG = LoggerFactory.getLogger(OlxApartmentScraper.class);

	private static final String RAILROAD_COMPARTIMENTALIZATION = "semidecomandat";
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
	public void addToScrapingSession(ScrapingSession scrapingSession, Apartment apartment) {
		apartment.setScrapingSession(scrapingSession);
		scrapingSession.getApartments().add(apartment);
	}

	@Override
	public void saveAdvertisment(Apartment apartment) {
		apartmentDao.save(apartment);
	}

	@Override
	public Apartment createAdvertisment() {
		return new Apartment();
	}

	@Override
	public void setAdvertismentDetails(Apartment apartment, Elements detailElements, OlxUrlBuilder olxUrlBuilder) {
		super.setAdvertismentDetails(apartment, detailElements, olxUrlBuilder);

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

	@Override
	public boolean isRelevant(Apartment apartment, List<String> wantedKeyWords, List<String> unwantedKeyWords) {
		return !isRailroad(apartment) && super.isRelevant(apartment, wantedKeyWords, unwantedKeyWords);
	}

	private boolean isRailroad(Apartment apartment) {
		boolean isRailroad = RAILROAD_COMPARTIMENTALIZATION.equalsIgnoreCase(apartment.getCompartimentalization());
		if (isRailroad) {
			LOG.info("E {}", RAILROAD_COMPARTIMENTALIZATION);
		}
		return isRailroad;
	}

}
