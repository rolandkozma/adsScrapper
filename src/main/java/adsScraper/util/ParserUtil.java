package adsScraper.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ParserUtil.class);

	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

	public static String getString(Document adsDetail, String selector, String fieldName) {
		String result = null;
		Elements elements = adsDetail.select(selector);

		if (!elements.isEmpty()) {
			result = elements.get(0).ownText();
		} else {
			LOG.warn("Failed to get {} from ads detail page!", fieldName);
		}

		LOG.debug("{}: {}", fieldName, result);
		return result;
	}

	public static String getString(Element element, String selector, String fieldName) {
		String result = null;
		Elements elements = element.select(selector);

		if (!elements.isEmpty()) {
			result = elements.get(0).ownText();
		} else {
			LOG.warn("Failed to get {} from element: {} !", fieldName, element);
		}

		LOG.debug("{}: {}", fieldName, result);
		return result;
	}

	public static Integer getInteger(Document adsDetail, String selector, String fieldName) {
		Integer result = null;
		Elements elements = adsDetail.select(selector);

		if (!elements.isEmpty()) {
			String textWithoutSpaces = elements.get(0).ownText().replaceAll("\\s", "");
			Matcher matcher = NUMBER_PATTERN.matcher(textWithoutSpaces);
			if (matcher.find()) {
				String number = matcher.group();
				try {
					result = Integer.valueOf(number);
				} catch (NumberFormatException e) {
					LOG.warn("Failed to parse {} from: {} !", fieldName, number);
				}
			} else {
				LOG.warn("Failed to parse {} from {}", fieldName, textWithoutSpaces);
			}
		} else {
			LOG.warn("Failed to get {} from ads detail page!", fieldName);
		}

		LOG.debug("{}: {}", fieldName, result);
		return result;
	}

	public static Integer getInteger(Element element, String selector, String fieldName) {
		Integer result = null;
		Elements elements = element.select(selector);

		if (!elements.isEmpty()) {
			String textWithoutSpaces = elements.get(0).ownText().replaceAll("\\s", "");
			Matcher matcher = NUMBER_PATTERN.matcher(textWithoutSpaces);
			if (matcher.find()) {
				String number = matcher.group();
				try {
					result = Integer.valueOf(number);
				} catch (NumberFormatException e) {
					LOG.warn("Failed to parse {} from: {} !", fieldName, number);
				}
			} else {
				LOG.warn("Failed to parse {} from {}", fieldName, textWithoutSpaces);
			}
		} else {
			LOG.warn("Failed to get {} from element: {}!", fieldName, element);
		}

		LOG.debug("{}: {}", fieldName, result);
		return result;
	}
}
