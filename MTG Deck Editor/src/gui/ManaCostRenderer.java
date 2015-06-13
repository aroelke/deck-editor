package gui;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import database.ManaCost;
import database.symbol.Symbol;

/**
 * This class represents a renderer of ManaCosts in a table.
 * 
 * @author Alec
 */
@SuppressWarnings("serial")
public class ManaCostRenderer extends DefaultTableCellRenderer
{
	/**
	 * Create the Component that will display the contents of the specified cell.  If that cell contains
	 * a ManaCost, then rather than displaying text, a panel containing a series of labels whose icons
	 * are mana symbols will be displayed instead. 
	 * 
	 * @return The Component that should be used to render the cell.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (value instanceof ManaCost)
		{
			ManaCost cost = (ManaCost)value;
			JPanel costPanel = new JPanel();
			costPanel.setLayout(new BoxLayout(costPanel, BoxLayout.X_AXIS));
			costPanel.setBorder(new EmptyBorder(0, 1, -1, 0));
			for (Symbol sym: cost.symbols())
				costPanel.add(new JLabel(sym.getIcon(13)));
			if (isSelected)
			{
				costPanel.setBackground(table.getSelectionBackground());
				costPanel.setForeground(table.getSelectionForeground());
			}
			else
			{
				costPanel.setBackground(table.getBackground());
				costPanel.setForeground(table.getForeground());
			}
			return costPanel;
		}
		else
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
}
