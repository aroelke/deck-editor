package editor.collection.category;

import java.awt.Color;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.filter.FilterGroup;
import editor.gui.MainFrame;
import editor.gui.SettingsDialog;

/**
 * This class represents a set of specifications for a category.  Those specifications are its name,
 * the lists of cards to include or exclude regardless of filter, its color, its filter, and its String
 * representation.
 * 
 * @author Alec Roelke
 */
public class CategorySpec implements Externalizable
{
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
	 * List separator for UIDs of cards in the String representation of a whitelist or a blacklist.
	 */
	private static final String EXCEPTION_SEPARATOR = ":";	
	
	/**
	 * Regex pattern for matching category strings and extracting their contents.  The first group
	 * will be the category's name, the second group will be the UIDs of the cards in its whitelist,
	 * the third group will the UIDs of the cards in its blacklist, the fourth group will be its color,
	 * and the fifth group will be its filter's String representation.  The first four groups will
	 * not include the group enclosing characters, but the fifth will.  The first through third groups
	 * will be empty strings if they are empty, but the fourth will be null.  The first and fifth groups
	 * should never be empty.
	 */
	private static final Pattern CATEGORY_PATTERN = Pattern.compile(
			"^" + Filter.BEGIN_GROUP + "([^" + Filter.END_GROUP + "]+)" + Filter.END_GROUP		// Name
			+ "\\s*" + Filter.BEGIN_GROUP + "([^" + Filter.END_GROUP + "]*)" + Filter.END_GROUP 	// Whitelist
			+ "\\s*" + Filter.BEGIN_GROUP + "([^" + Filter.END_GROUP + "]*)" + Filter.END_GROUP	// Blacklist
			+ "\\s*" + Filter.BEGIN_GROUP + "(#[0-9A-F-a-f]{6})?" + Filter.END_GROUP						// Color
			+ "\\s*(.*)$");
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
		whitelist = new HashSet<Card>(original.whitelist);
		blacklist = new HashSet<Card>(original.blacklist);
		color = original.color;
		filter = original.filter.copy();
		listeners = new HashSet<CategoryListener>();
	}
	
	/**
	 * Create a new CategorySpec from the given category String representation.
	 * 
	 * @param pattern String to parse
	 * @throws IllegalArgumentException if the String doesn't represent a category
	 */
	public CategorySpec(String pattern) throws IllegalArgumentException
	{
		Matcher m = CATEGORY_PATTERN.matcher(pattern);
		if (m.matches())
		{
			name = m.group(1);
			if (!m.group(2).isEmpty())
				whitelist = Arrays.stream(m.group(2).split(EXCEPTION_SEPARATOR)).map(MainFrame.inventory()::get).collect(Collectors.toSet());
			else
				whitelist = new HashSet<Card>();
			if (!m.group(3).isEmpty())
				blacklist = Arrays.stream(m.group(3).split(EXCEPTION_SEPARATOR)).map(MainFrame.inventory()::get).collect(Collectors.toSet());
			else
				blacklist = new HashSet<Card>();
			if (m.group(4) != null)
				color = SettingsDialog.stringToColor(m.group(4));
			else
			{
				Random rand = new Random();
				color = Color.getHSBColor(rand.nextFloat(), rand.nextFloat(), (float)Math.sqrt(rand.nextFloat()));
			}
			filter = new FilterGroup();
			filter.parse(m.group(5));
			listeners = new HashSet<CategoryListener>();
		}
		else
			throw new IllegalArgumentException("Illegal category string " + pattern);
	}
	
	/**
	 * Create a new CategorySpec with the given specifications.
	 * 
	 * @param name name of the new spec
	 * @param whitelist whitelist of the new spec
	 * @param blacklist blacklist of the new spec
	 * @param color color of the new spec
	 * @param filter filter of the new spec
	 */
	public CategorySpec(String name, Collection<Card> whitelist, Collection<Card> blacklist, Color color, Filter filter)
	{
		this.name = name;
		this.whitelist = new HashSet<Card>(whitelist);
		this.blacklist = new HashSet<Card>(blacklist);
		this.color = color;
		this.filter = filter;
		listeners = new HashSet<CategoryListener>();
	}
	
	/**
	 * Create a new CategorySpec with the given specifications and an empty white- and
	 * blacklist.
	 * 
	 * @param name name of the new spec
	 * @param color color of the new spec
	 * @param filter filter of the new spec
	 */
	public CategorySpec(String name, Color color, Filter filter)
	{
		this(name, new HashSet<Card>(), new HashSet<Card>(), color, filter);
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
		filter.parse(other.filter.toString());
		
		if (!equals(old))
		{
			Event e = new Event(old, new CategorySpec(this));
			for (CategoryListener listener: listeners)
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
		
		blacklist.add(c);
		whitelist.remove(c);
		
		Event e = new Event(old, new CategorySpec(this));
		if (e.whitelistChanged() || e.blacklistChanged())
		{
			for (CategoryListener listener: listeners)
				listener.categoryChanged(e);
		}
		
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
		return new HashSet<Card>(blacklist);
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
		return new HashSet<Card>(whitelist);
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
		
		whitelist.add(c);
		blacklist.remove(c);
		
		Event e = new Event(old, new CategorySpec(this));
		if (e.whitelistChanged() || e.blacklistChanged())
		{
			for (CategoryListener listener: listeners)
				listener.categoryChanged(e);
		}
		
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
		filter = (Filter)in.readObject();
		
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
			for (CategoryListener listener: listeners)
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
			for (CategoryListener listener: listeners)
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
			for (CategoryListener listener: listeners)
				listener.categoryChanged(e);
		}
	}

	/**
	 * Get this CategorySpec's String representation, except with the whitelist and
	 * blacklist empty.  Used for creating preset categories, as they should not have
	 * exclusions or inclusions.
	 * 
	 * @return a String representation of this CategorySpec with the whitelist and
	 * blacklist empty.  
	 */
	public String toListlessString()
	{
		return Filter.BEGIN_GROUP + name + Filter.END_GROUP
				+ " " + Filter.BEGIN_GROUP + Filter.END_GROUP
				+ " " + Filter.BEGIN_GROUP + Filter.END_GROUP
				+ " " + Filter.BEGIN_GROUP + SettingsDialog.colorToString(color, 3) + Filter.END_GROUP
				+ " " + filter.toString();
	}
	
	@Override
	public String toString()
	{
		StringJoiner white = new StringJoiner(EXCEPTION_SEPARATOR, String.valueOf(Filter.BEGIN_GROUP), String.valueOf(Filter.END_GROUP));
		for (Card c: whitelist)
			white.add(c.id());
		StringJoiner black = new StringJoiner(EXCEPTION_SEPARATOR, String.valueOf(Filter.BEGIN_GROUP), String.valueOf(Filter.END_GROUP));
		for (Card c: blacklist)
			black.add(c.id());
		return Filter.BEGIN_GROUP + name + Filter.END_GROUP
				+ " " + white.toString()
				+ " " + black.toString()
				+ " " + Filter.BEGIN_GROUP + SettingsDialog.colorToString(color, 3) + Filter.END_GROUP
				+ " " + filter.toString();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeUTF(name);
		out.writeObject(color);
		out.writeObject(filter);
		out.writeInt(blacklist.size());
		for (Card card: blacklist)
			out.writeUTF(card.id());
		out.writeInt(whitelist.size());
		for (Card card: whitelist)
			out.writeUTF(card.id());
	}
}
