package editor.collection.category;

import java.awt.Color;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.filter.FilterGroup;
import editor.gui.MainFrame;

/**
 * This class represents a set of specifications for a category.  Those specifications are its name,
 * the lists of cards to include or exclude regardless of filter, its color, its filter, and its String
 * representation.
 *
 * @author Alec Roelke
 */
public class CategorySpec implements Externalizable
{
    private static final Map<String, String> CODES = Map.ofEntries(new AbstractMap.SimpleImmutableEntry<>(FilterFactory.TYPE, "cardtype"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.ALL, "*"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.FORMAT_LEGALITY, "legal"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.TYPE_LINE, "type"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.BLOCK, "b"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.EXPANSION, "x"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.LAYOUT, "L"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.MANA_COST, "m"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.NAME, "n"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.NONE, "0"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.RARITY, "r"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.SUBTYPE, "sub"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.SUPERTYPE, "super"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.TAGS, "tag"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.LOYALTY, "l"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.ARTIST, "a"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.CARD_NUMBER, "#"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.CMC, "cmc"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.COLOR, "c"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.COLOR_IDENTITY, "ci"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.FLAVOR_TEXT, "f"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.POWER, "p"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.PRINTED_TEXT, "ptext"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.PRINTED_TYPES, "ptypes"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.RULES_TEXT, "o"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.TOUGHNESS, "t"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.GROUP, "group"),
                                                                   new AbstractMap.SimpleImmutableEntry<>(FilterFactory.DEFAULTS, ""));

    /**
     * This class represents an event that changes a {@link CategorySpec}.  It can tell
     * which {@link CategorySpec} changed, and which of its parameters changed as a result
     * of the event. Use the *changed methods to tell if a parameter changed.  If a
     * parameter did not change and its old or new value is requested from this CategoryEvent,
     * an {@link IllegalStateException} will be thrown.
     *
     * @author Alec Roelke
     */
    @SuppressWarnings("serial")
    public class Event extends EventObject
    {
        /**
         * Old state of the category specification.
         */
        private CategorySpec oldSpec;
        /**
         * New state of the category specification.
         */
        private CategorySpec newSpec;

        /**
         * Create a new Event showing no changes.
         *
         * @param old state of the specification before it changed
         */
        public Event(CategorySpec old, CategorySpec spec)
        {
            super(CategorySpec.this);
            oldSpec = old;
            newSpec = spec;
        }

        /**
         * Check if the blacklist of the category was changed.
         *
         * @return true if the event that generated this CategoryEvent changed the
         * category's blacklist.
         */
        public boolean blacklistChanged()
        {
            return !oldSpec.getBlacklist().equals(newSpec.getBlacklist());
        }

        /**
         * Check if the category's color changed.
         *
         * @return true if the event that generated this CategoryEvent changed the
         * category's color, and false otherwise.
         */
        public boolean colorChanged()
        {
            return !oldSpec.getColor().equals(newSpec.getColor());
        }

        /**
         * Check if the category's filter changed.
         *
         * @return true if the category's filter changed as a result of the event
         * that generated this CategoryEvent, and false otherwise.
         */
        public boolean filterChanged()
        {
            return !oldSpec.getFilter().equals(newSpec.getFilter());
        }

        @Override
        public CategorySpec getSource()
        {
            return CategorySpec.this;
        }

        /**
         * Check if the category's name changed.
         *
         * @return true if the category's name changed, and false otherwise.
         */
        public boolean nameChanged()
        {
            return !oldSpec.getName().equals(newSpec.getName());
        }

        /**
         * Get a copy of the category specification as it was before it changed.
         *
         * @return the old specification
         */
        public CategorySpec newSpec()
        {
            return newSpec;
        }

        /**
         * Get a copy of the category specification after the change.
         *
         * @return the new specification
         */
        public CategorySpec oldSpec()
        {
            return oldSpec;
        }

        /**
         * Check if the category's whitelist changed.
         *
         * @return true if the category's whitelist changed as a result of the event
         * that generated this CategoryEvent, and false otherwise.
         */
        public boolean whitelistChanged()
        {
            return !oldSpec.getWhitelist().equals(newSpec.getWhitelist());
        }
    }

    /**
     * Name of the category.
     */
    private String name;
    /**
     * List of cards to include in the category regardless of filter.
     */
    private Set<Card> whitelist;
    /**
     * List of cards to exclude from the category regardless of filter.
     */
    private Set<Card> blacklist;
    /**
     * Color of the category.
     */
    private Color color;
    /**
     * Filter of the category.
     */
    private Filter filter;

    /**
     * Collection of listeners listening for changes in this CategorySpec.
     */
    private Collection<CategoryListener> listeners;

    /**
     * Create a new CategorySpec with the color black and a filter that passes all cards.
     */
    public CategorySpec()
    {
        this("All Cards", Color.BLACK, FilterFactory.createFilter(FilterFactory.ALL));
    }

    /**
     * Copy constructor for CategorySpec, except the copy has no listeners.
     *
     * @param original original CategorySpec to copy
     */
    public CategorySpec(CategorySpec original)
    {
        name = original.name;
        whitelist = new HashSet<>(original.whitelist);
        blacklist = new HashSet<>(original.blacklist);
        color = original.color;
        filter = original.filter.copy();
        listeners = new HashSet<>();
    }

    /**
     * Create a new CategorySpec with the given specifications.
     *
     * @param name      name of the new spec
     * @param whitelist whitelist of the new spec
     * @param blacklist blacklist of the new spec
     * @param color     color of the new spec
     * @param filter    filter of the new spec
     */
    public CategorySpec(String name, Collection<Card> whitelist, Collection<Card> blacklist, Color color, Filter filter)
    {
        this.name = name;
        this.whitelist = new HashSet<>(whitelist);
        this.blacklist = new HashSet<>(blacklist);
        this.color = color;
        this.filter = filter;
        listeners = new HashSet<>();
    }

    /**
     * Create a new CategorySpec with the given specifications and an empty white- and
     * blacklist.
     *
     * @param name   name of the new spec
     * @param color  color of the new spec
     * @param filter filter of the new spec
     */
    public CategorySpec(String name, Color color, Filter filter)
    {
        this(name, new HashSet<>(), new HashSet<>(), color, filter);
    }

    /**
     * Add a new listener for changes in this CategorySpec.
     *
     * @param listener new listener to add
     */
    public void addCategoryListener(CategoryListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Copy the name, whitelist, blacklist, color, and filter from the given
     * CategorySpec, discarding those values from this one, alerting any
     * listeners of this event.
     *
     * @param other CategorySpec to copy
     * @return true if any changes were made to this CategorySpec, and false
     * otherwise.
     */
    public boolean copy(CategorySpec other)
    {
        CategorySpec old = new CategorySpec(this);

        name = other.name;
        whitelist.clear();
        whitelist.addAll(other.whitelist);
        blacklist.clear();
        blacklist.addAll(other.blacklist);
        color = other.color;
        filter = new FilterGroup();
        filter = other.filter.copy();

        if (!equals(old))
        {
            Event e = new Event(old, new CategorySpec(this));
            for (CategoryListener listener : listeners)
                listener.categoryChanged(e);
        }

        return !equals(old);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (!(other instanceof CategorySpec))
            return false;
        CategorySpec o = (CategorySpec)other;
        return name.equals(o.name) && color.equals(o.color) && filter.equals(o.filter)
                && blacklist.equals(o.blacklist) && whitelist.equals(o.whitelist);
    }

    /**
     * Exclude a card from the category, even if it passes through the filter.
     *
     * @param c card to exclude
     * @return true if the card was successfully excluded (it was added to the
     * blacklist or removed from the whitelist), and false otherwise.
     */
    public boolean exclude(Card c)
    {
        CategorySpec old = new CategorySpec(this);

        if (filter.test(c))
            blacklist.add(c);
        whitelist.remove(c);

        Event e = new Event(old, new CategorySpec(this));
        if (e.whitelistChanged() || e.blacklistChanged())
            for (CategoryListener listener : listeners)
                listener.categoryChanged(e);

        return e.whitelistChanged() || e.blacklistChanged();
    }

    /**
     * Get the set of cards that should not be included in the category,
     * even if they pass through the filter.
     *
     * @return the set of cards that explicitly must never pass through the
     * filter
     */
    public Set<Card> getBlacklist()
    {
        return new HashSet<>(blacklist);
    }

    /**
     * Get the category's color.
     *
     * @return the Color of the category.
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * Get the category's filter for automatically including cards.
     *
     * @return the filter of the category.
     */
    public Filter getFilter()
    {
        return filter;
    }

    /**
     * Get the name of the category this CategorySpec represents.
     *
     * @return the name of the category.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the set of cards that should be included in the category even
     * if they don't pass through the filter.
     *
     * @return the set of cards that explicitly must pass through the filter.
     */
    public Set<Card> getWhitelist()
    {
        return new HashSet<>(whitelist);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, color, filter, blacklist, whitelist);
    }

    /**
     * Include a card in the category, even if it doesn't pass through the filter.
     *
     * @param c card to include
     * @return true if the Card was successfully included (either it was added to
     * the whitelist or removed from the blacklist), and false otherwise.
     */
    public boolean include(Card c)
    {
        CategorySpec old = new CategorySpec(this);

        if (!filter.test(c))
            whitelist.add(c);
        blacklist.remove(c);

        Event e = new Event(old, new CategorySpec(this));
        if (e.whitelistChanged() || e.blacklistChanged())
            for (CategoryListener listener : listeners)
                listener.categoryChanged(e);

        return e.whitelistChanged() || e.blacklistChanged();
    }

    /**
     * Check if this CategorySpec's filter includes a card.
     *
     * @param c card to test for inclusion
     * @return true if this CategorySpec includes the given card, and false otherwise.
     */
    public boolean includes(Card c)
    {
        return (filter.test(c) || whitelist.contains(c)) && !blacklist.contains(c);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        int n;

        blacklist.clear();
        whitelist.clear();

        name = in.readUTF();
        color = (Color)in.readObject();

        String code = in.readUTF();
        for (String type: CODES.keySet())
        {
            if (code.equals(CODES.get(type)))
            {
                if (type.equals(FilterFactory.GROUP))
                    filter = new FilterGroup();
                else
                    filter = FilterFactory.createFilter(type);
                filter.readExternal(in);
                break;
            }
        }

        n = in.readInt();
        for (int i = 0; i < n; i++)
            blacklist.add(MainFrame.inventory().get(in.readUTF()));
        n = in.readInt();
        for (int i = 0; i < n; i++)
            whitelist.add(MainFrame.inventory().get(in.readUTF()));
    }

    /**
     * Remove a listener from this CategorySpec's list.
     *
     * @param listener Listener to remove
     * @return <code>true</code> if the listener was successfully
     * removed from the list, and <code>false</code> otherwise.
     */
    public boolean removeCategoryListener(CategoryListener listener)
    {
        return listeners.remove(listener);
    }

    /**
     * Set the Color of the category, and alert any listeners of this event.
     *
     * @param c new Color for the category
     */
    public void setColor(Color c)
    {
        if (!color.equals(c))
        {
            CategorySpec old = new CategorySpec(this);

            color = c;

            Event e = new Event(old, new CategorySpec(this));
            for (CategoryListener listener : listeners)
                listener.categoryChanged(e);
        }
    }

    /**
     * Change the filter of the category so a new set of cards is automatically
     * included, and alert any listeners of this event.
     *
     * @param f new filter for the category
     */
    public void setFilter(Filter f)
    {
        if (!filter.equals(f))
        {
            CategorySpec old = new CategorySpec(this);

            filter = f;

            Event e = new Event(old, new CategorySpec(this));
            for (CategoryListener listener : listeners)
                listener.categoryChanged(e);
        }
    }

    /**
     * Change the name of the category and update any listeners of this event.
     *
     * @param n new name for the category
     */
    public void setName(String n)
    {
        if (!name.equals(n))
        {
            CategorySpec old = new CategorySpec(this);

            name = n;

            Event e = new Event(old, new CategorySpec(this));
            for (CategoryListener listener : listeners)
                listener.categoryChanged(e);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeUTF(name);
        out.writeObject(color);

        out.writeUTF(CODES.get(filter.type()));
        filter.writeExternal(out);

        out.writeInt(blacklist.size());
        for (Card card : blacklist)
            out.writeUTF(card.id());
        out.writeInt(whitelist.size());
        for (Card card : whitelist)
            out.writeUTF(card.id());
    }
}
