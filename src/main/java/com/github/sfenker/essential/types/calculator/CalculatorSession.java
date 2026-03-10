package com.github.sfenker.essential.types.calculator;

import com.github.sfenker.essential.types.resources.StructureExplosivesSet;
import org.jetbrains.annotations.NotNull;

import static com.github.sfenker.essential.registry.ItemCraftCostRegistry.itemCraftCost;
import static com.github.sfenker.essential.types.resources.StructureExplosivesSet.Structure;
import static java.util.stream.IntStream.of;

public class CalculatorSession {

    public Integer rockets;
    public Integer bombs;
    public Integer satchels;
    public Integer explosiveAmmo;

    /* Settings */
    public Structure currentStructure;
    public StructureExplosivesSet currentExplosivesSet;

    {
        this.bombs = 0;
        this.rockets = 0;
        this.satchels = 0;
        this.explosiveAmmo = 0;
    }

    public @NotNull Integer totalSulphurNeeded() {
        return of(
            this.rockets * itemCraftCost("rocket").costs.sulphur,
            this.bombs * itemCraftCost("timed_explosive_charge").costs.sulphur,
            this.satchels * itemCraftCost("satchel_explosive_charge").costs.sulphur,
            this.explosiveAmmo * itemCraftCost("explosive_ammo").costs.sulphur
        ).sum();
    }

}
