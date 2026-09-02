# BlueMap Logistics Networks Add-on

[![CI](https://github.com/jan-guenter/bluemap-logistics-networks-addon/actions/workflows/ci.yml/badge.svg)](https://github.com/jan-guenter/bluemap-logistics-networks-addon/actions/workflows/ci.yml)

A narrow Java 21 add-on for BlueMap's exact 5.23 feature backport that gives persisted
`logisticsnetworks:logistics_node` entities a deterministic static frame on
ordinary full-cube hosts.

Version `0.1.0-alpha.2` is the owner-accepted BlueMap 5.23 release candidate.
It moves the alpha.1 rendering contract to the exact feature backport and
shared Adapter API, and preserves valid host admission through Camol's late
renderer wrapping.

The exact profile activates only for the All the Mons 1.2.0 runtime artifact
`logisticsnetworks-1.21.1-1.10.1.jar`, 988,995 bytes, SHA-256
`d94395da601ce93d8d7c9ffc434a018f6f46488303c654f6d6d5747961f56187`.
It registers a BlueMap entity DTO before world NBT is read, retains only
`AttachedPos`, `Valid`, and `RenderVisible`, then uses a bounded custom render
pass to build six-way adjacency.

Visible nodes receive an independently authored cuboid cage made from eight
corner joints and twelve edge bars. The one-block-tall cage spans local
`y=1/2..3/2`, leaving a visibly empty upper half above the full-cube host.
Provisional project-authored dimensions use `1/8`-block rails and a
`1/256`-block outset. Frame-wide planar UVs are a project-authored response to
the visually prominent prototype texture repetition; screenshot causation or
client parity is not claimed.

One upward top-sheet quad sits at local `y=351/256`, exactly the provisional
`1/256` outset below the top-rail underside. This independently chosen recess
avoids coplanar sheet/frame faces and their depth-order ambiguity. The sheet's
uniform pale-neutral RGBA texture is generated in memory under the synthetic
`bluemap_logistics_networks` namespace; no sheet PNG or upstream glass asset is
packaged. An isolated top sheet fills the inner aperture. Each evidenced
cardinal neighbor extends it exactly to the shared cell boundary and removes
that entire upper edge rail. Each exposed cardinal side also receives one
outward-facing vertical pane using the same generated material; a connected
side receives none. Side panes sit `1/256` inside the frame aperture, span from
`161/256` to `350/256`, and extend to a cell boundary only where a perpendicular
neighbor lets adjacent exterior panes meet there without area overlap. An upper
corner joint is removed only when both incident sides connect, while a vertical
post is removed when either incident side connects. This produces
project-authored isolated, straight, L-shaped,
and 2-by-2 surface hypotheses without an internal upper cross or central 2-by-2
post cluster. All sheets are visual approximations, not a client-renderer
reproduction.

Because the cage extends into `AttachedPos.UP`, that exact block must be air or
the node overlay fails closed. Vertical masks retain closed caps and receive no
invented bridges. A visible invalid node still receives its own frame, but
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
output. Renderer admission is captured before late resource-pack wrappers run,
so a wrapper such as Camol does not hide an otherwise admitted default-rendered
host. Variants that already used a custom renderer remain rejected.

The frame references the operator-installed `logisticsnetworks:entity/node`
texture. The sheet uses only the generated project-owned texture described
above. The upstream mod is All Rights Reserved; this project packages no
upstream source, class, model, texture, asset, algorithm, or derived mesh.

## Build

```bash
gradle --no-daemon \
  -PlogisticsNetworksJar=/absolute/path/logisticsnetworks-1.21.1-1.10.1.jar \
  -PreleaseTag=v0.1.0-alpha.2 \
  clean check build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyPublicationArtifacts \
  verifyReleaseCandidate
```

The production JAR is a plain add-on for BlueMap's `packs` directory. It has
no NeoForge metadata, bundled dependencies, or client hooks. Removing it and
restarting restores BlueMap's ordinary behavior without changing world data.

The alpha.2 production candidate passed the combined All the Mons 1.2.0
runtime review and owner visual acceptance on 2026-09-02. Neither release
claims a natural saved fixture or exact client-renderer parity. Publication is
allowed only from the independently audited pull request after its final-head
CI pass. See
[the release procedure](docs/RELEASING.md) and
[recorded candidate provenance](provenance/release.json).
