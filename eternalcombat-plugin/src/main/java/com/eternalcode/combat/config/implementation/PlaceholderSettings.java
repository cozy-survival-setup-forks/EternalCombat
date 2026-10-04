package com.eternalcode.combat.config.implementation;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class PlaceholderSettings extends OkaeriConfig {

    @Comment("Text returned by %eternalcombat_isInCombat_formatted% placeholder when the player is in combat")
    public String isInCombatFormattedTrue = "In Combat";

    @Comment("Text returned by %eternalcombat_isInCombat_formatted% placeholder when the player is out of combat")
    public String isInCombatFormattedFalse = "Not In Combat";

    @Comment("Text returned by %eternalcombat_pvp_status% placeholder when PvP is allowed at the player's location")
    public String pvpStatusAllowed = "PvP Allowed";

    @Comment("Text returned by %eternalcombat_pvp_status% placeholder when a protected region or claim blocks fights at the player's location")
    public String pvpStatusDisabled = "PvP Disabled";

    @Comment("Text returned by %eternalcombat_pvp_status% placeholder when the player is in combat (takes priority over the two above)")
    public String pvpStatusInCombat = "In Combat";

}
