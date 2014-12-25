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

public class OlxParser {

    private static final Logger LOG = LoggerFactory.getLogger(OlxParser.class);

    private static final Locale LOCALE = new Locale("RO");
    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, LOCALE);
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{1,2} \\w* \\d{4}");
    private static final Pattern TIME_PATTERN = Pattern.compile("\\d{1,2}:\\d{1,2}");

    public static final String BASE_URL = "http://olx.ro/imobiliare/apartamente-garsoniere-de-vanzare/";
    public static final String TWO_ROOMS = "2-camere/";
    public static final String THREE_ROOMS = "3-camere/";
    public static final String CLUJ_CITY = "cluj-napoca/";
    public static final String INDIVIDUALS = "search[private_business]=private";
    public static final String ORDER_DESC_BY_DATE = "search[order]=created_at%3Adesc";
    public static final String QUERY_PARAMS = "?";
    public static final String AND = "&";

    public static void main(String[] args) {

	Date currentDate = new Date();

	Document doc = getDocument(getUrl());

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
	}
    }

    private static boolean isPublishedToday(Date currentDate, Date publishingDate) {
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

    private static Date getPublishingDate(Document adsDetail) {
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

    private static String getDate(String dateText) {
	String date = null;
	Matcher matcher = DATE_PATTERN.matcher(dateText);
	if (matcher.find()) {
	    date = matcher.group();
	}
	return date;
    }

    private static String getTime(String dateText) {
	String time = null;
	Matcher matcher = TIME_PATTERN.matcher(dateText);
	if (matcher.find()) {
	    time = matcher.group();
	}
	return time;
    }

    private static Document getDocument(String url) {
	Document doc = null;
	try {
	    doc = Jsoup.connect(url).get();
	} catch (IOException e) {
	    LOG.warn(String.format("Failed to get document! url: %s", url), e);
	}
	return doc;
    }

    private static String getUrl() {
	return BASE_URL + TWO_ROOMS + CLUJ_CITY + QUERY_PARAMS + INDIVIDUALS + AND + ORDER_DESC_BY_DATE;
    }
}
