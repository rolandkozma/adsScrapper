package adsScraper.olx;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adsScraper.dto.MinimumAdsDetailDto;
import adsScraper.mongo.dao.ApartmentDao;
import adsScraper.mongo.entities.Apartment;
import adsScraper.mongo.entities.ScrapingSession;
import adsScraper.util.ParserUtil;

@Stateless
public class OlxScraper {
	private static final Logger LOG = LoggerFactory.getLogger(OlxScraper.class);

	private static final String REFERENCE_NUMBER_SELECTOR = "div.offerheadinner p small span span span.rel";
	private static final String PRICE_SELECTOR = "div#offeractions div div div.pricelabel strong";
	private static final String USER_NAME_SELECTOR = "div#offeractions div.userbox p.userdetails span.block:nth-child(1)";
	private static final String PHONE_NUMBER_SELECTOR = "ul#contact_methods li.link-phone div.contactitem strong";
	private static final String DESCRIPTION_SELECTOR = "div#textContent p";
	private static final String PUBLISHING_DATE_SELECTOR = "div.offerheadinner p small span";
	private static final String TABLE_DETAILS_DIV_SELECTOR = "div.descriptioncontent table.details tr td div";

	private static final Locale LOCALE = new Locale("RO");
	private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, LOCALE);
	private static final Pattern DATE_PATTERN = Pattern.compile("\\d{1,2} \\w* \\d{4}");
	private static final Pattern TIME_PATTERN = Pattern.compile("\\d{1,2}:\\d{1,2}");

	private static final String PROVIDED_BY_TEXT = "oferit";
	private static final String COMPARTIMENTALIZATION_TEXT = "compartimentare";
	private static final String SURFACE_TEXT = "suprafata";
	private static final String CONSTRUCTION_PERIOD_TEXT = "constructie";
	private static final String ENDOWMENTS_TEXT = "locuinta";

	private static final int PAUSE_TIME = 5000;
	private static final String OWNER = "proprietar";

	@Inject
	private ApartmentDao apartmentDao;

	public List<MinimumAdsDetailDto> scrap(OlxUrlBuilder olxUrlBuilder, List<String> keyWords, Date lastScrapingDate, ScrapingSession scrapingSession) {

		List<MinimumAdsDetailDto> allFoundApartments = new ArrayList<MinimumAdsDetailDto>();

		List<MinimumAdsDetailDto> minimumAdsDetailDtos = scrap(olxUrlBuilder, keyWords, lastScrapingDate, scrapingSession, false);
		allFoundApartments.addAll(minimumAdsDetailDtos);

		List<MinimumAdsDetailDto> promotedMinimumAdsDetailDtos = scrap(olxUrlBuilder, keyWords, lastScrapingDate, scrapingSession, true);
		allFoundApartments.addAll(promotedMinimumAdsDetailDtos);

		return allFoundApartments;
	}

	private List<MinimumAdsDetailDto> scrap(OlxUrlBuilder olxUrlBuilder, List<String> keyWords, Date lastScrapingDate,
			ScrapingSession scrapingSession, boolean isPromoted) {
		boolean parseNextPage = true;
		int pageNumber = 0;

		List<MinimumAdsDetailDto> minimumAdsDetailDtos = new ArrayList<MinimumAdsDetailDto>();
		String infoLog = String.format("scraping %s %s %s rooms", isPromoted ? "promoted" : "", olxUrlBuilder.getHouseType(),
				olxUrlBuilder.getRooms());
		LOG.info("--------------------- start {} -----------------------\n", infoLog);
		while (parseNextPage) {
			pageNumber++;
			String url = olxUrlBuilder.page(pageNumber).getUrl();
			Elements links = getAdLinks(url, isPromoted);
			LOG.debug("got: {}", links.size());

			for (Element link : links) {
				Apartment apartment = createApartment(link, olxUrlBuilder, scrapingSession, isPromoted);

				if ((apartment.getPublishingDate() != null) && apartment.getPublishingDate().before(lastScrapingDate)) {
					parseNextPage = false;
					break;
				}

				if (isPublishedByOwner(apartment) && hasKeyWords(apartment, keyWords)) {
					minimumAdsDetailDtos.add(new MinimumAdsDetailDto(apartment));
					scrapingSession.getApartments().add(apartment);
					apartmentDao.save(apartment);
				}

				LOG.info(apartment.toString());
				LOG.info("---------------------------------------------------------------------------------------------------\n");
				pause(PAUSE_TIME);
			}
		}
		LOG.info("--------------------- end {} -----------------------\n", infoLog);
		return minimumAdsDetailDtos;
	}

	private boolean isPublishedByOwner(Apartment apartment) {
		return OWNER.equalsIgnoreCase(apartment.getProvidedBy());
	}

	public boolean hasKeyWords(Apartment apartment, List<String> keyWords) {
		boolean hasKeyWords = false;
		if (apartment.getDescription() != null) {
			String lowerCaseDescription = apartment.getDescription().toLowerCase();
			for (String key : keyWords) {
				if (lowerCaseDescription.contains(key.toLowerCase())) {
					hasKeyWords = true;
					apartment.setKeyWord(key);
				}
			}
		}
		return hasKeyWords;
	}

	private Elements getAdLinks(String url, boolean isPromoted) {
		Elements links = new Elements();
		Document doc = getDocument(url);
		if (doc != null) {
			links = doc.select("a[href]").select(".marginright5").select(".link.linkWithHash")
					.select(isPromoted ? ".detailsLinkPromoted" : ".detailsLink");
		}
		return links;
	}

	private Apartment createApartment(Element link, OlxUrlBuilder olxUrlBuilder, ScrapingSession scrapingSession, boolean isPromoted) {
		Apartment apartment = new Apartment();
		apartment.setScrapingSession(scrapingSession);
		apartment.setPublishingSite(Apartment.Site.OLX.value());
		apartment.setTitle(link.text());
		apartment.setAbsUrl(link.absUrl("href"));

		Document adsDetail = getDocument(apartment.getAbsUrl());
		if (adsDetail != null) {
			apartment.setIsPromoted(isPromoted);
			apartment.setBusiness(olxUrlBuilder.getBusiness().value());
			apartment.setRooms(olxUrlBuilder.getRooms());
			apartment.setPublishingDate(getPublishingDate(adsDetail, PUBLISHING_DATE_SELECTOR, "publishingDate"));
			apartment.setReferenceNumber(ParserUtil.getInteger(adsDetail, REFERENCE_NUMBER_SELECTOR, "referenceNumber"));
			apartment.setDescription(ParserUtil.getString(adsDetail, DESCRIPTION_SELECTOR, "description"));
			apartment.setPrice(ParserUtil.getInteger(adsDetail, PRICE_SELECTOR, "price"));
			apartment.setPhoneNumber(ParserUtil.getString(adsDetail, PHONE_NUMBER_SELECTOR, "phoneNumber"));
			apartment.setUserName(ParserUtil.getString(adsDetail, USER_NAME_SELECTOR, "userName"));

			Elements elements = adsDetail.select(TABLE_DETAILS_DIV_SELECTOR);
			for (Element element : elements) {
				String elementText = element.ownText().trim().toLowerCase();
				if (elementText.contains(PROVIDED_BY_TEXT)) {
					apartment.setProvidedBy(ParserUtil.getString(element, "a", "providedBy"));
				} else if (elementText.contains(COMPARTIMENTALIZATION_TEXT)) {
					apartment.setCompartimentalization(ParserUtil.getString(element, "a", "compartimentalization"));
				} else if (elementText.contains(SURFACE_TEXT)) {
					apartment.setSurface(ParserUtil.getInteger(element, "strong", "surface"));
				} else if (elementText.contains(CONSTRUCTION_PERIOD_TEXT)) {
					apartment.setConstructionPeriod(ParserUtil.getString(element, "a", "constructionPeriod"));
				} else if (elementText.contains(ENDOWMENTS_TEXT)) {
					apartment.setEndowments(ParserUtil.getString(element, "a", "endowments"));
				}
			}
		}
		return apartment;
	}

	private void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			LOG.warn(e.getMessage(), e);
		}
	}

	private Date getPublishingDate(Document adsDetail, String selector, String fieldName) {
		Date publishingDate = null;
		String publishingDateText = ParserUtil.getString(adsDetail, selector, fieldName);

		if (publishingDateText != null) {
			publishingDateText = publishingDateText.toLowerCase();

			String time = getTime(publishingDateText);
			String date = getDate(publishingDateText);
			String formatedDateTime = String.format("%s %s", date, time);

			try {
				publishingDate = DATE_FORMAT.parse(formatedDateTime);
			} catch (ParseException e) {
				LOG.warn(String.format("Faled to parse date from string: %s", formatedDateTime), e);
			}
		}

		LOG.debug("publishingDate: {}", publishingDate);
		if (publishingDate == null) {
			LOG.error("Failed to get publishingDate! ");
		}
		return publishingDate;
	}

	private String getDate(String dateText) {
		String date = null;
		Matcher matcher = DATE_PATTERN.matcher(dateText);
		if (matcher.find()) {
			date = matcher.group();
		} else {
			LOG.warn("Failed to parse date from {}", dateText);
		}
		return date;
	}

	private String getTime(String dateText) {
		String time = null;
		Matcher matcher = TIME_PATTERN.matcher(dateText);
		if (matcher.find()) {
			time = matcher.group();
		} else {
			LOG.warn("Failed to parse time from {}", dateText);
		}
		return time;
	}

	private Document getDocument(String url) {
		Document doc = null;
		int retriesNr = 3;
		int i = 0;
		while ((doc == null) && (i < retriesNr)) {
			try {
				doc = Jsoup.connect(url).get();
			} catch (Exception e) {
				LOG.warn(String.format("Failed to get document! url: %s", url), e);
				pause(PAUSE_TIME);
			}
			i++;
		}
		return doc;
	}

}
