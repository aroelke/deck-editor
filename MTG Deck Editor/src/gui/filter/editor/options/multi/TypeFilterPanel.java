package gui.filter.editor.options.multi;

import gui.filter.FilterType;
import database.Card;

/**
 * This class represents a panel that can filter cards by their type.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class TypeFilterPanel extends MultiOptionsFilterPanel<String>
{
	/**
	 * Create a new TypeFilterPanel.
	 */
	public TypeFilterPanel()
	{
		super(FilterType.TYPE, Card.typeList, Card::types);
	}
}
