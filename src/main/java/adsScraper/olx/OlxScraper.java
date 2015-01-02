package adsScraper.olx;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adsScraper.dto.MinimumAdsDetailDto;
import adsScraper.mongo.entities.Advertisment;
import adsScraper.mongo.entities.Apartment;
import adsScraper.mongo.entities.ScrapingSession;
import adsScraper.util.ParserUtil;

public abstract class OlxScraper {
	private static final Logger LOG = LoggerFactory.getLogger(OlxScraper.class);

	// private static final String PHONE_NUMBER_SELECTOR = "ul#contact_methods li.link-phone div.contactitem strong";
	private static final String REFERENCE_NUMBER_SELECTOR = "div.offerheadinner p small span span span.rel";
	private static final String PRICE_SELECTOR = "div#offeractions div div div.pricelabel strong";
	private static final String USER_NAME_SELECTOR = "div#offeractions div.userbox p.userdetails span.block:nth-child(1)";
	private static final String DESCRIPTION_SELECTOR = "div#textContent p";
	private static final String PUBLISHING_DATE_SELECTOR = "div.offerheadinner p small span";
	private static final String TABLE_DETAILS_DIV_SELECTOR = "div.descriptioncontent table.details tr td div";
	private static final String PROVIDED_BY_TEXT = "oferit";
	private static final String SURFACE_TEXT = "suprafata";

	private static final Locale LOCALE = new Locale("RO");
	private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, LOCALE);
	private static final Pattern DATE_PATTERN = Pattern.compile("\\d{1,2} \\w* \\d{4}");
	private static final Pattern TIME_PATTERN = Pattern.compile("\\d{1,2}:\\d{1,2}");

	private static final int PAUSE_TIME = 5000;
	private static final String OWNER = "proprietar";

	public List<MinimumAdsDetailDto> scrap(OlxUrlBuilder olxUrlBuilder, List<String> wantedKeyWords, List<String> unwantedKeyWords,
			Date lastScrapingDate, ScrapingSession scrapingSession) {
		setRealEstateType(olxUrlBuilder);
		List<MinimumAdsDetailDto> minimumAdsDetailDtos = new ArrayList<MinimumAdsDetailDto>();
		boolean parseNextPage = true;
		int pageNumber = 0;

		String infoLog = getInfoLog(olxUrlBuilder);
		LOG.info("--------------------- start {} -----------------------\n", infoLog);
		while (parseNextPage) {
			pageNumber++;
			String pageUrl = olxUrlBuilder.page(pageNumber).getUrl();
			Elements links = getAdvertismentLinks(pageUrl);

			for (Element link : links) {
				Advertisment advertisment = createAdvertisment(link, olxUrlBuilder);

				if (isPublishedBeforeLastScrapingDate(advertisment, lastScrapingDate)) {
					parseNextPage = false;
					break;
				}

				if (isRelevant(advertisment, wantedKeyWords, unwantedKeyWords)) {
					addToScrapingSession(scrapingSession, advertisment);

					String phoneNumber = getPhoneNumber(advertisment.getUrl());
					advertisment.setPhoneNumber(phoneNumber);

					saveAdvertisment(advertisment);
					minimumAdsDetailDtos.add(new MinimumAdsDetailDto(advertisment));
					LOG.info(advertisment.toString());
				}

				LOG.info("---------------------------------------------------------------------------------------------------\n");
				pause(PAUSE_TIME);
			}
		}
		LOG.info("--------------------- end {} -----------------------\n", infoLog);
		return minimumAdsDetailDtos;
	}

	public abstract String getInfoLog(OlxUrlBuilder olxUrlBuilder);

	public abstract void setRealEstateType(OlxUrlBuilder olxUrlBuilder);

	private Elements getAdvertismentLinks(String url) {
		Elements links = new Elements();
		Document doc = getDocument(url);
		if (doc != null) {
			links = doc.select("table#offers_table a[href]").select(".marginright5").select(".link.linkWithHash");
		}
		return links;
	}

	private Advertisment createAdvertisment(Element link, OlxUrlBuilder olxUrlBuilder) {
		Advertisment advertisment = createAdvertisment();
		advertisment.setPublishingSite(Apartment.Site.OLX.value());
		advertisment.setTitle(link.text());
		advertisment.setUrl(link.absUrl("href"));

		Document advertismentPage = getDocument(advertisment.getUrl());
		if (advertismentPage != null) {
			advertisment.setBusiness(olxUrlBuilder.getBusiness().value());
			advertisment.setPublishingDate(getPublishingDate(advertismentPage, PUBLISHING_DATE_SELECTOR, "publishingDate"));
			advertisment.setReferenceNumber(ParserUtil.getInteger(advertismentPage, REFERENCE_NUMBER_SELECTOR, "referenceNumber"));
			advertisment.setDescription(ParserUtil.getString(advertismentPage, DESCRIPTION_SELECTOR, "description"));
			advertisment.setPrice(ParserUtil.getInteger(advertismentPage, PRICE_SELECTOR, "price"));
			advertisment.setUserName(ParserUtil.getString(advertismentPage, USER_NAME_SELECTOR, "userName"));

			Elements detailElements = advertismentPage.select(TABLE_DETAILS_DIV_SELECTOR);
			setAdvertismentDetails(advertisment, detailElements, olxUrlBuilder);
		}
		return advertisment;
	}

	public abstract Advertisment createAdvertisment();

	private void setAdvertismentDetails(Advertisment advertisment, Elements detailElements, OlxUrlBuilder olxUrlBuilder) {
		for (Element element : detailElements) {
			String elementText = element.ownText().trim().toLowerCase();
			if (elementText.contains(PROVIDED_BY_TEXT)) {
				advertisment.setProvidedBy(ParserUtil.getString(element, "a", "providedBy"));
			} else if (elementText.contains(SURFACE_TEXT)) {
				advertisment.setSurface(ParserUtil.getInteger(element, "strong", "surface"));
			}
		}

		setAdvertismentAditionalDetails(advertisment, detailElements, olxUrlBuilder);
	}

	public abstract void setAdvertismentAditionalDetails(Advertisment advertisment, Elements detailElements, OlxUrlBuilder olxUrlBuilder);

	private boolean isPublishedBeforeLastScrapingDate(Advertisment advertisment, Date lastScrapingDate) {
		return (advertisment.getPublishingDate() != null) && advertisment.getPublishingDate().before(lastScrapingDate);
	}

	private boolean isRelevant(Advertisment advertisment, List<String> wantedKeyWords, List<String> unwantedKeyWords) {
		return isPublishedByOwner(advertisment) && (wantedKeyWords.isEmpty() || hasKeyWords(advertisment, wantedKeyWords))
				&& (unwantedKeyWords.isEmpty() || !hasKeyWords(advertisment, unwantedKeyWords));
	}

	private boolean isPublishedByOwner(Advertisment advertisment) {
		return OWNER.equalsIgnoreCase(advertisment.getProvidedBy());
	}

	private boolean hasKeyWords(Advertisment advertisment, List<String> keyWords) {
		boolean hasKeyWords = false;
		String title = advertisment.getTitle() == null ? "" : advertisment.getTitle();
		String description = advertisment.getDescription() == null ? "" : advertisment.getDescription();
		String fullDescription = String.format("%s %s", title, description).toLowerCase();

		for (String key : keyWords) {
			if (fullDescription.contains(key.toLowerCase())) {
				hasKeyWords = true;
				advertisment.setKeyWord(key);
			}
		}
		return hasKeyWords;
	}

	public abstract void addToScrapingSession(ScrapingSession scrapingSession, Advertisment advertisment);

	private String getPhoneNumber(String detailsPageurl) {
		// TODO
		return null;
	}

	public abstract void saveAdvertisment(Advertisment advertisment);

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
		LOG.info(url);
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
