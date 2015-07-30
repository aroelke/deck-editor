package gui.filter.editor.number;

import gui.filter.FilterType;

import java.util.Arrays;

/**
 * This class represents a FilterPanel that filters cards by collector's
 * number.  If the number is missing (has a "--" value), then it is treated
 * as 0.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CardNumberFilterPanel extends NumberFilterPanel
{
	/**
	 * Create a new CardNumberFilterPanel.
	 */
	public CardNumberFilterPanel()
	{
		super((c) -> Arrays.stream(c.numbers()).mapToDouble((v) -> Double.valueOf(v.replace("--", "0"))).toArray(), FilterType.CARD_NUMBER.code);
	}
}
