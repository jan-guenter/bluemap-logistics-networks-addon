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
axis-aligned edge bars around a unit cube. Its outer envelope has a fixed
`1/64`-block outset so stock full-cube faces cannot depth-sort over coplanar
overlay fragments. A connection removes the eight coplanar part faces on the
corresponding side. It does not interpret or translate the upstream
entity-model JSON and does not reproduce the upstream client renderer's
procedural geometry.

Before emission the pass mirrors BlueMap's stock entity cave-removal light
guard. Cutaway maps therefore do not retain a node overlay after the host is
suppressed as a dark cave.

Any pass exception or BlueMap model-capacity exception resets all geometry
added by this pass while retaining earlier stock geometry. Per-node malformed
or unsupported data emits no overlay. Global artifact or resource failure
leaves the pass inactive.
