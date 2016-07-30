package editor.database.symbol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import editor.database.characteristics.ManaType;

/**
 * This class represents a two-color hybrid symbol.  It will sort its colors so that they appear in the correct order.
 * 
 * @author Alec Roelke
 * @see database.characteristics.Color.UnmodifiableList
 */
public class HybridSymbol extends Symbol
{
	/**
	 * Map mapping each Tuple of colors to their corresponding hybrid symbols.
	 * @see editor.database.symbol.Symbol
	 */
	private static final Map<ManaType.Tuple, HybridSymbol> SYMBOLS = new HashMap<ManaType.Tuple, HybridSymbol>();
	static
	{
		SYMBOLS.put(new ManaType.Tuple(ManaType.WHITE, ManaType.BLUE), new HybridSymbol(new ManaType.Tuple(ManaType.WHITE, ManaType.BLUE)));
		SYMBOLS.put(new ManaType.Tuple(ManaType.WHITE, ManaType.BLACK), new HybridSymbol(new ManaType.Tuple(ManaType.WHITE, ManaType.BLACK)));
		SYMBOLS.put(new ManaType.Tuple(ManaType.BLUE, ManaType.BLACK), new HybridSymbol(new ManaType.Tuple(ManaType.BLUE, ManaType.BLACK)));
		SYMBOLS.put(new ManaType.Tuple(ManaType.BLUE, ManaType.RED), new HybridSymbol(new ManaType.Tuple(ManaType.BLUE, ManaType.RED)));
		SYMBOLS.put(new ManaType.Tuple(ManaType.BLACK, ManaType.RED), new HybridSymbol(new ManaType.Tuple(ManaType.BLACK, ManaType.RED)));
		SYMBOLS.put(new ManaType.Tuple(ManaType.BLACK, ManaType.GREEN), new HybridSymbol(new ManaType.Tuple(ManaType.BLACK, ManaType.GREEN)));
		SYMBOLS.put(new ManaType.Tuple(ManaType.RED, ManaType.GREEN), new HybridSymbol(new ManaType.Tuple(ManaType.RED, ManaType.GREEN)));
		SYMBOLS.put(new ManaType.Tuple(ManaType.RED, ManaType.WHITE), new HybridSymbol(new ManaType.Tuple(ManaType.RED, ManaType.WHITE)));
		SYMBOLS.put(new ManaType.Tuple(ManaType.GREEN, ManaType.WHITE), new HybridSymbol(new ManaType.Tuple(ManaType.GREEN, ManaType.WHITE)));
		SYMBOLS.put(new ManaType.Tuple(ManaType.GREEN, ManaType.BLUE), new HybridSymbol(new ManaType.Tuple(ManaType.GREEN, ManaType.BLUE)));
	}
	
	/**
	 * Get the HybridSymbol corresponding to the given ManaTypes.
	 * 
	 * @param colors ManaTypes to look up
	 * @return The HybridSymbol corresponding to the given ManaTypes, or
	 * null if no such symbol exists.
	 */
	public static HybridSymbol get(ManaType color1, ManaType color2)
	{
		return SYMBOLS.get(new ManaType.Tuple(color1, color2));
	}
	
	/**
	 * Get the HybridSymbol corresponding to the String, which is two color characters
	 * separated by a "/".
	 * 
	 * @param pair The String to look up
	 * @return The HybridSymbol corresponding to the given String, or null if no
	 * such symbol exists.
	 */
	public static HybridSymbol get(String pair)
	{
		try
		{
			List<ManaType> colors = Arrays.stream(pair.split("/")).map(ManaType::get).collect(Collectors.toList());
			if (colors.size() != 2)
				return null;
			else
				return SYMBOLS.get(new ManaType.Tuple(colors));
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}
	
	/**
	 * This HybridSymbol's Tuple of colors.
	 */
	private final ManaType.Tuple colors;
	
	private HybridSymbol(ManaType.Tuple colors)
	{
		super(colors.get(0).toString().toLowerCase() + "_" + colors.get(1).toString().toLowerCase() + "_mana.png");
		this.colors = colors;
	}
	
	/**
	 * @return This HybridSymbol's text, which is the shorthand for its two colors separated by a /.
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return colors.get(0).shorthand() + "/" + colors.get(1).shorthand();
	}

	/**
	 * @return This HybridSymbol's value for converted cost, which is 1.
	 * @see editor.database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 1;
	}

	/**
	 * @return This HybridSymbol's color weight, which is 0.5 for each of its two colors and
	 * 0 for the other three.
	 * @see editor.database.symbol.Symbol#colorWeights()
	 */
	@Override
	public Map<ManaType, Double> colorWeights()
	{
		return createWeights(new ColorWeight(colors.get(0), 0.5),
							 new ColorWeight(colors.get(1), 0.5));
	}

	/**
	 * @param o Symbol to compare with
	 * @return A negative number if the other symbol should come after this HybridSymbol,
	 * the two symbols' Tuples' difference if they are both HybridSymbols, 0 if color order
	 * doesn't matter, and a postive number if the other symbol should come before.
	 * @see editor.database.symbol.Symbol#compareTo(Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		if (o instanceof HybridSymbol)
			return colors.compareTo(((HybridSymbol)o).colors);
		else
			return super.compareTo(o);
	}
}
