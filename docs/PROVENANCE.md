# Provenance and clean-room boundary

The exact runtime artifact supplies T2 byte evidence for registry identity,
metadata, serialized NBT field names/types, absence of a BlueMap entity-state
resource, and the operator texture identity. The upstream tag
`1.21.1-1.10.1` resolves to commit
`d8554a27f6666caca0fddcc2ecab95944eb3ff77`, tree
`7a95bcfc47aa4aded3715e0f0e37f248385b4761`; that is correlation evidence,
not a reproducible-build claim.

The exact source license and runtime metadata declare All Rights Reserved.
No upstream implementation was copied or adapted. The frame proportions,
bar decomposition, seam-face-removal connection representation, capacity policy,
resource-model proof, and fallback rules are independently authored here.

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

The 2026-08-21 geometry correction used only two owner-supplied raster
observations and the project-owned implementation and tests:

- client comparison, 2,564 by 1,500 pixels, SHA-256
  `7371bf7579cfa6e23b1659c4b50b8bb5a9d9d9a8744da6612d69b165ef2a2fc0`;
- BlueMap staging comparison, 2,002 by 2,068 pixels, SHA-256
  `bbc565e5e4d2c4b4c6306abcda852f38db4cc4bdb636e7313109814923e8da8b`.

The visible client landmarks are qualitatively consistent with a cage
concentrated in the upper half of the attached host: a lower rail crosses the
host side with stone visible beneath it, while the upper ring appears near the
host top in this view. Those landmarks motivated the project-authored
`Y_MIN=1/2` and `HEIGHT=1/2` hypotheses; the raster does not establish exact
rail heights or coplanar planes. On the closest readable top ring, the
projected opening is approximately three quarters of the outer span along
multiple edges. That observation
motivated, but does not prove, the project-authored `1/8` rail hypothesis. The
nearly flush silhouette similarly motivated the project-authored `1/256`
nonzero-outset choice; it is not an exact measurement. The visual difference
between the screenshots motivated replacing per-part full-range UVs with
project-authored planar frame coordinates, but the screenshots do not prove
that UV stretching caused the difference or that planar mapping matches the
client. These choices require a fresh render and owner review.

The upper-half hypothesis leaves vertically adjacent frame envelopes
`63/128` block apart. Consequently UP/DOWN masks retain their caps and no
unsupported bridge is invented; only contacting horizontal envelopes may
omit seam faces. No LogisticsNetworks source, bytecode, model, texture,
renderer implementation, or earlier discarded patch/build output was
inspected for this correction.
