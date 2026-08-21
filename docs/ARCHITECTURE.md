# Architecture

The entrypoint installs three exact BlueMap 5.22 registrations before maps or
world NBT are constructed:

1. `EntityType` maps only `logisticsnetworks:logistics_node` to the narrow DTO.
2. `ResourcePack.Extension` activates only for one exact operator artifact,
   reserves the node texture in BlueMap's atlas, and rejects an unexpected
   entity-state route.
3. `RenderPassType` adds a bounded pass alongside BlueMap's stock block/entity
   passes; no registry iteration order is assumed.

For one tile the pass queries one block beyond the X/Z tile boundary, caps
matching node entities at 1,024, validates base entity position against
`AttachedPos` at either the exact placement-height or centered-height origin,
and preflights the attached host resource model. It drops
duplicate attached positions atomically, sorts the remaining positions, and
computes a six-bit neighbor mask. An exact-position wrapper primes BlueMap's
modulo-indexed neighborhood cache before every sparse host lookup. Only
neighbors that are both visible and valid are eligible; current-node validity
is intentionally not part of the lookup rule.

Host preflight requires exactly one matching or default blockstate variant
set, between one and 64 alternatives, and independently proves every
alternative to be the same occupied unit-cube shape: default renderer,
positive finite individual and total weight, right-angle transform, one
full-cube element, six faces, and resolvable textures. This admits Minecraft
1.21.1 stone's four
stock weighted stone/stone-mirrored choices without treating texture or UV
differences as frame geometry, while any unsupported alternative fails the
host closed.

The geometry program is project-owned: eight corner joints and twelve thin
axis-aligned edge bars around the upper half of the `AttachedPos` host. Its
bottom rail lies at host mid-height and its top rail lies on the host top
plane. The `1/8`-block rails and `1/256`-block outer offset are provisional
project-authored choices pending a fresh render. Each vertex receives a
project-authored planar coordinate from the complete frame, so a narrow face
samples a narrow texture strip rather than the full texture. The screenshot
comparison motivated this mapping but does not prove the cause of the visual
difference or establish client parity.

Seam omission is contact-gated. Horizontal neighboring envelopes overlap by
`1/128` block across their shared plane and may remove the eight matching
caps. Vertically adjacent upper-half frames have a `63/128`-block envelope
gap, so UP/DOWN connections retain all caps and each frame stays independently
closed. No bridge is synthesized without black-box evidence for one. The pass
retains the complete six-way connection mask for bounded indexing and future
evidence. It does not interpret or translate upstream entity-model JSON or
reproduce upstream client renderer geometry.

Before emission the pass mirrors BlueMap's stock entity cave-removal light
guard. Cutaway maps therefore do not retain a node overlay after the host is
suppressed as a dark cave.

Any pass exception or BlueMap model-capacity exception resets all geometry
added by this pass while retaining earlier stock geometry. Per-node malformed
or unsupported data emits no overlay. Global artifact or resource failure
leaves the pass inactive.
