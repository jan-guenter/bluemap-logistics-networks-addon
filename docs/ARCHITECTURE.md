# Architecture

The entrypoint installs three exact BlueMap 5.23 registrations before maps or
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
and preflights the attached host resource model. Because the overlay reaches
into the block above the host, that exact block must report an air state before
the node is indexed. This rejects obstructed headroom and suppresses the lower
overlay in a vertically stacked full-cube pair. It drops
duplicate attached positions atomically, sorts the remaining positions, and
computes a six-bit neighbor mask. An exact-position wrapper primes BlueMap's
modulo-indexed neighborhood cache before every sparse host lookup. Only
neighbors that are both visible and valid are eligible; current-node validity
is intentionally not part of the lookup rule.

Host preflight requires exactly one matching or default blockstate variant
set, between one and 64 alternatives, and independently proves every
alternative to be the same occupied unit-cube shape: originally default
renderer, positive finite individual and total weight, right-angle transform,
one full-cube element, six faces, and resolvable textures. Renderer identity is
captured during extension bake before late resource wrappers run. This admits
Minecraft
1.21.1 stone's four
stock weighted stone/stone-mirrored choices without treating texture or UV
differences as frame geometry, while any unsupported alternative fails the
host closed.

The geometry program is project-owned: eight corner joints and twelve thin
axis-aligned edge bars form a one-block-tall cage over local `y=1/2..3/2`.
The `1/8`-block rails and `1/256`-block outer offset remain provisional
project-authored choices pending a fresh render. Each frame vertex receives a
project-authored planar coordinate from the complete cage, so a narrow face
samples a narrow texture strip rather than the full texture.

The top surface is one upward quad per admitted visible node at local
`y=351/256`, exactly the `1/256` outset below the top-rail underside. This
project-authored recess prevents a sheet from sharing a plane with any
horizontal frame face. A cardinal neighbor extends that node's sheet to the
exact local cell boundary and removes the entire shared upper edge rail. An
upper corner joint is retained when either incident side is exposed and omitted
only when both incident sides connect; a vertical post is retained only when
neither incident side connects.
Sheets therefore meet at boundaries without positive area overlap; 2-by-2
layouts have neither an internal upper cross nor a raised central post cluster.
Straight seams and connected L sides have no vertical posts. Lower rails and
corners, plus conservative vertical-cap behavior otherwise, remain unchanged.
UP/DOWN never omit caps or synthesize bridges.

Every exposed cardinal side has one outward-wound vertical sheet; a connected
side has none, so no pane occupies a shared interface. Its project-authored
plane is `1/256` inward from the inner rail face and its vertical interval is
`y=161/256..350/256`, leaving the same safety inset at the lower rail and top
sheet. Along the other horizontal axis it reaches the exact cell boundary when
a perpendicular neighbor is present and otherwise stops at the `1/8` aperture
edge. Collinear exterior panes therefore meet at boundary lines without
positive-area pane/pane or pane/frame coplanarity. Frame/post topology is not
otherwise changed by this fast visual-approximation pass.

The sheet material is a uniform 1-by-1 RGBA `(232,236,236,48)` image generated
with BlueMap's MIT `Texture.from` API during resource-extension bake, after
ordinary texture loading. The synthetic key is reserved during texture
collection. Any preexisting key, generation error, or post-generation identity
mismatch leaves the profile inactive. Render-pass construction verifies the
exact generated image and both texture-gallery material indices again. No PNG
is bundled. The upward and vertical faces and all topology rules are independent
visual approximations from owner feedback and screenshot evidence, including the `1/256`
depth-order gap, not client parity.

The emitter retains the texture gallery and the two stable resource keys, not
their numeric material IDs. BlueMap may clear and repopulate the same gallery
while preserving existing render-pass instances, which can renumber both IDs.
Every emit therefore resolves and validates the current nonzero, distinct IDs
before the first `TileModel` allocation. A missing or aliased mapping throws
before any custom geometry is added, allowing the pass-level atomic rollback
contract to remain intact.

Before emission the pass mirrors BlueMap's stock entity cave-removal light
guard. Cutaway maps therefore do not retain a node overlay after the host is
suppressed as a dark cave.

Any pass exception or BlueMap model-capacity exception resets all geometry
added by this pass while retaining earlier stock geometry. Per-node malformed
or unsupported data emits no overlay. Global artifact or resource failure
leaves the pass inactive.
