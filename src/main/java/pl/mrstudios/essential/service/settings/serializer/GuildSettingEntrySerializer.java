package pl.mrstudios.essential.service.settings.serializer;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.service.settings.setting.GuildSetting;
import pl.mrstudios.essential.service.settings.setting.GuildSettingContainer;

import java.lang.reflect.Type;

public class GuildSettingEntrySerializer implements JsonSerializer<GuildSettingContainer>, JsonDeserializer<GuildSettingContainer> {

    @Override
    public @NotNull JsonElement serialize(
        @NotNull GuildSettingContainer entry,
        @NotNull Type typeOfSource,
        @NotNull JsonSerializationContext context
    ) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("key", entry.key().id);
        jsonObject.add("value", context.serialize(entry.value(), entry.key().type));

        return jsonObject;

    }

    @Override
    public @NotNull GuildSettingContainer deserialize(
        @NotNull JsonElement element,
        @NotNull Type typeOf,
        @NotNull JsonDeserializationContext context
    ) {

        JsonObject jsonObject = element.getAsJsonObject();
        GuildSettingContainer container = new GuildSettingContainer(
            context.deserialize(jsonObject.get("key"), GuildSetting.class),
            null
        );

        container.value(context.deserialize(jsonObject.get("value"), container.key().type));

        return container;

    }

}
