package editor.database.symbol;


/**
 * This class represents the Chaos symbol that appears on Plane cards.
 * 
 * @author Alec Roelke
 */
public class ChaosSymbol extends Symbol
{
	/**
	 * Instance of the Chaos symbol.
	 * @see editor.database.symbol.Symbol
	 */
	public static final ChaosSymbol CHAOS = new ChaosSymbol();
	
	/**
	 * Create a new Chaos symbol.
	 */
	private ChaosSymbol()
	{
		super("chaos.png");
	}

	/**
	 * @return This ChaosSymbol's text: a "C."
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "CHAOS";
	}
	
	/**
	 * @return A String representation of this ChaosSymbol: The string "CHAOS".
	 */
	@Override
	public String toString()
	{
		return "CHAOS";
	}

	/**
	 * @param o Symbol to compare with
	 * @return 0, since ordering with other symbols is not defined for chaos symbol.
	 * @see editor.database.symbol.Symbol#compareTo(editor.database.symbol.Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		return 0;
	}

	/**
	 * @param other Symbol to compare to
	 * @return 0, since this symbol's ordering relative to other ones is
	 * not defined.
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof ChaosSymbol;
	}
}
