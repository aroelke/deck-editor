package editor.gui.filter.editor;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import editor.filter.Filter;
import editor.filter.FilterType;
import editor.filter.leaf.FilterLeaf;

/**
 * This class represents a panel that corresponds to a filter that
 * returns <code>true</code> for all cards.  There are no fields
 * to edit.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class AllFilterPanel extends FilterEditorPanel<FilterLeaf<?>>
{
	/**
	 * Create a new AllFilterPanel.
	 */
	public AllFilterPanel()
	{
		super();
		setLayout(new GridLayout(1, 1));
		setBorder(new EmptyBorder(0, 5, 0, 0));
		JLabel label = new JLabel("This clause will match every card.");
		add(label);
	}
	
	/**
	 * @return {@link editor.filter.leaf.FilterLeaf#ALL_CARDS}
	 */
	@Override
	public Filter filter()
	{
		return FilterType.ALL.createFilter();
	}

	/**
	 * There are no contents to set, so do nothing.
	 * 
	 * @param filter Filter to use
	 */
	@Override
	public void setContents(FilterLeaf<?> filter)
	{}
}
