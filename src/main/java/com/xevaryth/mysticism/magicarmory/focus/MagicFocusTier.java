package com.xevaryth.mysticism.magicarmory.focus;

public enum MagicFocusTier {
    FLINT_LEATHER(
        "flint", "flint_staff", "leather_tome", 15, false,
        new CastDefaults(3, 3.0D, 14.0D, 18, 160),
        new CastDefaults(4, 3.0D, 10.0D, 32, 160)
    ),
    AMETHYST(
        "amethyst", "amethyst_staff", "amethyst_tome", 18, false,
        new CastDefaults(4, 6.0D, 18.0D, 16, 384),
        new CastDefaults(8, 6.0D, 14.0D, 28, 384)
    ),
    DIAMOND(
        "diamond", "diamond_staff", "diamond_tome", 10, false,
        new CastDefaults(6, 9.0D, 22.0D, 14, 1561),
        new CastDefaults(12, 9.0D, 18.0D, 24, 1561)
    ),
    EMERALD(
        "emerald", "emerald_staff", "emerald_tome", 25, false,
        new CastDefaults(12, 12.0D, 24.0D, 12, 520),
        new CastDefaults(20, 12.0D, 20.0D, 22, 520)
    ),
    NETHERITE(
        "netherite", "netherite_staff", "netherite_tome", 15, true,
        new CastDefaults(10, 13.0D, 24.0D, 10, 2031),
        new CastDefaults(18, 13.0D, 22.0D, 20, 2031)
    ),
    ACRYL(
        "acryl", "acryl_staff", "acryl_tome", 25, false,
        new CastDefaults(6, 9.0D, 24.0D, 12, 1400),
        new CastDefaults(12, 9.0D, 20.0D, 24, 1400)
    );

    private final String key;
    private final String staffId;
    private final String tomeId;
    private final int enchantability;
    private final boolean fireResistant;
    private final CastDefaults staff;
    private final CastDefaults tome;

    MagicFocusTier(String key, String staffId, String tomeId, int enchantability,
                   boolean fireResistant, CastDefaults staff, CastDefaults tome) {
        this.key = key;
        this.staffId = staffId;
        this.tomeId = tomeId;
        this.enchantability = enchantability;
        this.fireResistant = fireResistant;
        this.staff = staff;
        this.tome = tome;
    }

    public String key() { return key; }
    public String staffId() { return staffId; }
    public String tomeId() { return tomeId; }
    public int enchantability() { return enchantability; }
    public boolean fireResistant() { return fireResistant; }
    public CastDefaults staffDefaults() { return staff; }
    public CastDefaults tomeDefaults() { return tome; }

    public record CastDefaults(int manaCost, double damage, double range,
                               int cooldownTicks, int durability) {}
}
