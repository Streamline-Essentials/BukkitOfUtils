package host.plas.bou.items;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Recipe;
import gg.drak.thebase.objects.Identified;
import gg.drak.thebase.lib.leonhard.storage.sections.FlatFileSection;

import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Configuration for a shaped crafting recipe, storing the three crafting grid lines,
 * ingredient mappings, and the result item identifier.
 */
@Getter @Setter
public class CraftingConfig implements Identified {
    /**
     * The unique identifier for this crafting recipe.
     *
     * @param identifier the identifier to set
     * @return the identifier
     */
    private String identifier;

    /**
     * The first line of the crafting grid pattern.
     *
     * @param line1 the first line to set
     * @return the first line
     */
    private String line1;

    /**
     * The second line of the crafting grid pattern.
     *
     * @param line2 the second line to set
     * @return the second line
     */
    private String line2;

    /**
     * The third line of the crafting grid pattern.
     *
     * @param line3 the third line to set
     * @return the third line
     */
    private String line3;

    /**
     * A map of single-character keys to material identifiers for the recipe ingredients.
     *
     * @param ingredients the ingredients map to set
     * @return the ingredients map
     */
    private ConcurrentSkipListMap<String, String> ingredients;

    /**
     * The result item identifier for this recipe.
     *
     * @param result the result identifier to set
     * @return the result identifier
     */
    private String result;

    /**
     * Constructs a new CraftingConfig with all recipe parameters.
     *
     * @param identifier  the unique identifier for this recipe
     * @param line1       the first line of the crafting grid pattern
     * @param line2       the second line of the crafting grid pattern
     * @param line3       the third line of the crafting grid pattern
     * @param ingredients a map of single-character keys to material identifiers
     * @param result      the result item identifier
     */
    public CraftingConfig(String identifier, String line1, String line2, String line3, ConcurrentSkipListMap<String, String> ingredients, String result) {
        this.identifier = identifier;
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.ingredients = ingredients;
        this.result = result;
    }

    /**
     * Registers this crafting recipe with the Bukkit server.
     */
    public void register() {
        ItemUtils.registerRecipe(this);
    }

    /**
     * Builds and returns the Bukkit {@link Recipe} for this configuration.
     *
     * @return the constructed shaped recipe
     */
    public Recipe getRecipe() {
        return ItemUtils.getRecipe(this);
    }

    /**
     * Creates a CraftingConfig by reading values from a configuration file section.
     *
     * @param section the configuration section to read from
     * @return a new CraftingConfig populated from the section
     */
    public static CraftingConfig fromConfig(FlatFileSection section) {
        return new CraftingConfig(
                section.getString("identifier"),
                section.getString("line1"),
                section.getString("line2"),
                section.getString("line3"),
                getIngredients(section),
                section.getString("result")
        );
    }

    /**
     * Reads the ingredients map from a configuration file section.
     *
     * @param section the configuration section containing the ingredients
     * @return a sorted map of ingredient keys to material identifiers
     */
    public static ConcurrentSkipListMap<String, String> getIngredients(FlatFileSection section) {
        ConcurrentSkipListMap<String, String> map = new ConcurrentSkipListMap<>();

        for (String key : section.singleLayerKeySet("ingredients")) {
            map.put(key, section.getString(key));
        }

        return map;
    }

    /**
     * Saves this crafting configuration to a configuration file section.
     *
     * @param section the configuration section to write to
     */
    public void save(FlatFileSection section) {
        section.set("identifier", getIdentifier());
        section.set("line1", getLine1());
        section.set("line2", getLine2());
        section.set("line3", getLine3());
        section.set("ingredients", getIngredients());
        section.set("result", getResult());
    }
}
