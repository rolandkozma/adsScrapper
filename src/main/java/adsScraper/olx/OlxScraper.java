package adsScraper.olx;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public abstract class OlxScraper<T extends Advertisment> {
	private static final Logger LOG = LoggerFactory.getLogger(OlxScraper.class);

	private static final String REFERENCE_NUMBER_SELECTOR = "div.offerheadinner p small span span span.rel";
	private static final String PRICE_SELECTOR = "div#offeractions div div div.pricelabel strong";
	private static final String USER_NAME_SELECTOR = "div#offeractions div.userbox p.userdetails span.block:nth-child(1)";
	private static final String DESCRIPTION_SELECTOR = "div#textContent p";
	private static final String PUBLISHING_DATE_SELECTOR = "div.offerheadinner p small span";
	private static final String TABLE_DETAILS_DIV_SELECTOR = "div.descriptioncontent table.details tr td div";
	private static final String PROVIDED_BY_TEXT = "oferit";
	private static final String SURFACE_TEXT = "suprafata";
	private static final String PHONE_URL = "http://olx.ro/ajax/misc/contact/phone/%s/";
	private static final String PHONE_LI_SELECTOR = "ul#contact_methods li.link-phone";

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
			String pageUrl = getPageUrl(olxUrlBuilder.page(pageNumber));
			Elements links = getAdvertismentLinks(pageUrl);

			for (Element link : links) {
				T advertisment = createAdvertisment(link, olxUrlBuilder);

				if (isPublishedBeforeLastScrapingDate(advertisment, lastScrapingDate)) {
					parseNextPage = false;
					break;
				}

				if (isRelevant(advertisment, wantedKeyWords, unwantedKeyWords)) {
					addToScrapingSession(scrapingSession, advertisment);

					String phone = getPhoneNumber(advertisment.getPhoneUrl());
					advertisment.setPhone(phone);

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

	public abstract String getPageUrl(OlxUrlBuilder page);

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

	private T createAdvertisment(Element link, OlxUrlBuilder olxUrlBuilder) {
		T advertisment = createAdvertisment();
		advertisment.setPublishingSite(Apartment.Site.OLX.value());
		advertisment.setTitle(link.text());
		advertisment.setUrl(link.absUrl("href"));

		Document advertismentPage = getDocument(advertisment.getUrl());
		advertisment.setPhoneUrl(String.format(PHONE_URL, getId(advertismentPage)));
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

	public String getId(Document advertismentPage) {
		String id = null;
		Elements elements = advertismentPage.select(PHONE_LI_SELECTOR);
		if ((elements != null) && !elements.isEmpty()) {
			String className = elements.get(0).className();

			int startJsonIndex = className.indexOf('{');
			int endJsonIndex = className.indexOf('}');

			if ((startJsonIndex != -1) && (endJsonIndex != -1) && (endJsonIndex > startJsonIndex)) {
				String json = className.substring(startJsonIndex, endJsonIndex + 1);
				try {
					JsonElement root = new JsonParser().parse(json);
					id = root.getAsJsonObject().get("id").getAsString();
				} catch (JsonParseException e) {
					LOG.warn(e.getMessage(), e);
				}
			}
		}
		return id;
	}
	public abstract T createAdvertisment();

	public void setAdvertismentDetails(T advertisment, Elements detailElements, OlxUrlBuilder olxUrlBuilder) {
		for (Element element : detailElements) {
			String elementText = element.ownText().trim().toLowerCase();
			if (elementText.contains(PROVIDED_BY_TEXT)) {
				advertisment.setProvidedBy(ParserUtil.getString(element, "a", "providedBy"));
			} else if (elementText.contains(SURFACE_TEXT)) {
				advertisment.setSurface(ParserUtil.getInteger(element, "strong", "surface"));
			}
		}
	}

	private boolean isPublishedBeforeLastScrapingDate(T advertisment, Date lastScrapingDate) {
		return (advertisment.getPublishingDate() != null) && advertisment.getPublishingDate().before(lastScrapingDate);
	}

	public boolean isRelevant(T advertisment, List<String> wantedKeyWords, List<String> unwantedKeyWords) {
		return isPublishedByOwner(advertisment) && (unwantedKeyWords.isEmpty() || !hasKeyWords(advertisment, unwantedKeyWords))
				&& (wantedKeyWords.isEmpty() || hasKeyWords(advertisment, wantedKeyWords));
	}

	private boolean isPublishedByOwner(T advertisment) {
		boolean isPublishedByOwner = OWNER.equalsIgnoreCase(advertisment.getProvidedBy());
		if (!isPublishedByOwner) {
			LOG.info("Nu e {}", OWNER);
		}
		return isPublishedByOwner;
	}

	private boolean hasKeyWords(T advertisment, List<String> keyWords) {
		boolean hasKeyWords = false;
		String title = advertisment.getTitle() == null ? "" : advertisment.getTitle();
		String description = advertisment.getDescription() == null ? "" : advertisment.getDescription();
		String fullDescription = String.format("%s %s", title, description).toLowerCase();

		for (String keyWord : keyWords) {
			if (fullDescription.contains(keyWord.toLowerCase())) {
				hasKeyWords = true;
				advertisment.setKeyWord(keyWord);
				break;
			}
		}

		if (hasKeyWords) {
			LOG.info("Cuvant cheie: {}", advertisment.getKeyWord());
		}
		return hasKeyWords;
	}

	public abstract void addToScrapingSession(ScrapingSession scrapingSession, T advertisment);

	private String getPhoneNumber(String phoneUrl) {
		String phonenumber = null;
		URL url = createUrl(phoneUrl);
		if (url != null) {
			HttpURLConnection phoneNumberRequest;
			int retriesNr = 3;
			int i = 0;
			while ((phonenumber == null) && (i < retriesNr)) {
				try {
					phoneNumberRequest = (HttpURLConnection) url.openConnection();
					try (InputStreamReader inputStreamReader = new InputStreamReader(phoneNumberRequest.getInputStream())) {
						JsonElement root = new JsonParser().parse(inputStreamReader);
						phonenumber = root.getAsJsonObject().get("value").getAsString().replaceAll("\\D+", "");
					}
				} catch (IOException | JsonParseException e) {
					LOG.warn(e.getMessage(), e);
				}
				i++;
			}
		}
		return phonenumber;
	}

	public URL createUrl(String phoneUrl) {
		URL url = null;
		try {
			url = new URL(phoneUrl);
		} catch (MalformedURLException e) {
			LOG.warn(e.getMessage(), e);
		}
		return url;
	}

	public abstract void saveAdvertisment(T advertisment);

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
