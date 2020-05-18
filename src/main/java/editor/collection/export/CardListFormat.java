package editor.collection.export;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import editor.collection.CardList;
import editor.gui.editor.DeckSerializer;

/**
 * This is an interface for formatting a list of cards.  It can format
 * a list of cards into a string and it can parse a string into a card
 * list.
 *
 * @author Alec Roelke
 */
public interface CardListFormat
{
    /**
     * Create a string representation of the given list of cards.
     *
     * @param list list of cards to format
     * @return the string representation of the list of cards
     */
    String format(CardList list);

    /**
     * @return <code>true</code> if this CardListFormat has headings, and
     * <code>false</code> otherwise.
     */
    default boolean hasHeader()
    {
        return !header().isEmpty();
    }

    /**
     * @return A String containing the formatted list header.  If the format
     * doesn't contain headers, it should be the empty String.
     */
    String header();

    /**
     * Read an input stream and parse it for a list of cards.
     * 
     * @param source input stream to parse
     * @return list of cards containing values from the parsed stream
     * @throws ParseException if the stream cannot be parsed
     * @throws IOException if the stream cannot be read
     */
    DeckSerializer parse(InputStream source) throws ParseException, IOException;
}