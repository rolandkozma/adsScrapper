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
import adsParser.olx.OlxUrlBuilder.Business;
import adsParser.olx.OlxUrlBuilder.City;
import adsParser.olx.OlxUrlBuilder.HouseType;
import adsParser.olx.OlxUrlBuilder.Order;

public class OlxParser implements Parser {

    private static final Logger LOG = LoggerFactory.getLogger(OlxParser.class);

    private static final Locale LOCALE = new Locale("RO");
    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, LOCALE);
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{1,2} \\w* \\d{4}");
    private static final Pattern TIME_PATTERN = Pattern.compile("\\d{1,2}:\\d{1,2}");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

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

	    Date publishingDate = getPublishingDate(adsDetail);
	    String providedBy = getProvidedBy(adsDetail);
	    String compartimentalization = getCompartimentalization(adsDetail);
	    Integer surface = getSurface(adsDetail);
	    String constructionTime = getConstructionTime(adsDetail);
	    String description = getDescription(adsDetail);
	    Integer price = getPrice(adsDetail);
	    String phoneNumber = getPhoneNumber(adsDetail);
	    String userName = getUserName(adsDetail);
	    Long referenceNr = getReferenceNumber(adsDetail);

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

    public String getProvidedBy(Document adsDetail) {
	String providedBy = null;

	Elements elements = adsDetail.select("div.descriptioncontent table.details tr:nth-child(1) td:nth-child(1) a");

	if (!elements.isEmpty()) {
	    providedBy = elements.get(0).ownText().toLowerCase();
	} else {
	    LOG.warn("Failed to get providedBy from ads detail page!");
	}

	LOG.debug("providedBy: {}", providedBy);
	return providedBy;
    }

    public String getCompartimentalization(Document adsDetail) {
	String compartimentalization = null;

	Elements elements = adsDetail.select("div.descriptioncontent table.details tr:nth-child(1) td:nth-child(2) a");

	if (!elements.isEmpty()) {
	    compartimentalization = elements.get(0).ownText().toLowerCase();
	} else {
	    LOG.warn("Failed to get compartimentalization from ads detail page!");
	}

	LOG.debug("compartimentalization: {}", compartimentalization);
	return compartimentalization;
    }

    public Integer getSurface(Document adsDetail) {
	Integer surface = null;

	Elements elements = adsDetail.select("div.descriptioncontent table.details tr:nth-child(1) td:nth-child(3) strong");

	if (!elements.isEmpty()) {
	    String surfaceText = elements.get(0).ownText().toLowerCase();
	    Matcher matcher = NUMBER_PATTERN.matcher(surfaceText);
	    if (matcher.find()) {
		String surfaceString = matcher.group();
		try {
		    surface = Integer.valueOf(surfaceString);
		} catch (NumberFormatException e) {
		    LOG.warn("Failed to parse surface from: {} !", surfaceString);
		}
	    } else {
		LOG.warn("Failed to parse surface from {}", surfaceText);
	    }

	} else {
	    LOG.warn("Failed to get surface from ads detail page!");
	}

	LOG.debug("surface: {}", surface);
	return surface;
    }

    private String getConstructionTime(Document adsDetail) {
	String constructionTime = null;

	Elements elements = adsDetail.select("div.descriptioncontent table.details tr:nth-child(3) td:nth-child(1) a");

	if (!elements.isEmpty()) {
	    constructionTime = elements.get(0).ownText().toLowerCase();
	} else {
	    LOG.warn("Failed to get constructionTime from ads detail page!");
	}

	LOG.debug("constructionTime: {}", constructionTime);
	return constructionTime;
    }

    private String getDescription(Document adsDetail) {
	String description = null;

	Elements elements = adsDetail.select("div.descriptioncontent div#textContent p");

	if (!elements.isEmpty()) {
	    description = elements.get(0).ownText();
	} else {
	    LOG.warn("Failed to get description from ads detail page!");
	}

	LOG.debug("description: {}", description);
	return description;
    }

    private Integer getPrice(Document adsDetail) {
	Integer price = null;

	Elements elements = adsDetail.select("div#offerbox div#offeractions div div div.pricelabel strong");

	if (!elements.isEmpty()) {
	    String priceText = elements.get(0).ownText().replaceAll("\\s", "");
	    Matcher matcher = NUMBER_PATTERN.matcher(priceText);
	    if (matcher.find()) {
		String priceString = matcher.group();
		try {
		    price = Integer.valueOf(priceString);
		} catch (NumberFormatException e) {
		    LOG.warn("Failed to parse price from: {} !", priceString);
		}
	    } else {
		LOG.warn("Failed to parse price from {}", priceText);
	    }

	} else {
	    LOG.warn("Failed to get price from ads detail page!");
	}

	LOG.debug("price: {}", price);
	return price;
    }

    private String getPhoneNumber(Document adsDetail) {
	String phoneNumber = null;

	Elements elements = adsDetail.select("ul#contact_methods li.link-phone div.contactitem strong");

	if (!elements.isEmpty()) {
	    phoneNumber = elements.get(0).ownText();
	} else {
	    LOG.warn("Failed to get phoneNumber from ads detail page!");
	}

	LOG.debug("phoneNumber: {}", phoneNumber);
	return phoneNumber;
    }

    private Long getReferenceNumber(Document adsDetail) {
	Long referenceNumber = null;

	Elements elements = adsDetail.select("div.offerheadinner p small span span span.rel");

	if (!elements.isEmpty()) {
	    String referenceNumberText = elements.get(0).ownText();
	    try {
		referenceNumber = Long.valueOf(referenceNumberText);
	    } catch (NumberFormatException e) {
		LOG.warn("Failed to parse referenceNumber from: {} !", referenceNumberText);
	    }
	} else {
	    LOG.warn("Failed to get referenceNumber from ads detail page!");
	}

	LOG.debug("referenceNumber: {}", referenceNumber);
	return referenceNumber;
    }

    private String getUserName(Document adsDetail) {
	String userName = null;

	Elements elements = adsDetail.select("div#offerbox div#offeractions div div div.userbox p.userdetails span.block:nth-child(1)");

	if (!elements.isEmpty()) {
	    userName = elements.get(0).ownText();
	} else {
	    LOG.warn("Failed to get userName from ads detail page!");
	}

	LOG.debug("userName: {}", userName);
	return userName;
    }

    public Date getPublishingDate(Document adsDetail) {
	Date publishingDate = null;
	Elements elements = adsDetail.select("div.offerheadinner p small span");

	if (!elements.isEmpty()) {
	    String dateText = elements.get(0).ownText().toLowerCase();
	    LOG.debug(dateText);

	    String time = getTime(dateText);
	    String date = getDate(dateText);
	    String formatedDateTime = String.format("%s %s", date, time);

	    try {
		publishingDate = DATE_FORMAT.parse(formatedDateTime);
	    } catch (ParseException ex) {
		LOG.warn(String.format("Faled to parse date from string: %s", formatedDateTime), ex);
	    }
	} else {
	    LOG.warn("Failed to get publishing date from ads detail page!");
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
