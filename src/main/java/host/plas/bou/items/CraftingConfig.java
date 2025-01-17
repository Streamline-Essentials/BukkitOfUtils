package host.plas.bou.items;

import de.leonhard.storage.sections.FlatFileSection;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Recipe;
import tv.quaint.objects.Identified;

import java.util.concurrent.ConcurrentSkipListMap;

@Getter @Setter
public class CraftingConfig implements Identified {
    private String identifier;

    private String line1;
    private String line2;
    private String line3;

    private ConcurrentSkipListMap<String, String> ingredients;

    private String result;

    public CraftingConfig(String identifier, String line1, String line2, String line3, ConcurrentSkipListMap<String, String> ingredients, String result) {
        this.identifier = identifier;
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.ingredients = ingredients;
        this.result = result;
    }

    public void register() {
        ItemUtils.registerRecipe(this);
    }

    public Recipe getRecipe() {
        return ItemUtils.getRecipe(this);
    }

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

    public static ConcurrentSkipListMap<String, String> getIngredients(FlatFileSection section) {
        ConcurrentSkipListMap<String, String> map = new ConcurrentSkipListMap<>();

        for (String key : section.singleLayerKeySet("ingredients")) {
            map.put(key, section.getString(key));
        }

        return map;
    }

    public void save(FlatFileSection section) {
        section.set("identifier", getIdentifier());
        section.set("line1", getLine1());
        section.set("line2", getLine2());
        section.set("line3", getLine3());
        section.set("ingredients", getIngredients());
        section.set("result", getResult());
    }
}
