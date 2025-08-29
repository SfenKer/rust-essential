package pl.mrstudios.essential.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Names;
import org.jetbrains.annotations.NotNull;

import static eu.okaeri.configs.annotation.NameModifier.TO_LOWER_CASE;
import static eu.okaeri.configs.annotation.NameStrategy.HYPHEN_CASE;

@SuppressWarnings("deprecation")
@Names(strategy = HYPHEN_CASE, modifier = TO_LOWER_CASE)
public class Configuration extends OkaeriConfig {

    @Comment("Token of the bot from Discord Developer Portal.")
    public @NotNull String token = "ENTER_BOT_TOKEN_HERE";

    @Comment({ "", "Key for Rust Maps from Rust Maps Dashboard."})
    public @NotNull String rustMapsApiKey = "ENTER_API_KEY_HERE";

    @Comment({ "", "Premium Subscription SKU" })
    public @NotNull Long subscriptionSku = 0L;

}
