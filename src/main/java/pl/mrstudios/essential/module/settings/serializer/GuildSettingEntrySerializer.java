package pl.mrstudios.essential.module.settings.serializer;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.module.settings.setting.GuildSetting;
import pl.mrstudios.essential.module.settings.setting.GuildSettingEntry;

import java.lang.reflect.Type;

public class GuildSettingEntrySerializer implements JsonSerializer<GuildSettingEntry>, JsonDeserializer<GuildSettingEntry> {

    @Override
    public @NotNull JsonElement serialize(
        @NotNull GuildSettingEntry entry,
        @NotNull Type typeOfSource,
        @NotNull JsonSerializationContext context
    ) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("key", entry.key().id);
        jsonObject.add("value", context.serialize(entry.value(), entry.key().type));

        return jsonObject;

    }

    @Override
    public @NotNull GuildSettingEntry deserialize(
        @NotNull JsonElement element,
        @NotNull Type typeOf,
        @NotNull JsonDeserializationContext context
    ) {

        JsonObject jsonObject = element.getAsJsonObject();
        GuildSettingEntry guildSettingEntry = new GuildSettingEntry(
            context.deserialize(jsonObject.get("key"), GuildSetting.class),
            null
        );

        guildSettingEntry.value(context.deserialize(jsonObject.get("value"), guildSettingEntry.key().type));

        return guildSettingEntry;

    }

}
