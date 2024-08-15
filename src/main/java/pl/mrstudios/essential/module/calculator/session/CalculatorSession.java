package pl.mrstudios.essential.module.calculator.session;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.module.calculator.resources.ItemCraftCost;
import pl.mrstudios.essential.module.calculator.resources.StructureExplosivesSet;

import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static pl.mrstudios.essential.module.calculator.resources.StructureExplosivesSet.Structure;
import static pl.mrstudios.essential.utility.StreamUtility.readResource;

@SuppressWarnings("unchecked")
public class CalculatorSession {

    public Integer rockets;
    public Integer bombs;
    public Integer satchels;
    public Integer explosiveAmmo;

    /* Settings */
    public Structure currentStructure;
    public StructureExplosivesSet currentExplosivesSet;

    public CalculatorSession() {
        this.rockets = 0;
        this.bombs = 0;
        this.satchels = 0;
        this.explosiveAmmo = 0;
    }

    public @NotNull Integer totalSulphurNeeded() {
        return this.rockets * itemCraftCosts.get("rocket").costs.sulphur +
                this.bombs * itemCraftCosts.get("timed_explosive_charge").costs.sulphur +
                this.satchels * itemCraftCosts.get("satchel_explosive_charge").costs.sulphur +
                this.explosiveAmmo * itemCraftCosts.get("explosive_ammo").costs.sulphur;
    }

    protected static final Gson gson = new Gson();
    protected static final Map<String, ItemCraftCost> itemCraftCosts = ofEntries(
            stream(gson.fromJson(readResource("data/rust/calculator/item_craft_cost.json"), ItemCraftCost[].class))
                    .map((value) -> entry(value.id, value))
                    .toArray(Map.Entry[]::new)
    );

}
