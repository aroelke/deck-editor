package gui.filter.editor.number;

import gui.filter.FilterType;

import java.util.function.Predicate;

import database.Card;

/**
 * This class represents a FilterPanel that filters cards by loyalty.  If a
 * card doesn't have a loyalty value, simply including this filter will filter
 * them out.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class LoyaltyFilterPanel extends NumberFilterPanel
{
	/**
	 * Create a new LoyaltyFilterPanel.
	 */
	public LoyaltyFilterPanel()
	{
		super((c) -> (double)c.loyalty().value, false, FilterType.LOYALTY.code);
	}
	
	/**
	 * @return A <code>Predicate<Card></code> representing this panel's filter, which is the same
	 * as NumberFilterPanel's filter except it also filters out cards without loyalties.
	 * @see NumberFilterPanel#getFilter()
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		Predicate<Card> hasLoyalty = (c) -> c.loyalty().value > 0;
		return hasLoyalty.and(super.getFilter());
	}
}
