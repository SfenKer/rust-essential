package pl.mrstudios.essential.module.calculator.session;

import com.google.gson.Gson;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.module.calculator.resources.ItemCraftCost;
import pl.mrstudios.essential.module.calculator.resources.StructureRaidCost;

import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static pl.mrstudios.essential.utility.StreamUtility.readResource;

@SuppressWarnings("unchecked")
public class CalculatorSession {

    public Integer rockets;
    public Integer bombs;
    public Integer explosiveAmmo;

    /* Interaction Hook */
    public InteractionHook interactionHook;

    /* Settings */
    public StructureRaidCost currentStructure;

    public CalculatorSession(
            @NotNull InteractionHook interactionHook
    ) {

        this.rockets = 0;
        this.bombs = 0;
        this.explosiveAmmo = 0;

        /* Interaction Hook */
        this.interactionHook = interactionHook;

    }

    public @NotNull Integer totalSulphurNeeded() {
        return this.rockets * itemCraftCosts.get("rocket").costs.sulphur +
                this.bombs * itemCraftCosts.get("timed_explosive_charge").costs.sulphur +
                this.explosiveAmmo * itemCraftCosts.get("explosive_ammo").costs.sulphur;
    }

    protected static final Gson gson = new Gson();
    protected static final Map<String, ItemCraftCost> itemCraftCosts = ofEntries(
            stream(gson.fromJson(readResource("data/rust/calculator/item_craft_cost.json"), ItemCraftCost[].class))
                    .map((value) -> entry(value.id, value))
                    .toArray(Map.Entry[]::new)
    );

}
