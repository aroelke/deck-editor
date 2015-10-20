package editor.gui.filter.editor.number;

import java.awt.Dimension;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.BoxLayout;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import editor.database.Card;
import editor.gui.filter.ComboBoxPanel;
import editor.gui.filter.FilterType;
import editor.gui.filter.editor.FilterEditorPanel;
import editor.util.Comparison;

/**
 * This class represents a FilterPanel that filters numerical characteristics of
 * Cards.  Ranges can be accomplished using two of them.  If numbers have non-
 * numerical characters in them (like "1a" or "7-*"), the numbers are extracted
 * from those Strings. 
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class NumberFilterPanel extends FilterEditorPanel
{	
	/**
	 * Characteristic of the Card to be filtered.
	 */
	private Function<Card, Collection<Double>> operand;
	/**
	 * Numerical comparison to make with the operand.
	 */
	protected ComboBoxPanel<Comparison> comparisonBox;
	/**
	 * Value to compare to the operand.
	 */
	protected JSpinner spinner;
	
	/**
	 * Create a new NumberFilterPanel.
	 * 
	 * @param type FilterType the new NumberFilterPanel edits
	 * @param op Function representing the characteristic to edit
	 * @param c 
	 */
	public NumberFilterPanel(FilterType type, Function<Card, Collection<Double>> op)
	{
		super(type);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		// Combo box for choosing the type of comparison to make
		add(comparisonBox = new ComboBoxPanel<Comparison>(Comparison.values()));
		
		// Value to compare the characteristic against
		spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(0.0, 0.0, null, 1.0));
		spinner.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));
		add(spinner);
		
		operand = op;
	}
	
	/**
	 * @param includeEmpty Include empty (or missing) values of the characteristic
	 * @return A <code>Predicate<Card></code> that represents the filter of this
	 * NumberFilterPanel's characteristic.  If empty values should be included,
	 * then they are treated as 0.
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		return (c) -> operand.apply(c).stream().anyMatch((v) -> comparisonBox.getSelectedItem().test(v, (double)spinner.getValue()));
	}

	/**
	 * @return <code>false</code> because this FilterPanel's value is controlled by
	 * a spinner, which always has a value in it.
	 */
	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	/**
	 * @return A String representation of this NumberFilterPanel's contents, which
	 * is its comparison type followed by its spinner's value.
	 */
	@Override
	protected String repr()
	{
		return comparisonBox.getSelectedItem().toString() + spinner.getValue();
	}

	/**
	 * Set the value of the comparison combo box and the spinner
	 * based on the contents of the input String.
	 * 
	 * @param content String to parse for content.
	 */
	@Override
	public void setContents(String content)
	{
		comparisonBox.setSelectedItem(Comparison.get(content.charAt(0)));
		spinner.setValue(Double.valueOf(content.substring(1)));
	}
}
