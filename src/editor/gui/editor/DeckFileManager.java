package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import editor.collection.category.CategorySpec;
import editor.collection.deck.Deck;
import editor.collection.export.CardListFormat;
import editor.database.card.Card;
import editor.gui.MainFrame;
import editor.util.ProgressInputStream;

public class DeckFileManager
{
    public static final SimpleDateFormat CHANGELOG_DATE = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss");
    /**
     * Latest version of save file.
     * 
     * Change log:
     * 1. Added save version number
     * 2. Switched changelog from read/writeObject to read/writeUTF
     */
    private static final long SAVE_VERSION = 2;

    /**
     * This class is a worker for loading a deck.
     *
     * @author Alec Roelke
     */
    private class LoadWorker extends SwingWorker<Void, Integer>
    {
        /**
         * Dialog containing the progress bar.
         */
        private JDialog dialog;
        /**
         * File to load the deck from.
         */
        private File file;
        /**
         * Progress bar to display progress to.
         */
        private JProgressBar progressBar;

        /**
         * Create a new LoadWorker.
         *
         * @param f file to load the deck from
         * @param v file to load contains save version number (only false if importing from before versions were implemented)
         * @param b progress bar showing progress
         * @param d dialog containing the progress bar
         */
        public LoadWorker(File f, JProgressBar b, JDialog d)
        {
            deck = new Deck();
            sideboard = new Deck();

            file = f;
            progressBar = b;
            dialog = d;

            progressBar.setMaximum((int)file.length());
        }

        /**
         * {@inheritDoc}
         * Load the deck, updating the progress bar all the while.
         */
        @Override
        protected Void doInBackground() throws Exception
        {
            long version;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file)))
            {
                version = ois.readLong();
            }
            // Assume that high bits in the first 64 bits are used by the serialization of a Deck
            // object and that SAVE_VERSION will never be that high.
            if (version > SAVE_VERSION)
                version = 0;

            try (ProgressInputStream pis = new ProgressInputStream(new FileInputStream(file)))
            {
                pis.addPropertyChangeListener((e) -> process(Collections.singletonList(((Long)e.getNewValue()).intValue())));
                try (ObjectInputStream ois = new ObjectInputStream(pis))
                {
                    if (version > 0)
                        ois.readLong(); // Throw out first 64 bits that have already been read
                    readDeck(deck, ois);
                    readDeck(sideboard, ois);
                    if (version < 2)
                        changelog = (String)ois.readObject();
                    else
                        changelog = ois.readUTF();
                }
            }
            return null;
        }

        @Override
        protected void done()
        {
            dialog.dispose();
        }

        @Override
        protected void process(List<Integer> chunks)
        {
            progressBar.setValue(chunks.get(chunks.size() - 1));
        }
    }

    private String changelog;
    private Deck deck;
    private File file;
    private Deck sideboard;

    public DeckFileManager()
    {
        reset();
    }

    public DeckFileManager(Deck d, Deck s, String c)
    {
        changelog = c;
        deck = d;
        file = null;
        sideboard = s;
    }

    public String changelog()
    {
        return changelog;
    }

    public Deck deck()
    {
        return deck;
    }

    public File file()
    {
        return file;
    }

    public void importList(CardListFormat format, File file) throws IOException, ParseException, IllegalStateException
    {
        // TODO: Change this to a better type of exception
        if (!deck.isEmpty())
            throw new IllegalStateException("Deck already loaded!");
        deck.addAll(format.parse(String.join(System.lineSeparator(), Files.readAllLines(file.toPath()))));
    }

    public boolean isEmpty()
    {
        return file == null;
    }

    public boolean load(File f, Window parent)
    {
        // TODO: Change this to a better type of exception
        if (!isEmpty())
            throw new IllegalStateException("Deck already loaded!");

        JDialog progressDialog = new JDialog(null, Dialog.ModalityType.APPLICATION_MODAL);
        JProgressBar progressBar = new JProgressBar();
        LoadWorker worker = new LoadWorker(f, progressBar, progressDialog);

        JPanel progressPanel = new JPanel(new BorderLayout(0, 5));
        progressDialog.setContentPane(progressPanel);
        progressPanel.add(new JLabel("Opening " + f.getName() + "..."), BorderLayout.NORTH);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        progressPanel.add(progressBar, BorderLayout.CENTER);
        JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((e) -> worker.cancel(false));
        cancelPanel.add(cancelButton);
        progressPanel.add(cancelPanel, BorderLayout.SOUTH);
        progressDialog.pack();

        worker.execute();
        progressDialog.setLocationRelativeTo(parent);
        progressDialog.setVisible(true);
        try
        {
            worker.get();
            file = f;
            return true;
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error opening " + f.getName() + ": " + e.getCause().getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
            reset();
            return false;
        }
    }

    /**
     * Read a deck from an object stream.
     * 
     * @param deck deck to load data into
     * @param in input stream to read from
     */
    private void readDeck(Deck deck, ObjectInput in) throws IOException, ClassNotFoundException
    {
        deck.clear();

        int n = in.readInt();
        for (int i = 0; i < n; i++)
        {
            Card card = MainFrame.inventory().get(in.readUTF());
            int count = in.readInt();
            LocalDate added = (LocalDate)in.readObject();
            deck.add(card, count, added);
        }
        n = in.readInt();
        for (int i = 0; i < n; i++)
        {
            CategorySpec spec = new CategorySpec();
            spec.readExternal(in);
            deck.addCategory(spec, in.readInt());
        }
    }

    private void reset()
    {
        changelog = "";
        deck = new Deck();
        file = null;
        sideboard = new Deck();
    }

    /**
     * Save the deck to the given file.
     *
     * @param f file to save to
     * @return true if the file was successfully saved, and false otherwise.
     */
    public boolean save(File f, Window parent)
    {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f, false)))
        {
            oos.writeLong(SAVE_VERSION);
            writeDeck(deck, oos);
            writeDeck(sideboard, oos);
            oos.writeUTF(changelog);
            file = f;
            return true;
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(parent, "Error saving " + f.getName() + ": " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public Deck sideboard()
    {
        return sideboard;
    }

    /**
     * Write a deck to an output stream.
     * 
     * @param deck deck to write
     * @param out stream to write to
     */
    public void writeDeck(Deck deck, ObjectOutput out) throws IOException
    {
        out.writeInt(deck.size());
        for (Card card : deck)
        {
            out.writeUTF(card.id());
            out.writeInt(deck.getData(card).count());
            out.writeObject(deck.getData(card).dateAdded());
        }
        out.writeInt(deck.numCategories());
        for (CategorySpec spec : deck.categories())
        {
            spec.writeExternal(out);
            out.writeInt(deck.getCategoryRank(spec.getName()));
        }
    }
}