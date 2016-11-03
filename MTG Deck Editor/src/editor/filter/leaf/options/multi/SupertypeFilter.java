package editor.filter.leaf.options.multi;

import java.util.Arrays;
import java.util.HashSet;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.util.Containment;

/**
 * This class represents a filter that groups cards by supertype.
 * 
 * @author Alec Roelke
 */
public class SupertypeFilter extends MultiOptionsFilter<String>
{
	/**
	 * List of all supertypes that appear on cards.
	 */
	public static String[] supertypeList = {};

	/**
	 * Create a new SupertypeFilter.
	 */
	public SupertypeFilter()
	{
		super(FilterFactory.SUPERTYPE, Card::supertypes);
	}
	
	/**
	 * Parse a String to determine this SupertypeFilter's containment
	 * and selected supertypes.
	 * 
	 * @param s String to parse
	 * @see editor.filter.Filter#parse(String)
	 */
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterFactory.SUPERTYPE);
		int delim = content.indexOf('{');
		contain = Containment.fromString(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			selected.addAll(Arrays.asList(content.substring(delim + 1, content.length() - 1).split(",")));
	}
	
	/**
	 * @return A new SupertypeFilter that is a copy of this one.
	 */
	@Override
	public Filter copy()
	{
		SupertypeFilter filter = (SupertypeFilter)FilterFactory.createFilter(FilterFactory.SUPERTYPE);
		filter.contain = contain;
		filter.selected = new HashSet<String>(selected);
		return filter;
	}

	@Override
	public String convertFromString(String str)
	{
		return str;
	}
}
