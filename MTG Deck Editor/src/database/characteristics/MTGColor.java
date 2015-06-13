package database.characteristics;

import java.util.Collections;
import java.util.List;

/**
 * This enum represents one of the five colors of Magic: The Gathering.
 * 
 * @author Alec Roelke
 */
public enum MTGColor
{
	WHITE("White"),
	BLUE("Blue"),
	BLACK("Black"),
	RED("Red"),
	GREEN("Green");
	
	/**
	 * Get an MTGColor from a String.  Acceptable values are "white," "w," "blue,"
	 * "u," "black," "b," "red," "r," "green," or "g," case insensitive.
	 * 
	 * @param color String to create an MTGColor from.
	 * @return MTGColor that corresponds to the String.
	 */
	public static MTGColor get(String color)
	{
		for (MTGColor c: MTGColor.values())
			if (c.color.equalsIgnoreCase(color) || color.equalsIgnoreCase(String.valueOf(c.shorthand())))
				return c;
		throw new IllegalArgumentException("Illegal color string \"" + color + "\"");
	}
	
	/**
	 * Get an MTGColor from a character.  Acceptable characters are 'w,' 'u,' 'b,'
	 * 'r,' or 'g,' case insensitive.
	 * 
	 * @param color Character to get a color from.
	 * @return MTGColor that corresponds to the character.
	 */
	public static MTGColor get(char color)
	{
		switch (Character.toLowerCase(color))
		{
		case 'w':
			return WHITE;
		case 'u':
			return BLUE;
		case 'b':
			return BLACK;
		case 'r':
			return RED;
		case 'g':
			return GREEN;
		default:
			throw new IllegalArgumentException("Illegal color shorthand");
		}
	}
	
	/**
	 * Sort a list of MTGColors in color order.  If the list contains two colors, it will be
	 * sorted according to how they appear on a card.  Otherwise, it will be sorted according
	 * to WUBRG order.  It is recommended to use this rather than using Java's built-in sorting
	 * functions.
	 * 
	 * @param colors List of MTGColors to sort.
	 */
	public static void sort(List<MTGColor> colors)
	{
		switch (colors.size())
		{
		case 2:
			Pair col = new Pair(colors.get(0), colors.get(1));
			colors.clear();
			colors.add(col.first);
			colors.add(col.last);
		case 3:
			// TODO: Impose the correct ordering on 3-color lists (assuming there is a single one)
			Collections.sort(colors);
			break;
		case 4:
			// TODO: Impose the correct ordering on 4-color lists (assuming there is one)
			Collections.sort(colors);
			break;
		case 5:
			Collections.sort(colors);
		default:
			break;
		}
	}
	
	/**
	 * String representation of this MTGColor.
	 */
	private final String color;
	
	/**
	 * Create a new MTGColor.
	 * 
	 * @param color String representation of the new MTGColor
	 */
	private MTGColor(final String color)
	{
		this.color = color;
	}
	
	/**
	 * @return A one-character shorthand for the name of this MTGColor.
	 */
	public char shorthand()
	{
		switch (this)
		{
		case WHITE:
			return 'W';
		case BLUE:
			return 'U';
		case BLACK:
			return 'B';
		case RED:
			return 'R';
		case GREEN:
			return 'G';
		default:
			return 'C';
		}
	}
	
	/**
	 * @return A String representation of this MTGColor (its name).
	 */
	@Override
	public String toString()
	{
		return color;
	}
	
	/**
	 * @param other MTGColor to compare to
	 * @return A negative number if this MTGColor should come before the other, 0 if they are the same,
	 * or a postive number if it should come after.  Typically this follows WUBRG order, but if the
	 * distance is too great (like white and green), then the order is reversed.
	 */
	public int colorOrder(MTGColor other)
	{
		int diff = compareTo(other);
		return Math.abs(diff) <= 2 ? diff : -diff;
	}
	
	/**
	 * This class represents a pair of colors that is sorted according to color order.
	 * 
	 * @author Alec Roelke
	 */
	public static class Pair implements Comparable<Pair>
	{
		/**
		 * First MTGColor in the pair.
		 */
		public final MTGColor first;
		/**
		 * Last MTGColor in the pair.
		 */
		public final MTGColor last;
		
		/**
		 * Create a new Pair of MTGColors, sorted according to their color order.
		 * 
		 * @param col1 One of the colors.
		 * @param col2 The other color.
		 * @see database.characteristics.MTGColor#colorOrder(MTGColor)
		 */
		public Pair(MTGColor col1, MTGColor col2)
		{
			if (col1.colorOrder(col2) < 0)
			{
				first = col1;
				last = col2;
			}
			else
			{
				first = col2;
				last = col1;
			}
		}
		
		/**
		 * @param other Object to compare to.
		 * @return <code>true</code> if the other object is an MTGColor and is the same color, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean equals(Object other)
		{
			if (other == this)
				return true;
			if (other == null)
				return false;
			if (!(other instanceof Pair))
				return false;
			Pair p = (Pair)other;
			return first.equals(p.first) && last.equals(p.last); 
		}
		
		/**
		 * @return an unique identifier for this MTGColor pair.
		 */
		@Override
		public int hashCode()
		{
			return first.hashCode()^last.hashCode();
		}

		/**
		 * @param other Pair to compare to.
		 * @return A negative number if this Pair should come before the other one, 0 if they have
		 * the same ordering, and a positive if it should come after.
		 */
		@Override
		public int compareTo(Pair other)
		{
			return first.colorOrder(other.first)*100 + last.colorOrder(other.last);
		}
	}
}
