package editor.gui.filter.editor;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import editor.filter.Filter;
import editor.filter.leaf.FilterLeaf;

@SuppressWarnings("serial")
public class AllFilterPanel extends FilterEditorPanel<FilterLeaf<?>>
{
	public static AllFilterPanel create()
	{
		return new AllFilterPanel();
	}
	
	private AllFilterPanel()
	{
		super();
		setLayout(new GridLayout(1, 1));
		setBorder(new EmptyBorder(0, 5, 0, 0));
		JLabel label = new JLabel("This clause will match every card.");
		add(label);
	}
	
	@Override
	public Filter filter()
	{
		return FilterLeaf.ALL_CARDS;
	}

	@Override
	public void setContents(FilterLeaf<?> filter)
	{}
}
