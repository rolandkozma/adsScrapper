package adsParser.olx;

public class OlxUrlBuilder {
    public static final String BASE_URL = "http://olx.ro/imobiliare/apartamente-garsoniere-de-vanzare/";
    public static final String TWO_ROOMS = "2-camere/";
    public static final String THREE_ROOMS = "3-camere/";
    public static final String CLUJ_CITY = "cluj-napoca/";
    public static final String INDIVIDUALS = "search[private_business]=private";
    public static final String ORDER_DESC_BY_DATE = "search[order]=created_at%3Adesc";
    public static final String PAGE = "page=";
    public static final String QUERY_PARAMS = "?";
    public static final String AND = "&";

    public String getUrl() {
	return BASE_URL + TWO_ROOMS + CLUJ_CITY + QUERY_PARAMS + INDIVIDUALS + AND + ORDER_DESC_BY_DATE;
    }
}
