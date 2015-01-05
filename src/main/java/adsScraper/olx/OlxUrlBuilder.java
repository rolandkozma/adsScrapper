package adsScraper.olx;

public class OlxUrlBuilder {
	private static final String BASE_URL = "http://olx.ro/imobiliare/";
	private static final String REAL_ESTATE_TYPE = "%s/";
	private static final String ROOMS = "%d-camere/";
	private static final String CITY = "%s/";
	private static final String BUSINESS = "search[private_business]=%s";
	private static final String ORDER_DESC_BY_DATE = "search[order]=%s";
	private static final String PAGE = "page=%d";
	private static final String QUERY_PARAMS = "?";
	private static final String AND = "&";
	private static final String APARTMENT_URL = BASE_URL + REAL_ESTATE_TYPE + ROOMS + CITY + QUERY_PARAMS + BUSINESS + AND + ORDER_DESC_BY_DATE + AND
			+ PAGE;
	private static final String HOUSE_URL = BASE_URL + REAL_ESTATE_TYPE + CITY + QUERY_PARAMS + BUSINESS + AND + ORDER_DESC_BY_DATE + AND + PAGE;
	private static final String LAND_URL = BASE_URL + REAL_ESTATE_TYPE + CITY + QUERY_PARAMS + BUSINESS + AND + ORDER_DESC_BY_DATE + AND + PAGE;

	private RealEstateType realEstateType;
	private Integer rooms;
	private City city;
	private Business business;
	private Order orderBy;
	private Integer page;

	public enum RealEstateType {
		APARTMENT("apartamente-garsoniere-de-vanzare"),

		HOUSE("case-de-vanzare"),

		LAND("terenuri");

		private final String value;

		private RealEstateType(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}
	}

	public enum City {
		CLUJ_NAPOCA("cluj-napoca");

		private final String value;

		private City(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}
	}

	public enum Business {
		PRIVATE("private"),

		BUSINESS("business");

		private final String value;

		private Business(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}
	}

	public enum Order {
		DATE("created_at%3Adesc"),

		CHIP("filter_float_price%3Aasc"),

		EXPANSIVE("filter_float_price%3Adesc");

		private final String value;

		private Order(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}
	}

	public OlxUrlBuilder rooms(int rooms) {
		this.rooms = rooms;
		return this;
	}

	public OlxUrlBuilder city(City city) {
		this.city = city;
		return this;
	}

	public OlxUrlBuilder business(Business business) {
		this.business = business;
		return this;
	}

	public OlxUrlBuilder orderBy(Order orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	public OlxUrlBuilder page(int page) {
		this.page = page;
		return this;
	}

	public OlxUrlBuilder realEstateType(RealEstateType realEstateType) {
		this.realEstateType = realEstateType;
		return this;
	}

	public RealEstateType getRealEstateType() {
		return realEstateType;
	}

	public Integer getRooms() {
		return rooms;
	}

	public City getCity() {
		return city;
	}

	public Business getBusiness() {
		return business;
	}

	public Order getOrderBy() {
		return orderBy;
	}

	public Integer getPage() {
		return page;
	}

	public String getApartmentUrl() {
		if ((realEstateType == null) || (rooms == null) || (city == null) || (business == null) || (orderBy == null) || (page == null)) {
			throw new NullPointerException("Cannot construct Apartment URL with null fields.");
		}

		return String.format(APARTMENT_URL, RealEstateType.APARTMENT.value(), rooms, getCity().value(), getBusiness().value(), getOrderBy().value(),
				getPage());
	}

	public String getHouseUrl() {
		if ((realEstateType == null) || (city == null) || (business == null) || (orderBy == null) || (page == null)) {
			throw new NullPointerException("Cannot construct House URL with null fields.");
		}

		return String.format(HOUSE_URL, RealEstateType.HOUSE.value(), getCity().value(), getBusiness().value(), getOrderBy().value(), getPage());
	}

	public String getLandUrl() {
		if ((realEstateType == null) || (city == null) || (business == null) || (orderBy == null) || (page == null)) {
			throw new NullPointerException("Cannot construct Land URL with null fields.");
		}

		return String.format(LAND_URL, RealEstateType.LAND.value(), getCity().value(), getBusiness().value(), getOrderBy().value(), getPage());
	}

}
