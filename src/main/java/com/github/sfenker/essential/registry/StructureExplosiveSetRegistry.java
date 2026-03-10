package com.github.sfenker.essential.registry;

import com.github.sfenker.essential.types.resources.StructureExplosivesSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

import static com.github.sfenker.essential.utility.StreamUtility.jsonResource;
import static java.util.Arrays.stream;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

public class StructureExplosiveSetRegistry {

    public static @NotNull StructureExplosivesSet structureExplosivesSet(
        @NotNull String id
    ) {
        return structureExplosiveSetsRegistry.get(id);
    }

    static final String resourcePath = "assets/data/structure_explosives_set.json";
    static final Map<String, StructureExplosivesSet> structureExplosiveSetsRegistry =
        stream(jsonResource(resourcePath, StructureExplosivesSet[].class))
            .collect(toMap(
                (item) -> item.id,
                identity()
            ));

    public static @NotNull Collection<StructureExplosivesSet> structureExplosivesSetRegistry() {
        return structureExplosiveSetsRegistry.values();
    }

}
