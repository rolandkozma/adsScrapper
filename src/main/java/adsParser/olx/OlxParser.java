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

import adsParser.olx.OlxUrlBuilder.Business;
import adsParser.olx.OlxUrlBuilder.City;
import adsParser.olx.OlxUrlBuilder.HouseType;
import adsParser.olx.OlxUrlBuilder.Order;

public class OlxParser {

    private static final Logger LOG = LoggerFactory.getLogger(OlxParser.class);

    private static final Locale LOCALE = new Locale("RO");
    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, LOCALE);
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{1,2} \\w* \\d{4}");
    private static final Pattern TIME_PATTERN = Pattern.compile("\\d{1,2}:\\d{1,2}");

    public void parse(City city, Business business, HouseType houseType, int rooms) {
	Date currentDate = new Date();

	int page = 1;
	OlxUrlBuilder olxUrlBuilder = new OlxUrlBuilder().city(city).business(business).houseType(houseType).orderBy(Order.DATE).rooms(rooms)
		.page(page);
	String url = olxUrlBuilder.getUrl();
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

	    if (isPublishedToday(currentDate, publishingDate)) {
		LOG.debug("E de azi.");
	    } else {
		break;
	    }

	    pause(1000);
	}
    }

    private void pause(long millis) {
	try {
	    Thread.sleep(millis);
	} catch (InterruptedException e) {
	    LOG.warn(e.getMessage(), e);
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

    private Date getPublishingDate(Document adsDetail) {
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
	}
	return date;
    }

    private String getTime(String dateText) {
	String time = null;
	Matcher matcher = TIME_PATTERN.matcher(dateText);
	if (matcher.find()) {
	    time = matcher.group();
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