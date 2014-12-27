package adsScraper.olx;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.Stateless;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adsScraper.ParserUtil;
import adsScraper.olx.OlxUrlBuilder.Business;
import adsScraper.olx.OlxUrlBuilder.City;
import adsScraper.olx.OlxUrlBuilder.HouseType;
import adsScraper.olx.OlxUrlBuilder.Order;

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

	public void scrap(City city, Business business, HouseType houseType, int rooms) {
		OlxUrlBuilder olxUrlBuilder = new OlxUrlBuilder().city(city).business(business).houseType(houseType).orderBy(Order.DATE).rooms(rooms);

		Date currentDate = new Date();
		Date lastRunDate = getLastRunDate(currentDate);

		boolean parseNextPage = true;
		int pageNumber = 0;

		LOG.info("-------------------------------start scraping----------------------------------------------------------\n");
		while (parseNextPage) {
			pageNumber++;
			String url = olxUrlBuilder.page(pageNumber).getUrl();
			Elements links = getNotSponsoredLinks(url); // TODO scrap sponsored links too
			LOG.debug("got: {}", links.size());

			for (Element link : links) {
				Advertisment advertisment = createAdvertisment(link);

				if ((advertisment.getPublishingDate() == null) || advertisment.getPublishingDate().before(lastRunDate)) {
					parseNextPage = false;
					break;
				}

				LOG.info(advertisment.toString());
				LOG.info("---------------------------------------------------------------------------------------------------\n");
				pause(PAUSE_TIME);
			}
		}
		LOG.info("-------------------------------end scraping---------------------------------------------------------\n");
	}

	private Elements getNotSponsoredLinks(String url) {
		Document doc = getDocument(url);
		Elements links = doc.select("a[href]").select(".marginright5").select(".link.linkWithHash").select(".detailsLink");
		return links;
	}

	private Advertisment createAdvertisment(Element link) {
		Advertisment advertisment = new Advertisment();
		advertisment.setTitle(link.text());
		advertisment.setAbsUrl(link.absUrl("href"));

		Document adsDetail = getDocument(advertisment.getAbsUrl());
		advertisment.setPublishingDate(getPublishingDate(adsDetail, PUBLISHING_DATE_SELECTOR, "publishingDate"));
		advertisment.setReferenceNumber(ParserUtil.getInteger(adsDetail, REFERENCE_NUMBER_SELECTOR, "referenceNumber"));
		advertisment.setDescription(ParserUtil.getString(adsDetail, DESCRIPTION_SELECTOR, "description"));
		advertisment.setPrice(ParserUtil.getInteger(adsDetail, PRICE_SELECTOR, "price"));
		advertisment.setPhoneNumber(ParserUtil.getString(adsDetail, PHONE_NUMBER_SELECTOR, "phoneNumber"));
		advertisment.setUserName(ParserUtil.getString(adsDetail, USER_NAME_SELECTOR, "userName"));

		Elements elements = adsDetail.select(TABLE_DETAILS_DIV_SELECTOR);
		for (Element element : elements) {
			String elementText = element.ownText().trim().toLowerCase();
			if (elementText.contains(PROVIDED_BY_TEXT)) {
				advertisment.setProvidedBy(ParserUtil.getString(element, "a", "providedBy"));
			} else if (elementText.contains(COMPARTIMENTALIZATION_TEXT)) {
				advertisment.setCompartimentalization(ParserUtil.getString(element, "a", "compartimentalization"));
			} else if (elementText.contains(SURFACE_TEXT)) {
				advertisment.setSurface(ParserUtil.getInteger(element, "strong", "surface"));
			} else if (elementText.contains(CONSTRUCTION_PERIOD_TEXT)) {
				advertisment.setConstructionPeriod(ParserUtil.getString(element, "a", "constructionPeriod"));
			} else if (elementText.contains(ENDOWMENTS_TEXT)) {
				advertisment.setEndowments(ParserUtil.getString(element, "a", "endowments"));
			}
		}
		return advertisment;
	}

	private Date getLastRunDate(Date currentDate) {
		Date lastRunDate = null;
		// TODO get last run date from db
		lastRunDate = getYesterdaysDate(currentDate);
		LOG.debug("lastRunDate: {}", lastRunDate);
		return lastRunDate;
	}

	private Date getYesterdaysDate(Date currentDate) {
		Calendar calendar = Calendar.getInstance(LOCALE);
		calendar.setTime(currentDate);
		calendar.add(Calendar.DATE, -1);
		return calendar.getTime();
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
			LOG.error("Failed to get publishingDate - > scraping will stop! ");
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
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			LOG.warn(String.format("Failed to get document! url: %s", url), e);
		}
		return doc;
	}

}
