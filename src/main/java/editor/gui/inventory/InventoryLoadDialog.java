package editor.gui.inventory;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import editor.collection.Inventory;
import editor.database.attributes.Expansion;
import editor.database.attributes.Legality;
import editor.database.attributes.ManaType;
import editor.database.attributes.Rarity;
import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.database.card.FlipCard;
import editor.database.card.MeldCard;
import editor.database.card.SingleCard;
import editor.database.card.SplitCard;
import editor.database.card.TransformCard;
import editor.filter.leaf.options.multi.CardTypeFilter;
import editor.filter.leaf.options.multi.LegalityFilter;
import editor.filter.leaf.options.multi.SubtypeFilter;
import editor.filter.leaf.options.multi.SupertypeFilter;
import editor.gui.MainFrame;
import editor.gui.settings.SettingsDialog;
import editor.util.UnicodeSymbols;

/**
 * This class represents a dialog that shows the progress for loading the
 * inventory and blocking the main frame until it is finished.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class InventoryLoadDialog extends JDialog
{
    /**
     * This class represents a worker that loads cards from a JSON file in the background.
     *
     * @author Alec Roelke
     */
    private class InventoryLoadWorker extends SwingWorker<Inventory, String>
    {
        /**
         * File to load from.
         */
        private File file;

        /**
         * Create a new InventoryWorker.
         *
         * @param f #File to load
         */
        public InventoryLoadWorker(File f)
        {
            super();
            file = f;

            progressBar.setIndeterminate(true);
            addPropertyChangeListener((e) -> {
                if ("progress".equals(e.getPropertyName()))
                {
                    int p = (Integer)e.getNewValue();
                    progressBar.setIndeterminate(p < 0);
                    progressBar.setValue(p);
                }
            });
        }

        /**
         * Convert a card that has a single face but incorrectly is loaded as a
         * multi-faced card into a card with a {@link CardLayout#NORMAL} layout.
         * 
         * @param card card to convert
         * @return a {@link Card} with the same information as the input but a
         * {@link CardLayout#NORMAL} layout.
         */
        private Card convertToNormal(Card card)
        {
            return new SingleCard(CardLayout.NORMAL,
                                  card.name().get(0),
                                  Optional.of(card.manaCost().get(0).toString()),
                                  Optional.of(new ArrayList<>(card.colors())),
                                  Optional.of(new ArrayList<>(card.colorIdentity())),
                                  Optional.of(card.supertypes()),
                                  card.types(),
                                  Optional.of(card.subtypes()),
                                  Optional.of(card.printedTypes().get(0)),
                                  card.rarity(),
                                  card.expansion(),
                                  Optional.of(card.oracleText().get(0)),
                                  Optional.of(card.flavorText().get(0)),
                                  Optional.of(card.printedText().get(0)),
                                  Optional.of(card.artist().get(0)),
                                  card.multiverseid().get(0),
                                  Optional.of(card.number().get(0)),
                                  Optional.of(card.power().get(0).toString()),
                                  Optional.of(card.toughness().get(0).toString()),
                                  Optional.of(card.loyalty().get(0).toString()),
                                  Optional.of(new TreeMap<>(card.rulings())),
                                  Optional.of(card.legality()));
        }

        /**
         * {@inheritDoc}
         * Import a list of all cards that exist in Magic: the Gathering from a JSON file downloaded from
         * {@link "http://www.mtgjson.com"}.  Also populate the lists of types and expansions (and their blocks).
         *
         * @return The inventory of cards that can be added to a deck.
         */
        @Override
        protected Inventory doInBackground() throws Exception
        {
            final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            publish("Opening " + file.getName() + "...");

            var cards = new ArrayList<Card>();
            var faces = new HashMap<Card, List<String>>();
            var expansions = new HashSet<Expansion>();
            var blockNames = new HashSet<String>();
            var supertypeSet = new HashSet<String>();
            var typeSet = new HashSet<String>();
            var subtypeSet = new HashSet<String>();
            var formatSet = new HashSet<String>();

            // Read the inventory file
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8")))
            {
                publish("Parsing " + file.getName() + "...");
                JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
                int numCards = 0;
                for (var setNode : root.entrySet())
                    for (JsonElement card : setNode.getValue().getAsJsonObject().get("cards").getAsJsonArray())
                        if (card.getAsJsonObject().has("multiverseId"))
                            numCards += 1;

                publish("Reading cards from " + file.getName() + "...");
                setProgress(0);
                for (var setNode : root.entrySet())
                {
                    if (isCancelled())
                    {
                        expansions.clear();
                        blockNames.clear();
                        supertypeSet.clear();
                        typeSet.clear();
                        subtypeSet.clear();
                        formatSet.clear();
                        cards.clear();
                        return new Inventory();
                    }

                    // Create the new Expansion
                    JsonObject setProperties = setNode.getValue().getAsJsonObject();
                    JsonArray setCards = setProperties.get("cards").getAsJsonArray();
                    Expansion set = new Expansion(setProperties.get("name").getAsString(),
                            Optional.ofNullable(setProperties.get("block")).map(JsonElement::getAsString).orElse("<No Block>"),
                            setProperties.get("code").getAsString(),
                            setProperties.get(setProperties.has("oldCode") ? "oldCode" : "code").getAsString(),
                            setProperties.get(setProperties.has("magicCardsInfoCode") ? "magicCardsInfoCode" : "code").getAsString().toUpperCase(),
                            setProperties.get(setProperties.has("gathererCode") ? "gathererCode" : "code").getAsString(),
                            setCards.size(),
                            LocalDate.parse(setProperties.get("releaseDate").getAsString(), Expansion.DATE_FORMATTER));
                    expansions.add(set);
                    blockNames.add(set.block);
                    publish("Loading cards from " + set + "...");

                    for (JsonElement cardElement : setCards)
                    {
                        // Create the new card for the expansion
                        JsonObject card = cardElement.getAsJsonObject();

                        // Card's multiverseid.  Skip cards that aren't in gatherer
                        long multiverseid = Optional.ofNullable(card.get("multiverseId")).map(JsonElement::getAsLong).orElse(-1L);
                        if (multiverseid < 0)
                            continue;

                        // Card's name
                        String name = card.get("name").getAsString();

                        // If the card is a token, skip it
                        CardLayout layout;
                        try
                        {
                            layout = CardLayout.valueOf(card.get("layout").getAsString().toUpperCase().replaceAll("[^A-Z]", "_"));
                        }
                        catch (IllegalArgumentException e)
                        {
                            errors.add(name + " (" + set + "): " + e.getMessage());
                            continue;
                        }

                        Card c = new SingleCard(layout,
                                name,
                                Optional.ofNullable(card.get("manaCost")).map(JsonElement::getAsString),
                                Optional.ofNullable(card.get("colors")).map((e) -> {
                                    var colors = new ArrayList<ManaType>();
                                    for (JsonElement colorElement : e.getAsJsonArray())
                                        colors.add(ManaType.parseManaType(colorElement.getAsString()));
                                    return colors;
                                }),
                                Optional.ofNullable(card.get("colorIdentity")).map((e) -> {
                                    var colorIdentity = new ArrayList<ManaType>();
                                    for (JsonElement identityElement : e.getAsJsonArray())
                                        colorIdentity.add(ManaType.parseManaType(identityElement.getAsString()));
                                    return colorIdentity;
                                }),
                                Optional.ofNullable(card.get("supertypes")).map((e) -> {
                                    var supertypes = new LinkedHashSet<String>();
                                    for (JsonElement superElement : e.getAsJsonArray())
                                        supertypes.add(superElement.getAsString());
                                    return supertypes;
                                }),
                                Optional.ofNullable(card.get("types")).map((e) -> {
                                    var types = new LinkedHashSet<String>();
                                    for (JsonElement typeElement : e.getAsJsonArray())
                                        types.add(typeElement.getAsString());
                                    return types;
                                }).get(),
                                Optional.ofNullable(card.get("subtypes")).map((e) -> {
                                    var subtypes = new LinkedHashSet<String>();
                                    for (JsonElement subElement : e.getAsJsonArray())
                                        subtypes.add(subElement.getAsString());
                                    return subtypes;
                                }),
                                Optional.ofNullable(card.get("originalType")).map(JsonElement::getAsString),
                                Rarity.parseRarity(card.get("rarity").getAsString()),
                                set,
                                Optional.ofNullable(card.get("text")).map(JsonElement::getAsString),
                                Optional.ofNullable(card.get("flavorText")).map(JsonElement::getAsString),
                                Optional.ofNullable(card.get("originalText")).map(JsonElement::getAsString),
                                Optional.ofNullable(card.get("artist")).map(JsonElement::getAsString),
                                multiverseid,
                                Optional.ofNullable(card.get("number")).map(JsonElement::getAsString),
                                Optional.ofNullable(card.get("power")).map(JsonElement::getAsString),
                                Optional.ofNullable(card.get("toughness")).map(JsonElement::getAsString),
                                Optional.ofNullable(card.get("loyalty")).map((e) -> e.isJsonNull() ? "X" : e.getAsString()),
                                Optional.ofNullable(card.get("rulings")).map((e) -> {
                                    var r = new TreeMap<Date, List<String>>();
                                    for (JsonElement l : e.getAsJsonArray())
                                    {
                                        JsonObject o = l.getAsJsonObject();
                                        String ruling = o.get("text").getAsString();
                                        try
                                        {
                                            Date date = format.parse(o.get("date").getAsString());
                                            if (!r.containsKey(date))
                                                r.put(date, new ArrayList<>());
                                            r.get(date).add(ruling);
                                        }
                                        catch (ParseException x)
                                        {
                                            errors.add(name + " (" + set + "): " + x.getMessage());
                                        }
                                    }
                                    return r;
                                }),
                                Optional.ofNullable(card.get("legalities")).map((e) -> {
                                    var l = new HashMap<String, Legality>();
                                    for (var entry : e.getAsJsonObject().entrySet())
                                        l.put(entry.getKey(), Legality.parseLegality(entry.getValue().getAsString()));
                                    return l;
                                }));
                        supertypeSet.addAll(c.supertypes());
                        typeSet.addAll(c.types());
                        subtypeSet.addAll(c.subtypes());
                        formatSet.addAll(c.legality().keySet().stream().map((e) -> e.substring(0, 1).toUpperCase() + e.substring(1)).collect(Collectors.toSet()));

                        // Collect unexpected card values
                        if (c.artist().stream().anyMatch(String::isEmpty))
                            errors.add(c.unifiedName() + " (" + c.expansion() + "): Missing artist!");

                        // Add to map of faces if the card has multiple faces
                        if (layout.isMultiFaced)
                        {
                            var names = new ArrayList<String>();
                            for (JsonElement e : card.get("names").getAsJsonArray())
                                names.add(e.getAsString());
                            faces.put(c, names);
                        }

                        cards.add(c);
                        setProgress(cards.size()*100/numCards);
                    }
                }

                publish("Processing multi-faced cards...");
                List<Card> facesList = new ArrayList<>(faces.keySet());
                while (!facesList.isEmpty())
                {
                    boolean error = false;

                    Card face = facesList.remove(0);
                    var faceNames = faces.get(face);
                    var otherFaces = new ArrayList<Card>();
                    for (Card c : facesList)
                        if (faceNames.contains(c.unifiedName()) && c.expansion().equals(face.expansion()))
                            otherFaces.add(c);
                    facesList.removeAll(otherFaces);
                    otherFaces.add(face);
                    cards.removeAll(otherFaces);

                    otherFaces.sort(Comparator.comparingInt((a) -> faceNames.indexOf(a.unifiedName())));
                    switch (face.layout())
                    {
                    case SPLIT: case AFTERMATH: case ADVENTURE:
                        if (otherFaces.size() < 2)
                        {
                            errors.add(face.toString() + " (" + face.expansion() + "): Can't find other face(s) for split card.");
                            error = true;
                        }
                        else
                        {
                            for (Card f : otherFaces)
                            {
                                if (f.layout() != face.layout())
                                {
                                    errors.add(face.toString() + " (" + face.expansion() + "): Can't join non-split faces into a split card.");
                                    error = true;
                                }
                            }
                        }
                        if (!error)
                            cards.add(new SplitCard(otherFaces));
                        else
                            for (Card f : otherFaces)
                                cards.add(convertToNormal(f));
                        break;
                    case FLIP:
                        if (otherFaces.size() < 2)
                        {
                            errors.add(face.toString() + " (" + face.expansion() + "): Can't find other side of flip card.");
                            error = true;
                        }
                        else if (otherFaces.size() > 2)
                        {
                            errors.add(face.toString() + " (" + face.expansion() + "): Too many sides for flip card.");
                            error = true;
                        }
                        else if (otherFaces.get(0).layout() != CardLayout.FLIP || otherFaces.get(1).layout() != CardLayout.FLIP)
                        {
                            errors.add(face.toString() + " (" + face.expansion() + "): Can't join non-flip faces into a flip card.");
                            error = true;
                        }
                        if (!error)
                            cards.add(new FlipCard(otherFaces.get(0), otherFaces.get(1)));
                        else
                            for (Card f : otherFaces)
                                cards.add(convertToNormal(f));
                        break;
                    case TRANSFORM:
                        if (otherFaces.size() < 2)
                        {
                            errors.add(face.toString() + " (" + face.expansion() + "): Can't find other face of double-faced card.");
                            error = true;
                        }
                        else if (otherFaces.size() > 2)
                        {
                            errors.add(face.toString() + " (" + face.expansion() + "): Too many faces for double-faced card.");
                            error = true;
                        }
                        else if (otherFaces.get(0).layout() != CardLayout.TRANSFORM || otherFaces.get(1).layout() != CardLayout.TRANSFORM)
                        {
                            errors.add(face.toString() + " (" + face.expansion() + "): Can't join single-faced cards into double-faced cards.");
                            error = true;
                        }
                        if (!error)
                            cards.add(new TransformCard(otherFaces.get(0), otherFaces.get(1)));
                        else
                            for (Card f : otherFaces)
                                cards.add(convertToNormal(f));
                        break;
                    case MELD:
                        if (otherFaces.size() < 3)
                        {
                            errors.add(face.toString() + " (" + face.expansion() + "): Can't find some faces of meld card.");
                            error = true;
                        }
                        else if (otherFaces.size() > 3)
                        {
                            errors.add(face.toString() + " (" + face.expansion() + "): Too many faces for meld card.");
                            error = true;
                        }
                        else if (otherFaces.get(0).layout() != CardLayout.MELD || otherFaces.get(1).layout() != CardLayout.MELD || otherFaces.get(2).layout() != CardLayout.MELD)
                        {
                            errors.add(face.toString() + " (" + face.expansion() + "): Can't join single-faced cards into meld cards.");
                            error = true;
                        }
                        if (!error)
                        {
                            cards.add(new MeldCard(otherFaces.get(0), otherFaces.get(2), otherFaces.get(1)));
                            cards.add(new MeldCard(otherFaces.get(2), otherFaces.get(0), otherFaces.get(1)));
                        }
                        else
                            for (Card f : otherFaces)
                                cards.add(convertToNormal(f));
                        break;
                    default:
                        break;
                    }
                }

                publish("Removing duplicate entries...");
                var unique = new HashMap<Long, Card>();
                for (Card c : cards)
                    if (!unique.containsKey(c.multiverseid().get(0)))
                        unique.put(c.multiverseid().get(0), c);
                cards = new ArrayList<>(unique.values());

                // Store the lists of expansion and block names and types and sort them alphabetically
                Expansion.expansions = expansions.stream().sorted().toArray(Expansion[]::new);
                Expansion.blocks = blockNames.stream().sorted().toArray(String[]::new);
                SupertypeFilter.supertypeList = supertypeSet.stream().sorted().toArray(String[]::new);
                CardTypeFilter.typeList = typeSet.stream().sorted().toArray(String[]::new);
                SubtypeFilter.subtypeList = subtypeSet.stream().sorted().toArray(String[]::new);
                LegalityFilter.formatList = formatSet.stream().sorted().toArray(String[]::new);
            }

            Inventory inventory = new Inventory(cards);

            if (Files.exists(Path.of(SettingsDialog.settings().inventory.tags)))
            {
                @SuppressWarnings("unchecked")
                var rawTags = (Map<Long, Set<String>>)MainFrame.SERIALIZER.fromJson(String.join("\n", Files.readAllLines(Path.of(SettingsDialog.settings().inventory.tags))), new TypeToken<Map<Long, Set<String>>>() {}.getType());
                Card.tags.clear();
                Card.tags.putAll(rawTags.entrySet().stream().collect(Collectors.toMap((e) -> inventory.get(e.getKey()), Map.Entry::getValue)));
            }

            return inventory;
        }

        /**
         * {@inheritDoc}
         * Close the dialog and allow it to return the Inventory
         * that was created.
         */
        @Override
        protected void done()
        {
            setVisible(false);
            dispose();
            if (SettingsDialog.settings().inventory.warn && !errors.isEmpty())
            {
                SwingUtilities.invokeLater(() -> {
                    StringJoiner join = new StringJoiner("<li>", "<html>", "</ul></html>");
                    join.add("Errors ocurred while loading the following card(s):<ul style=\"margin-top:0;margin-left:20pt\">");
                    for (String failure : errors)
                        join.add(failure);
                    JPanel warningPanel = new JPanel(new BorderLayout());
                    JLabel warningLabel = new JLabel(join.toString());
                    warningPanel.add(warningLabel, BorderLayout.CENTER);
                    JCheckBox suppressBox = new JCheckBox("Don't show this warning in the future");
                    warningPanel.add(suppressBox, BorderLayout.SOUTH);
                    JOptionPane.showMessageDialog(null, warningPanel, "Warning", JOptionPane.WARNING_MESSAGE);
                });
            }
        }

        /**
         * {@inheritDoc}
         * Change the label in the dialog to match the stage this worker is in.
         */
        @Override
        protected void process(List<String> chunks)
        {
            for (String chunk : chunks)
            {
                progressLabel.setText(chunk);
                progressArea.append(chunk + "\n");
            }
        }
    }

    /**
     * List of errors that occurred while loading cards.
     */
    private List<String> errors;
    /**
     * Area showing past and current progress of loading.
     */
    private JTextArea progressArea;
    /**
     * Progress bar showing overall progress of loading.
     */
    private JProgressBar progressBar;
    /**
     * Label showing the current stage of loading.
     */
    private JLabel progressLabel;

    /**
     * Worker that loads the inventory.
     */
    private InventoryLoadWorker worker;

    /**
     * Create a new InventoryLoadDialog over the given {@link JFrame}.
     *
     * @param owner owner of the new InventoryLoadDialog
     */
    public InventoryLoadDialog(JFrame owner)
    {
        super(owner, "Loading Inventory", Dialog.ModalityType.APPLICATION_MODAL);
        setPreferredSize(new Dimension(350, 220));
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        worker = null;
        errors = new ArrayList<>();

        // Content panel
        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[]{0};
        layout.columnWeights = new double[]{1.0};
        layout.rowHeights = new int[]{0, 0, 0, 0};
        layout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0};
        JPanel contentPanel = new JPanel(layout);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(contentPanel);

        // Stage progress label
        progressLabel = new JLabel("Loading inventory...");
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.fill = GridBagConstraints.BOTH;
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 0;
        labelConstraints.insets = new Insets(0, 0, 2, 0);
        contentPanel.add(progressLabel, labelConstraints);

        // Overall progress bar
        progressBar = new JProgressBar();
        GridBagConstraints barConstraints = new GridBagConstraints();
        barConstraints.fill = GridBagConstraints.BOTH;
        barConstraints.gridx = 0;
        barConstraints.gridy = 1;
        barConstraints.insets = new Insets(0, 0, 2, 0);
        contentPanel.add(progressBar, barConstraints);

        // History text area
        progressArea = new JTextArea();
        progressArea.setEditable(false);
        GridBagConstraints areaConstraints = new GridBagConstraints();
        areaConstraints.fill = GridBagConstraints.BOTH;
        areaConstraints.gridx = 0;
        areaConstraints.gridy = 2;
        areaConstraints.insets = new Insets(0, 0, 10, 0);
        contentPanel.add(new JScrollPane(progressArea), areaConstraints);

        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((e) -> {
            if (worker != null)
                worker.cancel(false);
        });
        GridBagConstraints cancelConstraints = new GridBagConstraints();
        cancelConstraints.gridx = 0;
        cancelConstraints.gridy = 3;
        contentPanel.add(cancelButton, cancelConstraints);

        pack();
    }

    /**
     * Make this dialog visible and then begin loading the inventory.  Block until it is
     * complete, and then return the newly-created Inventory.
     *
     * @return the #Inventory that was created.
     */
    public Inventory createInventory(File file)
    {
        worker = new InventoryLoadWorker(file);
        worker.execute();
        setVisible(true);
        progressArea.setText("");
        try
        {
            return worker.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            JOptionPane.showMessageDialog(null, "Error loading inventory: " + e.getCause().getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return new Inventory();
        }
        catch (CancellationException e)
        {
            return new Inventory();
        }
    }
}
