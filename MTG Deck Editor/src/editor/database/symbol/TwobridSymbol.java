package editor.database.symbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.database.characteristics.ManaType;

/**
 * This class represents a colorless-colored hybrid mana symbol, which can be paid for either by one
 * mana of the corresponding color, or two mana of any color.  These are referred to as "twobrid"
 * symbols.
 * 
 * @author Alec Roelke
 */
public class TwobridSymbol extends Symbol
{
	/**
	 * Map of colors onto their corresponding twobrid symbols.
	 * @see editor.database.symbol.Symbol
	 */
	private static final Map<ManaType, TwobridSymbol> SYMBOLS = Collections.unmodifiableMap(
			Arrays.stream(ManaType.colors()).collect(Collectors.toMap(Function.identity(), TwobridSymbol::new)));
	
	/**
	 * Get the TwobridSymbol corresponding to the given color.
	 * 
	 * @param col Color to look up
	 * @return The TwobridSymbol corresponding to the given ManaType, or
	 * null if no such symbol exists.
	 */
	public static TwobridSymbol get(ManaType col)
	{
		return SYMBOLS.get(col);
	}
	
	/**
	 * Get the TwobridSymbol corresponding to the given String.
	 * 
	 * @param col Color to look up
	 * @return The TwobridSymbol corresponding to the given String, or
	 * null if no such symbol exists.
	 */
	public static TwobridSymbol get(String col)
	{
		try
		{
			int index = col.indexOf('/');
			if (index > 0 && col.charAt(index - 1) == '2')
				return get(ManaType.get(col.charAt(index + 1)));
			else
				return null;
		}
		catch (IllegalArgumentException | StringIndexOutOfBoundsException e)
		{
			return null;
		}
	}
	
	/**
	 * This TwobridSymbol's color.
	 */
	private final ManaType color;
	
	/**
	 * Create a TwobridSymbol
	 * 
	 * @param color The new TwobridSymbol's color.
	 */
	private TwobridSymbol(ManaType color)
	{
		super("2_" + color.toString().toLowerCase() + "_mana.png");
		this.color = color;
	}
	
	/**
	 * @return The ManaType of this TwobridSymbol.
	 */
	public ManaType color()
	{
		return color;
	}
	
	/**
	 * @return This TwobridSymbol's text, which is a "2/" followed by its color shorthand.
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "2/" + String.valueOf(color.shorthand());
	}

	/**
	 * @return This TwobridSymbol's value in converted mana costs, which is 2.
	 * @see editor.database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 2;
	}

	/**
	 * @return This TwobridSymbol's color weight, which is 0.5 for its color and 0
	 * for the others.
	 * @see editor.database.symbol.Symbol#colorWeights()
	 */
	@Override
	public Map<ManaType, Double> colorWeights()
	{
		return createWeights(new ColorWeight(color, 0.5));
	}

	/**
	 * @param o Symbol to compare with
	 * @return A negative number if this TwobridSymbol should come before the other
	 * Symbol in costs, the color ordering of the two symbols if they are both
	 * TwobridSymbols, a postive number if it should come after, and 0 if ordering
	 * is not defined.
	 * @see editor.database.symbol.Symbol#compareTo(Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		if (o instanceof TwobridSymbol)
			return color.colorOrder(((TwobridSymbol)o).color);
		else
			return super.compareTo(o);
	}
}
