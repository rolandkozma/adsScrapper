package adsParser.olx;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adsParser.Parser;
import adsParser.ParserUtil;
import adsParser.olx.OlxUrlBuilder.Business;
import adsParser.olx.OlxUrlBuilder.City;
import adsParser.olx.OlxUrlBuilder.HouseType;
import adsParser.olx.OlxUrlBuilder.Order;

public class OlxParser implements Parser {

    private static final Logger LOG = LoggerFactory.getLogger(OlxParser.class);

    private static final String REFERENCE_NUMBER_SELECTOR = "div.offerheadinner p small span span span.rel";
    private static final String SURFACE_SELECTOR = "div.descriptioncontent table.details tr:nth-child(1) td:nth-child(3) strong";
    private static final String PRICE_SELECTOR = "div#offeractions div div div.pricelabel strong";
    private static final String USER_NAME_SELECTOR = "div#offeractions div.userbox p.userdetails span.block:nth-child(1)";
    private static final String PHONE_NUMBER_SELECTOR = "ul#contact_methods li.link-phone div.contactitem strong";
    private static final String DESCRIPTION_SELECTOR = "div#textContent p";
    private static final String CONSTRUCTION_PERIOD_SELECTOR = "div.descriptioncontent table.details tr:nth-child(3) td:nth-child(1) a";
    private static final String PROVIDED_BY_SELECTOR = "div.descriptioncontent table.details tr:nth-child(1) td:nth-child(1) a";
    private static final String COMPARTIMENTALIZATION_SELECTOR = "div.descriptioncontent table.details tr:nth-child(1) td:nth-child(2) a";
    private static final String PUBLISHING_DATE_SELECTOR = "div.offerheadinner p small span";

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, new Locale("RO"));
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{1,2} \\w* \\d{4}");
    private static final Pattern TIME_PATTERN = Pattern.compile("\\d{1,2}:\\d{1,2}");

    private final OlxUrlBuilder olxUrlBuilder;

    public OlxParser(City city, Business business, HouseType houseType, int rooms) {
	olxUrlBuilder = new OlxUrlBuilder().city(city).business(business).houseType(houseType).orderBy(Order.DATE).rooms(rooms);
    }

    public void parse() {
	Date currentDate = new Date();

	String url = olxUrlBuilder.page(1).getUrl();
	Document doc = getDocument(url);

	Elements links = doc.select("a[href]").select(".marginright5").select(".link.linkWithHash").select(".detailsLink");

	LOG.debug("got: {}", links.size());

	for (Element link : links) {
	    String title = link.text();
	    LOG.info(title);

	    String absUrl = link.absUrl("href");
	    LOG.info(absUrl);

	    Document adsDetail = getDocument(absUrl);

	    Date publishingDate = getPublishingDate(adsDetail, PUBLISHING_DATE_SELECTOR, "publishingDate");
	    String providedBy = ParserUtil.getString(adsDetail, PROVIDED_BY_SELECTOR, "providedBy");
	    String compartimentalization = ParserUtil.getString(adsDetail, COMPARTIMENTALIZATION_SELECTOR, "compartimentalization");
	    Integer surface = ParserUtil.getInteger(adsDetail, SURFACE_SELECTOR, "surface");
	    String constructionPeriod = ParserUtil.getString(adsDetail, CONSTRUCTION_PERIOD_SELECTOR, "constructionPeriod");
	    String description = ParserUtil.getString(adsDetail, DESCRIPTION_SELECTOR, "description");
	    Integer price = ParserUtil.getInteger(adsDetail, PRICE_SELECTOR, "price");
	    String phoneNumber = ParserUtil.getString(adsDetail, PHONE_NUMBER_SELECTOR, "phoneNumber");
	    String userName = ParserUtil.getString(adsDetail, USER_NAME_SELECTOR, "userName");
	    Integer referenceNumber = ParserUtil.getInteger(adsDetail, REFERENCE_NUMBER_SELECTOR, "referenceNumber");

	    if (isPublishedToday(currentDate, publishingDate)) {
		LOG.debug("E de azi.");
	    } else {
		break;
	    }

	    pause(2000);
	}
    }

    private boolean isPublishedToday(Date currentDate, Date publishingDate) {
	boolean isPublishedToday = false;

	Calendar currentDateCalendar = Calendar.getInstance();
	currentDateCalendar.setTime(currentDate);

	Calendar publishingDateCalendar = Calendar.getInstance();
	publishingDateCalendar.setTime(publishingDate);

	if ((currentDateCalendar.get(Calendar.YEAR) == publishingDateCalendar.get(Calendar.YEAR))
		&& ((currentDateCalendar.get(Calendar.MONTH) == publishingDateCalendar.get(Calendar.MONTH)))
		&& ((currentDateCalendar.get(Calendar.DAY_OF_MONTH) == publishingDateCalendar.get(Calendar.DAY_OF_MONTH)))) {
	    isPublishedToday = true;
	}

	return isPublishedToday;
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
