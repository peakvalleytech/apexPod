package de.danoeh.apexpod.parser.feed.element.namespace;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import de.danoeh.apexpod.model.feed.Feed;
import de.danoeh.apexpod.parser.feed.FeedHandler;
import de.danoeh.apexpod.parser.feed.FeedHandlerResult;
import de.danoeh.apexpod.parser.feed.UnsupportedFeedtypeException;

@RunWith(RobolectricTestRunner.class)
public class TestFeedHandler {
    @Test(expected = UnsupportedFeedtypeException.class)
    public void testFeedHandlerCreationThrowsOnInCompleteFeed() throws UnsupportedFeedtypeException, SAXException, ParserConfigurationException, IOException {
        FeedHandler handler = new FeedHandler();
        Feed parsedFeed = new Feed("http://example.com/feed", null);
        handler.parseFeed(parsedFeed);
    }

    @Test
    public void testFeedHandlerCreationWithFeedFile() throws UnsupportedFeedtypeException, SAXException, ParserConfigurationException, IOException {
        FeedHandler handler = new FeedHandler();
        Feed parsedFeed = new Feed("http://example.com/feed", null);
        File feedFile = FeedParserTestHelper.getFeedFile("feed-atom-testLogoWithWhitespace.xml");
        parsedFeed.setFile_url(feedFile.getAbsolutePath());
        handler.parseFeed(parsedFeed);
    }

    @Test
    public void testFeedHandlerCreationDoesNotChangeUrl() throws UnsupportedFeedtypeException, SAXException, ParserConfigurationException, IOException {
        FeedHandler handler = new FeedHandler();
        String url = "http://example.com/feed";
        Feed parsedFeed = new Feed(url, null);
        File feedFile = FeedParserTestHelper.getFeedFile("feed-atom-testLogoWithWhitespace.xml");
        parsedFeed.setFile_url(feedFile.getAbsolutePath());
        FeedHandlerResult result = handler.parseFeed(parsedFeed);
        assertEquals(result.feed.getDownload_url(), url);
    }
}
