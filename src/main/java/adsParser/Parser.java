package adsParser;

import java.util.Date;

import org.jsoup.nodes.Document;

import adsParser.olx.OlxUrlBuilder.Business;
import adsParser.olx.OlxUrlBuilder.City;
import adsParser.olx.OlxUrlBuilder.HouseType;

public interface Parser {
    /**
     *
     * @param city
     * @param business
     * @param houseType
     * @param rooms
     */
    public void parse(City city, Business business, HouseType houseType, int rooms);

    /**
     * The date when the advertisement was published.
     *
     * @param adsDetail
     *            The document representing the details page of the advertisement
     * @return The publishing date.
     */
    public Date getPublishingDate(Document adsDetail);
}
