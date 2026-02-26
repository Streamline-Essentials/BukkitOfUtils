package host.plas.bou.items;

import org.bukkit.inventory.Recipe;
import gg.drak.thebase.objects.Identified;
import gg.drak.thebase.lib.leonhard.storage.sections.FlatFileSection;
import java.util.concurrent.ConcurrentSkipListMap;

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
        return new CraftingConfig(section.getString("identifier"), section.getString("line1"), section.getString("line2"), section.getString("line3"), getIngredients(section), section.getString("result"));
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

    public String getIdentifier() {
        return this.identifier;
    }

    public String getLine1() {
        return this.line1;
    }

    public String getLine2() {
        return this.line2;
    }

    public String getLine3() {
        return this.line3;
    }

    public ConcurrentSkipListMap<String, String> getIngredients() {
        return this.ingredients;
    }

    public String getResult() {
        return this.result;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public void setLine1(final String line1) {
        this.line1 = line1;
    }

    public void setLine2(final String line2) {
        this.line2 = line2;
    }

    public void setLine3(final String line3) {
        this.line3 = line3;
    }

    public void setIngredients(final ConcurrentSkipListMap<String, String> ingredients) {
        this.ingredients = ingredients;
    }

    public void setResult(final String result) {
        this.result = result;
    }
}
