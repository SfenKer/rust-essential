package pl.mrstudios.essential.module.settings.setting;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor
@AllArgsConstructor
public class GuildSettingEntry {

    @SerializedName("key")
    private @NotNull GuildSetting key;

    @SerializedName("value")
    private @Nullable Object value;

    public @NotNull GuildSetting key() {
        return this.key;
    }

    @SuppressWarnings("unchecked")
    public @Nullable <VALUE> VALUE value() {
        return (VALUE) this.value;
    }

    public void value(
        @Nullable Object value
    ) {
        this.value = value;
    }

    public static @NotNull GuildSettingEntry guildSettingEntry(
        @NotNull GuildSetting key
    ) {
        return new GuildSettingEntry(key, null);
    }

    public static @NotNull GuildSettingEntry guildSettingEntry(
        @NotNull GuildSetting key,
        @Nullable Object value
    ) {
        return new GuildSettingEntry(key, value);
    }

}
