package gui.filter.editor.options;

import gui.filter.FilterType;
import gui.filter.editor.FilterEditorPanel;

/**
 * This class represents a FilterPanel that presents a set of options
 * to the user to choose from to fill out the filter.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public abstract class OptionsFilterPanel extends FilterEditorPanel
{
	/**
	 * TODO: Comment this
	 * @param type
	 */
	public OptionsFilterPanel(FilterType type)
	{
		super(type);
	}
}
