package com.github.sfenker.essential.registry;

import com.github.sfenker.essential.types.resources.ItemCraftCost;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.github.sfenker.essential.utility.StreamUtility.jsonResource;
import static java.util.Arrays.stream;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

public class ItemCraftCostRegistry {

    public static @NotNull ItemCraftCost itemCraftCost(
        @NotNull String id
    ) {
        return craftCostRegistry.get(id);
    }

    static final String resourcePath = "assets/data/item_craft_cost.json";
    static final Map<String, ItemCraftCost> craftCostRegistry =
        stream(jsonResource(resourcePath, ItemCraftCost[].class))
            .collect(toMap(
                (item) -> item.id,
                identity()
            ));

}
