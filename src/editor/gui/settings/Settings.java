package editor.gui.settings;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import editor.collection.deck.CategorySpec;
import editor.database.characteristics.CardAttribute;

public final class Settings
{
    public static final class InventorySettings
    {
        public final String source;
        public final String file;
        public final String versionFile;
        public final String version;
        public final String location;
        public final String scans;
        public final Map<Long, Set<String>> tags;
        public final boolean update;
        public final boolean warn;
        public final List<CardAttribute> columns;
        public final Color background;
        public final Color stripe;

        protected InventorySettings(String source,
                                    String file,
                                    String versionFile,
                                    String version,
                                    String location,
                                    String scans,
                                    Map<Long, Set<String>> tags,
                                    boolean update,
                                    boolean warn,
                                    List<CardAttribute> columns,
                                    Color background,
                                    Color stripe)
        {
            this.source = source;
            this.file = file;
            this.versionFile = versionFile;
            this.version = version;
            this.location = location;
            this.scans = scans;
            this.tags = Collections.unmodifiableMap(tags.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> Collections.unmodifiableSet(new HashSet<>(e.getValue())))));
            this.update = update;
            this.warn = warn;
            this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
            this.background = background;
            this.stripe = stripe;
        }

        public String path()
        {
            return location + File.separator + file;
        }

        public Set<String> tags()
        {
            return tags.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        }

        public String url()
        {
            return source + file;
        }

        public String versionSite()
        {
            return source + versionFile;
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == null)
                return false;
            if (other == this)
                return true;
            if (!(other instanceof InventorySettings))
                return false;

            InventorySettings o = (InventorySettings)other;
            return source.equals(o.source) &&
                   file.equals(o.file) &&
                   versionFile.equals(o.versionFile) &&
                   version.equals(o.version) &&
                   location.equals(o.location) &&
                   scans.equals(o.scans) &&
                   tags.equals(o.tags) &&
                   update == o.update &&
                   warn == o.warn &&
                   columns.equals(o.columns) &&
                   background.equals(o.background) &&
                   stripe.equals(o.stripe);
        }
    }

    public static final class EditorSettings
    {
        public static final class RecentsSettings
        {
            public final int count;
            public final List<String> files;

            protected RecentsSettings(int count, List<String> files)
            {
                this.count = count;
                this.files = Collections.unmodifiableList(new ArrayList<>(files));
            }

            @Override
            public boolean equals(Object other)
            {
                if (other == null)
                    return false;
                if (other == this)
                    return true;
                if (!(other instanceof RecentsSettings))
                    return false;

                RecentsSettings o = (RecentsSettings)other;
                return count == o.count && files.equals(o.files);
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(count, files);
            }
        }

        public static final class CategoriesSettings
        {
            public final List<CategorySpec> presets;
            public final int rows;

            protected CategoriesSettings(List<CategorySpec> presets, int rows)
            {
                this.presets = Collections.unmodifiableList(new ArrayList<>(presets));
                this.rows = rows;
            }

            @Override
            public boolean equals(Object other)
            {
                if (other == null)
                    return false;
                if (other == this)
                    return true;
                if (!(other instanceof CategoriesSettings))
                    return false;

                CategoriesSettings o = (CategoriesSettings)other;
                return presets.equals(o.presets) && rows == o.rows;
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(presets, rows);
            }
        }

        public static final class HandSettings
        {
            public final int size;
            public final String rounding;
            public final Color background;

            protected HandSettings(int size, String rounding, Color background)
            {
                this.size = size;
                this.rounding = rounding;
                this.background = background;
            }

            @Override
            public boolean equals(Object other)
            {
                if (other == null)
                    return false;
                if (other == this)
                    return true;
                if (!(other instanceof HandSettings))
                    return false;
                
                HandSettings o = (HandSettings)other;
                return size == o.size && rounding.equals(o.rounding) && background.equals(o.background);
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(size, rounding, background);
            }
        }

        public final RecentsSettings recents;
        public final int explicits;
        public final CategoriesSettings categories;
        public final List<CardAttribute> columns;
        public final Color stripe;
        public final HandSettings hand;

        protected EditorSettings(int recentsCount, List<String> recentsFiles,
                                 int explicits,
                                 List<CategorySpec> presetCategories, int categoryRows,
                                 List<CardAttribute> columns, Color stripe,
                                 int handSize, String handRounding, Color handBackground)
        {
            this.recents = new RecentsSettings(recentsCount, recentsFiles);
            this.explicits = explicits;
            this.categories = new CategoriesSettings(presetCategories, categoryRows);
            this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
            this.stripe = stripe;
            this.hand = new HandSettings(handSize, handRounding, handBackground);
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == null)
                return false;
            if (other == this)
                return true;
            if (!(other instanceof EditorSettings))
                return false;
            
            EditorSettings o = (EditorSettings)other;
            return recents.equals(o.recents) &&
                   explicits == o.explicits &&
                   categories.equals(o.categories) &&
                   columns.equals(o.columns) &&
                   stripe.equals(o.stripe) &&
                   hand.equals(o.hand);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(recents, explicits, categories, columns, stripe, hand);
        }
    }

    public final InventorySettings inventory;
    public final EditorSettings editor;
    public final String cwd;

    protected Settings(String inventorySource, String inventoryFile, String inventoryVersionFile, String inventoryVersion, String inventoryLocation, String inventoryScans, Map<Long, Set<String>> inventoryTags, boolean inventoryUpdate, boolean inventoryWarn, List<CardAttribute> inventoryColumns, Color inventoryBackground, Color inventoryStripe, int recentsCount, List<String> recentsFiles, int explicits, List<CategorySpec> presetCategories, int categoryRows, List<CardAttribute> editorColumns, Color editorStripe, int handSize, String handRounding, Color handBackground, String cwd)
    {
        this.inventory = new InventorySettings(inventorySource, inventoryFile, inventoryVersionFile, inventoryVersion, inventoryLocation, inventoryScans, inventoryTags, inventoryUpdate, inventoryWarn, inventoryColumns, inventoryBackground, inventoryStripe);
        this.editor = new EditorSettings(recentsCount, recentsFiles, explicits, presetCategories, categoryRows, editorColumns, editorStripe, handSize, handRounding, handBackground);
        this.cwd = cwd;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (this == other)
            return true;
        if (!(other instanceof Settings))
            return false;

        Settings o = (Settings)other;
        return inventory.equals(o.inventory) && editor.equals(o.editor) && cwd == o.cwd;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(inventory, editor, cwd);
    }
}