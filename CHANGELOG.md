# 0.16.2

- Fixed Invocation activation by matching supported catalysts directly instead of requiring a successful runtime item-tag lookup.
- Added clear feedback when an Invocation tome is sneak-used without a supported offhand catalyst.
- Restored the original Wind Burst behavior: the caster, allies, and enemies inside the tome radius are pushed.
- Restored Flame and Wind Burst particles and sounds.
- Flame now ignites hostile targets before tome damage, allowing lethal casts to produce cooked drops.

# Changelog

## 0.16.1

- Rebalanced Arcane Impact to use vanilla Sharpness-style flat melee bonuses instead of scaling from staff spell damage.
- Raised the tome-holding arm in third person.
- Moved held tomes farther in front of the player and rotated the open pages toward the holder.

## 0.16.0

- Reworked staff and tome enchantment compatibility around vanilla enchantments.
- Added Arcane Reach, Arcane Impact, and Invocation.
- Made every tome tier use one shared 4-block area radius.
- Added Invocation catalysts for potions, ender pearls, TNT, arrows, fireworks, and milk.
- Added temporary arrow-rain projectiles that vanish on collision.
- Added Piercing-driven multi-target staff beams and stopped trails at the final target hit.
- Replaced universal staff trails with material-colored particles; Acryl trails use its dye color and partial alpha.
- Darkened Acryl beacon beam colors relative to vanilla stained glass.
- Corrected the animated held-tome pose while preserving static inventory models.

## 0.15.3 — Smooth glass, beacon support, and seamless pillars

- Added Smooth Acryl Glass and Smooth Acryl Glass Panes in the natural color and all sixteen dye colors.
- Acryl Glass blocks, panes, stairs, and slabs now tint beacon beams; natural Acryl produces a pink beam.
- Every full opaque Acryl block form can be used in a beacon base.
- Acryl Glass continues to drop itself without Silk Touch, including the new smooth forms.
- Reworked Acryl Pillars into a vertically tileable spiral pattern with chiseled-derived end caps.

## 0.15.2 — Held tome animation and smooth Acryl surfaces

- Restored animated BookModel pages for tomes only while held in first or third person.
- Kept the 0.15.1 flat tome models unchanged in inventories, hotbars, frames, and dropped-item contexts.
- Smoothed the regular Acryl surface used by blocks, slabs, and stairs.
- Made Smooth Acryl the same uninterrupted surface without the outer shell border.
- Applied the smoother bordered surface to regular Acryl Glass, including its slabs, stairs, and panes.

## 0.15.1 — Acryl architecture and focus progression

- Restored Flint, Amethyst, Diamond, Emerald, Netherite, and Acryl staffs.
- Restored Leather, Amethyst, Diamond, Emerald, Netherite, and Acryl AoE tomes.
- Removed the Lapis focus tier and kept runic ingredients removed.
- Rebuilt all Acryl block textures around segmented shulker-shell forms rather than quartz.
- Replaced Acryl Glass pillars, smooth blocks, and walls with regular, brick, and chiseled pane variants.
- Added matching-color internal-face culling to Acryl Glass full blocks and panes.
- Grouped colored construction recipes by block shape, following vanilla recipe-book behavior.

## 0.15.0 — Acryl consolidation

- Replaced Veilsteel, Resonite, and Riftrose with one material: Acryl.
- Added the Echo-Shard-gated Acryl recipe and renewable Warden Echo Shard drops.
- Added diamond-tier Acryl equipment, focused staffs, AoE tomes, mana enchantments, trims, and boss-proof building families.
