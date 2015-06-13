package database.characteristics;

/**
 * This class represents an expansion set of Magic: The Gathering cards.  It has a name, a block, a code,
 * and a number of cards.
 * 
 * @author Alec
 */
public class Expansion
{
	/**
	 * Array containing all expansion names.
	 */
	public static String[] expansions = {};
	/**
	 * Array containing all block names.
	 */
	public static String[] blocks = {};
	
	/**
	 * This Expansion's name.
	 */
	public final String name;
	/**
	 * Name of the block this Expansion belongs to (empty if there is none).
	 */
	public final String block;
	/**
	 * This Expansion's code.
	 */
	public final String code;
	/**
	 * Number of cards in this Expansion.
	 */
	public final int count;
	
	/**
	 * Create a new Expansion.
	 * 
	 * @param name Name of the new expansion
	 * @param block Name of the block the new Expansion belongs to
	 * @param code Code of the new expansion (usually three letters)
	 * @param count Number of cards in the new Expansion
	 */
	public Expansion(String name, String block, String code, int count)
	{
		this.name = name;
		this.block = block;
		this.code = code;
		this.count = count;
	}
	
	/**
	 * @return A String representation of this Expansion. 
	 */
	@Override
	public String toString()
	{
		return name + (block != "" ? ": " + block : "") + " (" + code + ": " + count + ")";
	}
	
	/**
	 * @param other Object to test.
	 * @return <code>true</code> if this Expansion has the same name as the other one, and <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (!(other instanceof Expansion))
			return false;
		if (other == this)
			return true;
		
		Expansion o = (Expansion)other;
		return name.equals(o.name);
	}
	
	/**
	 * @return An integer uniquely identifying this Expansion.
	 */
	@Override
	public int hashCode()
	{
		return name.hashCode();
	}
}
