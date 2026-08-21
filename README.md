# BlueMap Logistics Networks Add-on

A narrow Java 21 BlueMap 5.22 prototype that gives persisted
`logisticsnetworks:logistics_node` entities a deterministic static frame on
ordinary full-cube hosts.

The exact profile activates only for the All the Mons 1.2.0 runtime artifact
`logisticsnetworks-1.21.1-1.10.1.jar`, 988,995 bytes, SHA-256
`d94395da601ce93d8d7c9ffc434a018f6f46488303c654f6d6d5747961f56187`.
It registers a BlueMap entity DTO before world NBT is read, retains only
`AttachedPos`, `Valid`, and `RenderVisible`, then uses a bounded custom render
pass to build six-way adjacency.

Visible nodes receive an independently authored cuboid frame made from eight
corner joints and twelve edge bars. The half-block-tall frame occupies the
upper half of the `AttachedPos` host. Provisional project-authored dimensions
use `1/8`-block rails and a `1/256`-block outset. Frame-wide planar UVs are a
project-authored response to the visually prominent prototype texture
repetition; screenshot causation or client parity is not claimed. An adjacent
visible and valid node removes matching seam faces only where the
translated envelopes contact horizontally. Vertically adjacent upper-half
frames remain `63/128` block apart, keep their caps, and receive no invented
bridge geometry. A visible invalid node still receives its own frame, but
cannot be selected as another node's neighbor. Hidden nodes emit no geometry.

This first slice deliberately supports only ordinary resource-model outcomes
containing one unrotated `[0,0,0]..[16,16,16]` element with all six faces and
resolvable textures. The blockstate must select one unique, bounded variant
set; every weighted alternative must independently prove that same occupied
full-cube geometry and may rotate it only by right angles. Texture and UV
differences are irrelevant because BlueMap renders the stock host separately.
Multipart, overlapping, over-capacity, custom-renderer, missing-texture,
partial, dynamic, or otherwise ambiguous hosts receive no node overlay. The
underlying host, including `logisticsnetworks:computer`, remains stock BlueMap
output.

The frame references the operator-installed
`logisticsnetworks:entity/node` texture. The upstream mod is All Rights
Reserved; this project packages no upstream source, class, model, texture,
asset, algorithm, or derived mesh.

## Build

```bash
gradle --no-daemon \
  -PlogisticsNetworksJar=/absolute/path/logisticsnetworks-1.21.1-1.10.1.jar \
  clean check build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication
```

The production JAR is a plain add-on for BlueMap's `packs` directory. It has
no NeoForge metadata, bundled dependencies, or client hooks. Removing it and
restarting restores BlueMap's ordinary behavior without changing world data.

Natural entity-NBT fixture capture, modded-client comparison, disposable
staging, owner visual acceptance, and any release remain open gates.
