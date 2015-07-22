package util;

import java.awt.Color;
import java.awt.Component;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import database.characteristics.PowerToughness;

/**
 * This class represents a table whose alternating occupied rows will be different
 * colors.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class StripedTable extends JTable
{
	/**
	 * Color of the alternate rows.
	 */
	private Color stripeColor;
	
	/**
	 * Create a new StripedTable with the default color.
	 */
	public StripedTable()
	{
		super();
		stripeColor = Color.LIGHT_GRAY;
		setRowSorter(new EmptyTableRowSorter(getModel()));
	}
	
	/**
	 * Create a new StripedTable with the default color and given model.
	 * 
	 * @param model Model for the new table.
	 */
	public StripedTable(TableModel model)
	{
		super(model);
		stripeColor = Color.LIGHT_GRAY;
		setRowSorter(new EmptyTableRowSorter(model));
	}
	
	/**
	 * TODO: Comment this
	 * @param model
	 */
	@Override
	public void setModel(TableModel model)
	{
		super.setModel(model);
		setRowSorter(new EmptyTableRowSorter(model));
	}
	
	/**
	 * Set the color for the stripes of this StripedTable.
	 * 
	 * @param col New stripe color
	 */
	public void setStripeColor(Color col)
	{
		stripeColor = col;
		repaint();
	}
	
	/**
	 * The table will track the viewport width if the viewport is larger than its preferred size.
	 */
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return getPreferredSize().width < getParent().getWidth();
	}
	
	/**
	 * Prepares the renderer.  Changes its background according to the striping color.
	 * @see JTable#prepareRenderer(TableCellRenderer, int, int)
	 */
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component c = super.prepareRenderer(renderer, row, column);
		if (!isRowSelected(row))
			c.setBackground(row%2 == 0 ? new Color(getBackground().getRGB()) : stripeColor);
		return c;
	}
	
	private class EmptyTableRowSorter extends TableRowSorter<TableModel>
	{
		private TableModel model;
		
		public EmptyTableRowSorter(TableModel m)
		{
			super(m);
			model = m;
		}
		
		@Override
		public Comparator<?> getComparator(int column)
		{
			if (model.getColumnClass(column).equals(PowerToughness.class))
			{
				boolean ascending = getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING;
				return (a, b) -> {
					PowerToughness pt1 = (PowerToughness)a;
					PowerToughness pt2 = (PowerToughness)b;
					if (Double.isNaN(pt1.value) && Double.isNaN(pt2.value))
						return 0;
					if (Double.isNaN(pt1.value))
						return ascending ? 1 : -1;
					else if (Double.isNaN(pt2.value))
						return ascending ? -1 : 1;
					else
						return pt1.compareTo(pt2);
				};
			}
			else
				return super.getComparator(column);
		}
	}
}
