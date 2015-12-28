package editor.gui.filter.editor;

import java.util.function.Function;

import editor.database.Card;
import editor.database.characteristics.Expansion;
import editor.database.characteristics.Rarity;
import editor.filter.FilterType;
import editor.filter.leaf.ColorFilter;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.ManaCostFilter;
import editor.filter.leaf.NumberFilter;
import editor.filter.leaf.TextFilter;
import editor.filter.leaf.TypeLineFilter;
import editor.filter.leaf.VariableNumberFilter;
import editor.filter.leaf.options.multi.CardTypeFilter;
import editor.filter.leaf.options.multi.LegalityFilter;
import editor.filter.leaf.options.multi.SubtypeFilter;
import editor.filter.leaf.options.multi.SupertypeFilter;
import editor.filter.leaf.options.single.BlockFilter;
import editor.filter.leaf.options.single.ExpansionFilter;
import editor.filter.leaf.options.single.RarityFilter;

/**
 * This enum enumerates the types of filter editor panels and
 * can instantiate them.
 * 
 * @author Alec Roelke
 */
public enum FilterPanelType
{
	NAME(FilterType.NAME, (f) -> {
		if (f.type == FilterType.NAME)
			return TextFilterPanel.create((TextFilter)f);
		else
			throw new IllegalArgumentException("Illegal name filter type " + f.type.name());
	}),
	MANA_COST(FilterType.MANA_COST, (f) -> {
		if (f.type == FilterType.MANA_COST)
			return ManaCostFilterPanel.create((ManaCostFilter)f);
		else
			throw new IllegalArgumentException("Illegal mana cost filter type " + f.type.name());
	}),
	CMC(FilterType.CMC, (f) -> {
		if (f.type == FilterType.CMC)
			return NumberFilterPanel.create((NumberFilter)f);
		else
			throw new IllegalArgumentException("Illegal cmc filter type " + f.type.name());
	}),
	COLOR(FilterType.COLOR, (f) -> {
		if (f.type == FilterType.COLOR)
			return ColorFilterPanel.create((ColorFilter)f);
		else
			throw new IllegalArgumentException("Illegal color filter type " + f.type.name());
	}),
	COLOR_IDENTITY(FilterType.COLOR_IDENTITY, (f) -> {
		if (f.type == FilterType.COLOR_IDENTITY)
			return ColorFilterPanel.create((ColorFilter)f);
		else
			throw new IllegalArgumentException("Illegal color identity filter type " + f.type.name());
	}),
	TYPE_LINE(FilterType.TYPE_LINE, (f) -> {
		if (f.type == FilterType.TYPE_LINE)
			return TypeLineFilterPanel.create((TypeLineFilter)f);
		else
			throw new IllegalArgumentException("Illegal type line filter type " + f.type.name());
	}),
	SUPERTYPE(FilterType.SUPERTYPE, (f) -> {
		if (f.type == FilterType.SUPERTYPE)
			return OptionsFilterPanel.create((SupertypeFilter)f, Card.supertypeList);
		else
			throw new IllegalArgumentException("Illegal supertype filter type " + f.type.name());
	}),
	TYPE(FilterType.TYPE, (f) -> {
		if (f.type == FilterType.TYPE)
			return OptionsFilterPanel.create((CardTypeFilter)f, Card.typeList);
		else
			throw new IllegalArgumentException("Illegal card type filter type " + f.type.name());
	}),
	SUBTYPE(FilterType.SUBTYPE, (f) -> {
		if (f.type == FilterType.SUBTYPE)
			return OptionsFilterPanel.create((SubtypeFilter)f, Card.subtypeList);
		else
			throw new IllegalArgumentException("Illegal subtype filter type " + f.type.name());
	}),
	EXPANSION(FilterType.EXPANSION, (f) -> {
		if (f.type == FilterType.EXPANSION)
			return OptionsFilterPanel.create((ExpansionFilter)f, Expansion.expansions);
		else
			throw new IllegalArgumentException("Illegal expansion filter type " + f.type.name());
	}),
	BLOCK(FilterType.BLOCK, (f) -> {
		if (f.type == FilterType.BLOCK)
			return OptionsFilterPanel.create((BlockFilter)f, Expansion.blocks);
		else
			throw new IllegalArgumentException("Illegal block filter type " + f.type.name());
	}),
	RARITY(FilterType.RARITY, (f) -> {
		if (f.type == FilterType.RARITY)
			return OptionsFilterPanel.create((RarityFilter)f, Rarity.values());
		else
			throw new IllegalArgumentException("Illegal rarity filter type " + f.type.name());
	}),
	RULES_TEXT(FilterType.RULES_TEXT, (f) -> {
		if (f.type == FilterType.RULES_TEXT)
			return TextFilterPanel.create((TextFilter)f);
		else
			throw new IllegalArgumentException("Illegal rules text filter type " + f.type.name());
	}),
	FLAVOR_TEXT(FilterType.FLAVOR_TEXT, (f) -> {
		if (f.type == FilterType.FLAVOR_TEXT)
			return TextFilterPanel.create((TextFilter)f);
		else
			throw new IllegalArgumentException("Illegal flavor text filter type " + f.type.name());
	}),
	POWER(FilterType.POWER, (f) -> {
		if (f.type == FilterType.POWER)
			return VariableNumberFilterPanel.create((VariableNumberFilter)f);
		else
			throw new IllegalArgumentException("Illegal power filter type " + f.type.name());
	}),
	TOUGHNESS(FilterType.TOUGHNESS, (f) -> {
		if (f.type == FilterType.TOUGHNESS)
			return VariableNumberFilterPanel.create((VariableNumberFilter)f);
		else
			throw new IllegalArgumentException("Illegal toughness filter type " + f.type.name());
	}),
	LOYALTY(FilterType.LOYALTY, (f) -> {
		if (f.type == FilterType.LOYALTY)
			return NumberFilterPanel.create((NumberFilter)f);
		else
			throw new IllegalArgumentException("Illegal loyalty filter type " + f.type.name());
	}),
	ARTIST(FilterType.ARTIST, (f) -> {
		if (f.type == FilterType.ARTIST)
			return TextFilterPanel.create((TextFilter)f);
		else
			throw new IllegalArgumentException("Illegal artist filter type " + f.type.name());
	}),
	CARD_NUMBER(FilterType.CARD_NUMBER, (f) -> {
		if (f.type == FilterType.CARD_NUMBER)
			return NumberFilterPanel.create((NumberFilter)f);
		else
			throw new IllegalArgumentException("Illegal card number filter type " + f.type.name());
	}),
	FORMAT_LEGALITY(FilterType.FORMAT_LEGALITY, (f) -> {
		if (f.type == FilterType.FORMAT_LEGALITY)
			return LegalityFilterPanel.create((LegalityFilter)f);
		else
			throw new IllegalArgumentException("Illegal legality filter type " + f.type.name());
	}),
	DEFAULTS(FilterType.DEFAULTS, (f) -> {
		if (f == null)
			return DefaultsFilterPanel.create();
		else
			throw new IllegalArgumentException("Illegal defaults filter type " + f.type.name());
	}),
	NONE(FilterType.NONE, (f) -> {
		if (f.type == FilterType.NONE)
			return NoneFilterPanel.create();
		else
			throw new IllegalArgumentException("Illegal none filter type " + f.type.name());
	}),
	ALL(FilterType.ALL, (f) -> {
		if (f.type == FilterType.ALL)
			return AllFilterPanel.create();
		else
			throw new IllegalArgumentException("Illegal all filter type " + f.type.name());
	});
	
	/**
	 * Type of filter this FilterPanelType corresponds to.
	 */
	public final FilterType type;
	/**
	 * Function for generating a filter editor panel.
	 */
	private Function<FilterLeaf<?>, FilterEditorPanel<?>> editor;
	
	/**
	 * Create a new FilterPanelType.
	 * 
	 * @param t FilterType corresponding to the new FilterPanelType
	 * @param e Function for creating a new filter editor panel out of the given
	 * FilterLeaf.
	 */
	private FilterPanelType(FilterType t, Function<FilterLeaf<?>, FilterEditorPanel<?>> e)
	{
		type = t;
		editor = e;
	}
	
	/**
	 * Create a new FilterEditorPanel out of the given filter.
	 * 
	 * @param filter Filter to use for initial contents
	 * @return The FilterEditorPanel that was created.
	 */
	public FilterEditorPanel<?> createPanel(FilterLeaf<?> filter)
	{
		return editor.apply(filter);
	}
	
	/**
	 * Create a new FilterEditorPanel with the default contents.
	 * 
	 * @return The FilterEditorPanel that was created.
	 */
	public FilterEditorPanel<?> createPanel()
	{
		return editor.apply(type.createFilter());
	}
	
	/**
	 * @return A String representation of this FilterPanelType,
	 * which is the name of its corresponding FilterType.
	 */
	@Override
	public String toString()
	{
		return type.toString();
	}
}
