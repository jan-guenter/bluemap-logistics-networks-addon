# Provenance and clean-room boundary

The exact runtime artifact supplies T2 byte evidence for registry identity,
metadata, serialized NBT field names/types, absence of a BlueMap entity-state
resource, and the operator texture identity. The upstream tag
`1.21.1-1.10.1` resolves to commit
`d8554a27f6666caca0fddcc2ecab95944eb3ff77`, tree
`7a95bcfc47aa4aded3715e0f0e37f248385b4761`; that is correlation evidence,
not a reproducible-build claim.

The exact source license and runtime metadata declare All Rights Reserved.
No upstream implementation was copied or adapted. The frame proportions, bar
decomposition, top-sheet material and topology, connection representation,
headroom and capacity policies, resource-model proof, and fallback rules are
independently authored here.

Project-owned MIT reuse is explicit: the BlueMap entrypoint, exact-artifact
detector, registration/archive boundary and low-level cuboid emitter structure
adapt patterns from BlueMap Pipez Add-on `v0.1.0-alpha.1` at `fa3e773a...`
and BlueMap Integrated Dynamics Add-on `v0.1.0-alpha.1` at `bbc8f502...`.
Neither source contains LogisticsNetworks-derived behavior or assets.

The source license SHA-256 observed for the correlated revision is
`54ef830d6b8c7d1eb58ef65e35af499aec60e901bf7cdbad3cd291828a0d52b4`.
The exact operator node texture is 4,291 bytes with SHA-256
`03194c53acc840f953e44838e1086a350f27036a0fcb44165bb5a1786ae7885e`;
it is referenced only by resource key and never stored in this repository.

## Owner-screenshot correction evidence

The latest 2026-08-21 geometry correction used only two new owner-supplied
raster observations, BlueMap's MIT APIs, and the clean committed project-owned
implementation and tests. These observations supersede the earlier
half-height hypothesis:

- client comparison, 2,564 by 1,500 pixels, SHA-256
  `fe05c48c9406e26f944ea2702b297f50bfa4c6f698db6709572250cd1d6d53f4`;
- BlueMap staging comparison, 2,002 by 2,068 pixels, SHA-256
  `cc2d23026fd81af2e4f63f36ab486183fa46ac9c2caafb86db8b2ecde29ea6ed`.

The client raster is qualitatively consistent with a cage spanning local
`y=1/2..3/2`: its bottom ring lies around the host mid-plane while its top ring
stands roughly one block higher, leaving an empty upper half above the host.
The apparent up surface is also consistent with a faint transparent sheet,
and cardinal groups are consistent with glass-like merging rather than the
internal upper crosses visible in the BlueMap comparison. Rasters do not prove
exact geometry, material, alpha, winding, or hidden faces.

The new implementation therefore uses the independently authored full-height
cage, one upward sheet, and cardinal-only topology. A subsequent project-owned
staging render exposed a stable raised square at the center of a 2-by-2 group,
formed by the four independently authored internal vertical corner posts. It
does not establish client behavior. The correction applies the same
both-incident connection predicate to upper corner joints and vertical corner
posts, removing that internal cluster while retaining posts at straight seam
endpoints and around L perimeters.

The sheet sits at `y=351/256`, an independently chosen `1/256` recess below the
top-rail underside that prevents positive-area coplanar sheet/frame faces. The
uniform 1-by-1 RGBA `(232,236,236,48)` material, exact sheet plane, one-sided
face, upper-edge removal, and corner/post rules are project-authored visual
approximations. The earlier screenshot reading still weakly supports, but does
not measure, the retained provisional `1/8` rail and `1/256` outset choices.
Planar frame UVs remain a project-owned hypothesis. None of these choices is a
client-parity claim; all require a fresh BlueMap render and owner review.

The full-height envelope reaches into `AttachedPos.UP`, so exact-air headroom
is required before indexing. UP/DOWN masks conservatively retain caps and add
no bridge despite envelope contact. No diagonal topology is inferred. No
LogisticsNetworks source, bytecode, model, texture, renderer implementation,
algorithm, or earlier discarded patch/build output was inspected for this
correction.

The subsequent owner feedback was limited to the visual result: the top faces
now connect, while the other glass sides do not. The fast follow-up therefore
adds independently authored outward-facing panes only on exposed cardinal
sides, reusing the project-generated material. Pane planes, `1/256` safety
insets, boundary-extension rule, winding, and omission at connected interfaces
are project-authored visual approximations. Existing frame/post topology is
unchanged. No upstream LogisticsNetworks source, bytecode, model, texture,
renderer, or research evidence was inspected for this follow-up; one fresh
BlueMap visual review remains required.
