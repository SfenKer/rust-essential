package pl.mrstudios.essential.module.settings.serializer;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.module.settings.setting.GuildSetting;

import java.lang.reflect.Type;

import static pl.mrstudios.essential.module.settings.setting.GuildSetting.fromId;

public class GuildSettingSerializer implements JsonSerializer<GuildSetting>, JsonDeserializer<GuildSetting> {

    @Override
    public @NotNull JsonElement serialize(
        @NotNull GuildSetting option,
        @NotNull Type sourceType,
        @NotNull JsonSerializationContext context
    ) {
        return context.serialize(option.id, String.class);
    }

    @Override
    public @NotNull GuildSetting deserialize(
        @NotNull JsonElement element,
        @NotNull Type typeOf,
        @NotNull JsonDeserializationContext context
    ) {
        return fromId(context.deserialize(element, String.class));
    }

}
