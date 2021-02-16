package de.danoeh.antennapod.core.util.id3reader;

import de.danoeh.antennapod.core.feed.Chapter;
import de.danoeh.antennapod.core.feed.ID3Chapter;
import de.danoeh.antennapod.core.util.id3reader.model.FrameHeader;
import org.apache.commons.io.input.CountingInputStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static de.danoeh.antennapod.core.util.id3reader.Id3ReaderTest.concat;
import static de.danoeh.antennapod.core.util.id3reader.Id3ReaderTest.generateFrameHeader;
import static de.danoeh.antennapod.core.util.id3reader.Id3ReaderTest.generateId3Header;
import static org.junit.Assert.assertEquals;

public class ChapterReaderTest {
    private static final byte CHAPTER_WITHOUT_SUBFRAME_START_TIME = 23;
    private static final byte[] CHAPTER_WITHOUT_SUBFRAME = {
            'C', 'H', '1', 0, // String ID for mapping to CTOC
            0, 0, 0, CHAPTER_WITHOUT_SUBFRAME_START_TIME, // Start time
            0, 0, 0, 0, // End time
            0, 0, 0, 0, // Start offset
            0, 0, 0, 0 // End offset
    };

    @Test
    public void testReadFullTagWithChapter() throws IOException, ID3ReaderException {
        byte[] chapter = concat(
                generateFrameHeader(ChapterReader.FRAME_ID_CHAPTER, CHAPTER_WITHOUT_SUBFRAME.length),
                CHAPTER_WITHOUT_SUBFRAME);
        byte[] data = concat(
                generateId3Header(chapter.length),
                chapter);
        CountingInputStream inputStream = new CountingInputStream(new ByteArrayInputStream(data));
        ChapterReader reader = new ChapterReader(inputStream);
        reader.readInputStream();
        assertEquals(1, reader.getChapters().size());
        assertEquals(CHAPTER_WITHOUT_SUBFRAME_START_TIME, reader.getChapters().get(0).getStart());
    }

    @Test
    public void testReadFullTagWithMultipleChapters() throws IOException, ID3ReaderException {
        byte[] chapter = concat(
                generateFrameHeader(ChapterReader.FRAME_ID_CHAPTER, CHAPTER_WITHOUT_SUBFRAME.length),
                CHAPTER_WITHOUT_SUBFRAME);
        byte[] data = concat(
                generateId3Header(2 * chapter.length),
                chapter,
                chapter);
        CountingInputStream inputStream = new CountingInputStream(new ByteArrayInputStream(data));
        ChapterReader reader = new ChapterReader(inputStream);
        reader.readInputStream();
        assertEquals(2, reader.getChapters().size());
        assertEquals(CHAPTER_WITHOUT_SUBFRAME_START_TIME, reader.getChapters().get(0).getStart());
        assertEquals(CHAPTER_WITHOUT_SUBFRAME_START_TIME, reader.getChapters().get(1).getStart());
    }

    @Test
    public void testReadChapterWithoutSubframes() throws IOException, ID3ReaderException {
        FrameHeader header = new FrameHeader(ChapterReader.FRAME_ID_CHAPTER,
                CHAPTER_WITHOUT_SUBFRAME.length, (short) 0);
        CountingInputStream inputStream = new CountingInputStream(new ByteArrayInputStream(CHAPTER_WITHOUT_SUBFRAME));
        Chapter chapter = new ChapterReader(inputStream).readChapter(header);
        assertEquals(CHAPTER_WITHOUT_SUBFRAME_START_TIME, chapter.getStart());
    }

    @Test
    public void testReadChapterWithTitle() throws IOException, ID3ReaderException {
        byte[] title = {
            ID3Reader.ENCODING_ISO,
            'H', 'e', 'l', 'l', 'o', // Title
            0 // Null-terminated
        };
        byte[] chapterData = concat(
            CHAPTER_WITHOUT_SUBFRAME,
            generateFrameHeader(ChapterReader.FRAME_ID_TITLE, title.length),
            title);
        FrameHeader header = new FrameHeader(ChapterReader.FRAME_ID_CHAPTER, chapterData.length, (short) 0);
        CountingInputStream inputStream = new CountingInputStream(new ByteArrayInputStream(chapterData));
        ChapterReader reader = new ChapterReader(inputStream);
        Chapter chapter = reader.readChapter(header);
        assertEquals(CHAPTER_WITHOUT_SUBFRAME_START_TIME, chapter.getStart());
        assertEquals("Hello", chapter.getTitle());
    }

    @Test
    public void testReadTitleWithGarbage() throws IOException, ID3ReaderException {
        byte[] titleSubframeContent = {
                ID3Reader.ENCODING_ISO,
                'A', // Title
                0, // Null-terminated
                42, 42, 42, 42 // Garbage, should be ignored
        };
        FrameHeader header = new FrameHeader(ChapterReader.FRAME_ID_TITLE, titleSubframeContent.length, (short) 0);
        CountingInputStream inputStream = new CountingInputStream(new ByteArrayInputStream(titleSubframeContent));
        ChapterReader reader = new ChapterReader(inputStream);
        Chapter chapter = new ID3Chapter("", 0);
        reader.readChapterSubFrame(header, chapter);
        assertEquals("A", chapter.getTitle());

        // Should skip the garbage and point to the next frame
        assertEquals(titleSubframeContent.length, reader.getPosition());
    }
}
