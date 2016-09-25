package editor.database.card;

import java.util.Arrays;
import java.util.List;

import editor.database.characteristics.ManaCost;

/**
 * This class represents a Card with two faces:  One on the front, and one
 * on the back.
 * 
 * @author Alec Roelke
 */
public class DoubleFacedCard extends MultiCard
{
	/**
	 * Card representing the front face.
	 */
	private final Card front;
	/**
	 * Tuple of this DoubleFacedCard's faces' mana costs.
	 */
	private ManaCost.Tuple manaCost;
	/**
	 * Converted mana costs of this DoubleFacedCard's faces.
	 */
	private List<Double> cmc;
	
	/**
	 * Create a new DoubleFacedCard with the given Cards as faces.  Their layouts should
	 * say that they are double-faced card.
	 * 
	 * @param f Card representing the front face
	 * @param b Card representing the back face
	 */
	public DoubleFacedCard(Card f, Card b)
	{
		super(CardLayout.DOUBLE_FACED, f, b);
		front = f;
		if (front.layout() != CardLayout.DOUBLE_FACED|| b.layout() != CardLayout.DOUBLE_FACED)
			throw new IllegalArgumentException("can't join non-double-faced cards into double-faced cards");
		
		manaCost = null;
		cmc = null;
	}
	
	/**
	 * @return A list containing the mana cost of this DoubleFacedCard.  Only the front
	 * face has a mana cost.
	 */
	@Override
	public ManaCost.Tuple manaCost()
	{
		if (manaCost == null)
			manaCost = new ManaCost.Tuple(front.manaCost()[0], new ManaCost());
		return manaCost;
	}
	
	/**
	 * @return A list containing the converted mana cost of this DoubleFacedCard.  While only
	 * the front face has a mana cost, the back face has the same converted mana cost as the
	 * front.
	 */
	@Override
	public List<Double> cmc()
	{
		if (cmc == null)
			cmc = Arrays.asList(front.cmc()[0], front.cmc()[0]);
		return cmc;
	}
	
	/**
	 * @return The converted mana cost of this DoubleFacedCard's front face.
	 */
	@Override
	public double minCmc()
	{
		return front.minCmc();
	}
}
