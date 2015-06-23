package gui.editor;

import gui.ManaCostRenderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.function.Predicate;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;

import database.Card;
import database.Deck;
import database.ManaCost;
import database.characteristics.CardCharacteristic;

/**
 * This class represents a panel that shows information about a category in a deck.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CategoryPanel extends JPanel
{
	/**
	 * Number of rows in the card table to display.
	 */
	private static final int ROWS_TO_DISPLAY = 6;
	
	/**
	 * Category in the Deck data structure.
	 */
	private Deck.Category category;
	/**
	 * Table to display the contents of the category.
	 */
	private JTable table;
	/**
	 * Model to tell the table how to display the contents of the category.
	 */
	private DeckTableModel model;
	/**
	 * Label showing the number of cards in the category.
	 */
	private JLabel countLabel;
	/**
	 * Button for editing the category.
	 */
	private JButton editButton;
	/**
	 * Button to remove the category.
	 */
	private JButton removeButton;
	/**
	 * Border showing the name of the category.
	 */
	private TitledBorder border;
	
	/**
	 * Create a new CategoryPanel.
	 * 
	 * @param n Name of the new category
	 * @param r String representation of the new category
	 * @param list List of cards in the new category
	 * @param p Filter for the new category
	 */
	public CategoryPanel(String n, String r, Deck list, Predicate<Card> p)
	{
		super();
		
		if (list.containsCategory(n))
			throw new IllegalArgumentException("Categories must have unique names");
		category = list.addCategory(n, r, p);
		
		// Each category is surrounded by a border with a title
		border = new TitledBorder(category.name());
		setBorder(border);
		
		setLayout(new BorderLayout());
		
		// Label showing the number of cards in the category
		JPanel countPanel = new JPanel();
		countPanel.setLayout(new BorderLayout(0, 0));
		countLabel = new JLabel("Cards: " + category.size());
		countLabel.setVerticalAlignment(SwingConstants.TOP);
		countPanel.add(countLabel, BorderLayout.WEST);
		
		// Panel containing edit and remove buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		editButton = new JButton("Edit");
		buttonPanel.add(editButton);
		removeButton = new JButton("-");
		buttonPanel.add(removeButton);
		countPanel.add(buttonPanel, BorderLayout.EAST);
		
		add(countPanel, BorderLayout.NORTH);
		
		// Table showing the cards in the category
		model = new DeckTableModel(category, Arrays.asList(CardCharacteristic.NAME, CardCharacteristic.COUNT,
	   													CardCharacteristic.MANA_COST, CardCharacteristic.TYPE_LINE,
	   													CardCharacteristic.EXPANSION_NAME, CardCharacteristic.RARITY));
		table = new JTable(model)
		{
			@Override
			public boolean getScrollableTracksViewportWidth()
			{
				return getPreferredSize().width < getParent().getWidth();
			}
			
			@Override
			public Dimension getPreferredScrollableViewportSize()
			{
				Dimension d = getPreferredSize();
				d.height = getRowHeight()*ROWS_TO_DISPLAY;
				return d;
			}
		};
		table.setAutoCreateRowSorter(true);
		table.setDefaultRenderer(ManaCost.class, new ManaCostRenderer());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setShowGrid(false);
		table.setFillsViewportHeight(true);
		JScrollPane tablePane = new JScrollPane(table);
		tablePane.addMouseWheelListener(new PDMouseWheelListener(tablePane));
		add(tablePane, BorderLayout.CENTER);
	}
	
	/**
	 * Update the GUI to reflect changes in a category.
	 */
	public void update()
	{
		model.fireTableDataChanged();
		countLabel.setText("Cards: " + category.total());
		border.setTitle(category.name());
		table.revalidate();
		table.repaint();
		revalidate();
		repaint();
	}
	
	/**
	 * @return The name of the category.
	 */
	public String name()
	{
		return category.name();
	}
	
	/**
	 * @return The String representation of the category.
	 * @see CategoryDialog#initializeFromString(String)
	 */
	@Override
	public String toString()
	{
		return category.toString();
	}
	
	/**
	 * @return The Card filter for the category.
	 */
	public Predicate<Card> filter()
	{
		return category.filter();
	}
	
	/**
	 * Change the parameters of the category.
	 * 
	 * @param newName New name for the category
	 * @param newRepr New String representation of the category
	 * @param newFilter New filter for the category
	 */
	public void edit(String newName, String newRepr, Predicate<Card> newFilter)
	{
		if (!category.edit(newName, newRepr, newFilter))
			throw new IllegalArgumentException("Category \"" + newName + "\" already exists");
		update();
	}
	
	/**
	 * @return An array containing the rows that are selected in this CategorsPanel's table.
	 */
	public int[] getSelectedRows()
	{
		return table.getSelectedRows();
	}
	
	/**
	 * Select the given row interval in this CategoryPanel's table in addition to the
	 * selection that may have already been made.
	 * 
	 * @param index0 Starting index of the interval
	 * @param index1 Ending index of the interval
	 */
	public void addRowSelectionInterval(int index0, int index1)
	{
		table.addRowSelectionInterval(index0, index1);
	}
	
	/**
	 * Add an action that should be performed when one or more rows in the table are
	 * selected.
	 * 
	 * @param x Listener for a selection event
	 */
	public void addListSelectionListener(ListSelectionListener x)
	{
		table.getSelectionModel().addListSelectionListener(x);
	}
	
	/**
	 * Add an action to perform when the edit button is clicked.
	 * 
	 * @param x Listener for a click event on the edit button
	 */
	public void addEditButtonListener(ActionListener x)
	{
		editButton.addActionListener(x);
	}
	
	/**
	 * Add an action to perform when the remove button is clicked.
	 * 
	 * @param x Listener for a click event on the remove button
	 */
	public void addRemoveButtonListener(ActionListener x)
	{
		removeButton.addActionListener(x);
	}
	
	/**
	 * Clear the selection in this CategoryPanel's table.  This will fire
	 * the table's ListSelectionListener.
	 */
	public void clearSelection()
	{
		table.clearSelection();
	}
	
	/**
	 * Convert the row index in the view of this CategoryPanel's table to a
	 * row in its backing data structure.
	 * 
	 * @param viewRowIndex Row index to convert
	 * @return The converted row index.
	 */
	public int convertRowIndexToModel(int viewRowIndex)
	{
		return table.convertRowIndexToModel(viewRowIndex);
	}
	
	/**
	 * Converts the row index in the model of thie CategoryPanel's table to
	 * a row in the view.
	 * 
	 * @param modelRowIndex Row index to convert
	 * @return The converted row index.
	 */
	public int convertRowIndexToView(int modelRowIndex)
	{
		return table.convertRowIndexToView(modelRowIndex);
	}
	
	/**
	 * Add a MouseListener to this category's table.
	 * 
	 * @param m MouseListener to add to the table
	 */
	public void addTableMouseListener(MouseListener m)
	{
		table.addMouseListener(m);
	}
	
	/**
	 * This class represents a mouse wheel listener that returns mouse wheel control to an outer scroll
	 * pane when this one's scroll pane has reached a limit.
	 * 
	 * It is adapted from a StackOverflow answer to the same problem, which can be found at
	 * @link{http://stackoverflow.com/questions/1377887/jtextpane-prevents-scrolling-in-the-parent-jscrollpane}.
	 * 
	 * @author Nemi
	 * @since November 24, 2009
	 */
	private class PDMouseWheelListener implements MouseWheelListener
	{
		private JScrollBar bar;
		private int previousValue = 0;
		private JScrollPane parentScrollPane;
		private JScrollPane parent;

		private JScrollPane getParentScrollPane()
		{
			if (parentScrollPane == null)
			{
				Component parent = getParent();
				while (!(parent instanceof JScrollPane) && parent != null)
					parent = parent.getParent();
				parentScrollPane = (JScrollPane)parent;
			}
			return parentScrollPane;
		}

		public PDMouseWheelListener(JScrollPane p)
		{
			parent = p;
			bar = parent.getVerticalScrollBar();
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			JScrollPane parent = getParentScrollPane();
			if (parent != null)
			{
				/*
				 * Only dispatch if we have reached top/bottom on previous scroll
				 */
				 if (e.getWheelRotation() < 0)
				 {
					 if (bar.getValue() == 0 && previousValue == 0)
						 parent.dispatchEvent(cloneEvent(e));
				 }
				 else
				 {
					 if (bar.getValue() == getMax() && previousValue == getMax())
						 parent.dispatchEvent(cloneEvent(e));
				 }
				 previousValue = bar.getValue();
			}
			/* 
			 * If parent scrollpane doesn't exist, remove this as a listener.
			 * We have to defer this till now (vs doing it in constructor) 
			 * because in the constructor this item has no parent yet.
			 */
			else
				this.parent.removeMouseWheelListener(this);
		}
		
		private int getMax()
		{
			return bar.getMaximum() - bar.getVisibleAmount();
		}
		
		private MouseWheelEvent cloneEvent(MouseWheelEvent e)
		{
			return new MouseWheelEvent(getParentScrollPane(), e.getID(), e
					.getWhen(), e.getModifiers(), 1, 1, e
					.getClickCount(), false, e.getScrollType(), e
					.getScrollAmount(), e.getWheelRotation());
		}
	}
}
