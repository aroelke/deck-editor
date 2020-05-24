package editor.gui.ccp;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Arrays;
import java.util.Collection;

import editor.database.card.Card;

/**
 * This class represents the data that can be transferred from an inventory via
 * drag and drop or cut/copy/paste.  It supports card and String flavors.
 *
 * @author Alec Roelke
 */
public class InventoryTransferData implements Transferable
{
    /**
     * Cards to be transferred.
     */
    private Card[] cards;

    /**
     * Create a new TransferData from the given cards.
     *
     * @param cards cards to transfer
     */
    public InventoryTransferData(Card... cards)
    {
        this.cards = cards;
    }

    /**
     * Create a new TransferData from the given cards.
     *
     * @param cards cards to transfer
     */
    public InventoryTransferData(Collection<Card> cards)
    {
        this(cards.toArray(new Card[0]));
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
    {
        if (flavor.equals(Card.cardFlavor))
            return cards;
        else if (flavor.equals(DataFlavor.stringFlavor))
            return Arrays.stream(cards).map(Card::unifiedName).reduce("", (a, b) -> a + "\n" + b);
        else
            throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        return new DataFlavor[]{Card.cardFlavor, DataFlavor.stringFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return Arrays.asList(getTransferDataFlavors()).contains(flavor);
    }
}