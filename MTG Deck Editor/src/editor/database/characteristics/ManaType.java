package editor.database.characteristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * This enum represents one of the five colors of Magic: The Gathering.
 * 
 * @author Alec Roelke
 */
public enum ManaType
{
	WHITE("White", 'W'),
	BLUE("Blue", 'U'),
	BLACK("Black", 'B'),
	RED("Red", 'R'),
	GREEN("Green", 'G'),
	
	COLORLESS("Colorless", 'C');
	
	/**
	 * @return The number of colors of Magic, which is the number of ManaTypes
	 * minus the number of ManaTypes that do not represent colors.
	 */
	public static int colors()
	{
		return values().length - 1;
	}
	
	/**
	 * Get a ManaType from a String.  Acceptable values are "white," "w," "blue,"
	 * "u," "black," "b," "red," "r," "green," or "g," case insensitive.
	 * 
	 * @param color String to create an ManaType from.
	 * @return ManaType that corresponds to the String.
	 */
	public static ManaType get(String color)
	{
		for (ManaType c: ManaType.values())
			if (c.color.equalsIgnoreCase(color) || color.equalsIgnoreCase(String.valueOf(c.shorthand)))
				return c;
		throw new IllegalArgumentException("Illegal color string \"" + color + "\"");
	}
	
	/**
	 * Get a ManaType from a character.  Acceptable characters are 'w,' 'u,' 'b,'
	 * 'r,' or 'g,' case insensitive.
	 * 
	 * @param color Character to get a color from.
	 * @return ManaType that corresponds to the character.
	 */
	public static ManaType get(char color)
	{
		for (ManaType c: ManaType.values())
			if (Character.toLowerCase(c.shorthand) == Character.toLowerCase(color))
				return c;
		throw new IllegalArgumentException("Illegal color shorthand '" + color + "'");
	}
	
	/**
	 * Sort a list of ManaTypes in color order.  If the list contains two colors, it will be
	 * sorted according to how they appear on a card.  Otherwise, it will be sorted according
	 * to CWUBRG order.  It is recommended to use this rather than using Java's built-in sorting
	 * functions.  If the list contains any duplicate colors, they will be removed.
	 * 
	 * @param colors List of ManaTypes to sort.
	 */
	public static void sort(List<ManaType> colors)
	{
		if (!colors.isEmpty())
		{
			Tuple t = new Tuple(colors);
			colors.clear();
			colors.addAll(t);
		}
	}
	
	/**
	 * This class represents a sorted list of unique colors in the correct order around
	 * the color pie.
	 * 
	 * @author Alec Roelke
	 */
	public static class Tuple extends editor.util.Tuple<ManaType> implements Comparable<Tuple>
	{
		/**
		 * Helper method for cleaning and sorting a collection of colors before calling the
		 * super constructor on it.
		 * 
		 * @param cols Collection of colors to sort
		 * @return A cleaned and sorted copy of the given collection of colors.  Each color
		 * will only appear once.
		 */
		private static List<ManaType> sorted(Collection<ManaType> cols)
		{
			List<ManaType> colors = new ArrayList<ManaType>(new HashSet<ManaType>(cols));
			boolean colorless = colors.remove(COLORLESS);
			Collections.sort(colors);
			switch (colors.size())
			{
			case 0:
				break;
			case 2:
				if (colors.get(0).colorOrder(colors.get(1)) > 0)
					Collections.reverse(colors);
				break;
			case 3:
				while (colors.get(0).distance(colors.get(1)) != colors.get(1).distance(colors.get(2)))
					Collections.rotate(colors, 1);
				break;
			case 4:
				boolean equal;
				do
				{
					equal = true;
					for (int i = 0; i < 3; i++)
					{
						if (colors.get(i).distance(colors.get(i + 1)) != 1)
						{
							equal = false;
							Collections.rotate(colors, 1);
							break;
						}
					}
				} while (!equal);
				break;
			default:
				break;
			}
			if (colorless)
				colors.add(0, COLORLESS);
			return colors;
		}
		
		/**
		 * Create a new tuple out of the given list of colors.  Unique colors will be extracted
		 * and then sorted around the color pie.
		 * 
		 * @param cols Colors to make the tuple out of
		 */
		public Tuple(Collection<ManaType> cols)
		{
			super(sorted(cols));
		}
		
		/**
		 * Create a new tuple out of the given colors.
		 * 
		 * @param cols Colors to make the tuple out of
		 */
		public Tuple(ManaType... cols)
		{
			this(Arrays.asList(cols));
		}
		
		/**
		 * @param other Tuple to compare with
		 * @return A negative number if this tuple comes before the other one, which happens
		 * if this one has fewer elements or if the colors come before the other one's colors,
		 * 0 if both tuples are the same, and a positive number otherwise.
		 */
		@Override
		public int compareTo(Tuple other)
		{
			int diff = size() - other.size();
			if (diff == 0)
				for (int i = 0; i < size(); i++)
					diff += get(i).compareTo(other.get(i))*Math.pow(10, size() - i);
			return diff;
		}
		
		/**
		 * @param other Object to compare with
		 * @return <code>true</code> if the other object is a Tuple with the same colors
		 * as this one, and <code>false</code> otherwise.
		 */
		@Override
		public boolean equals(Object other)
		{
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (!(other instanceof Tuple))
				return false;
			Tuple t = (Tuple)other;
			return containsAll(t) && t.containsAll(this);
		}
		
		/**
		 * @return An int representation of this Tuple.
		 */
		@Override
		public int hashCode()
		{
			int h = Integer.MAX_VALUE;
			for (ManaType col: this)
				h ^= col.hashCode();
			return h;
		}
	}
	
	/**
	 * String representation of this ManaType.
	 */
	private final String color;
	/**
	 * Single-character shorthand for this ManaType.
	 */
	private final char shorthand;
	
	/**
	 * Create a new ManaType.
	 * 
	 * @param c String representation of the new ManaType
	 * @param s Single-character shorthand representation of the new ManaType
	 */
	private ManaType(final String c, final char s)
	{
		color = c;
		shorthand = s;
	}
	
	/**
	 * @return A one-character shorthand for the name of this ManaType.
	 */
	public char shorthand()
	{
		return shorthand;
	}
	
	/**
	 * @return A String representation of this ManaType (its name).
	 */
	@Override
	public String toString()
	{
		return color;
	}
	
	/**
	 * @param other ManaType to compare to
	 * @return A negative number if this ManaType should come before the other, 0 if they are the same,
	 * or a postive number if it should come after.  Typically this follows WUBRG order, but if the
	 * distance is too great (like white and green), then the order is reversed.
	 */
	public int colorOrder(ManaType other)
	{
		if (this == COLORLESS && other == COLORLESS)
			return 0;
		else if (this == COLORLESS)
			return -1;
		else if (other == COLORLESS)
			return 1;
		else
		{
			int diff = compareTo(other);
			return Math.abs(diff) <= 2 ? diff : -diff;
		}
	}
	
	/**
	 * @param other ManaType to compare to
	 * @return The distance around the color pie from this color to the other color.
	 */
	public int distance(ManaType other)
	{
		if (this == COLORLESS || other == COLORLESS)
			throw new IllegalArgumentException("Colorless is not a color");
		return (other.ordinal() - ordinal() + colors())%colors();
	}
}
