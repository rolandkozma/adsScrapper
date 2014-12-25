package adsParser;

import adsParser.olx.OlxParser;
import adsParser.olx.OlxUrlBuilder.Business;
import adsParser.olx.OlxUrlBuilder.City;
import adsParser.olx.OlxUrlBuilder.HouseType;

public class Main {

    public static void main(String[] args) {
	new OlxParser().parse(City.CLUJ_NAPOCA, Business.PRIVATE, HouseType.APARTMENT, 2);
    }

}
