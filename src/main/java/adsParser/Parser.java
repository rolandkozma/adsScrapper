package adsParser;

import java.util.Date;

import org.jsoup.nodes.Document;

public interface Parser {

    /**
     * Perform the parsing algorithm.
     */
    public void parse();

    /**
     * The date when the advertisement was published.
     *
     * @param adsDetail
     *            The document representing the details page of the advertisement
     * @return The publishing date.
     */
    public Date getPublishingDate(Document adsDetail);
}
