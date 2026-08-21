# Agent guide for BlueMap Logistics Networks Add-on

Read `/root/work/allthemons/AGENTS.md` and this file before changing this
repository. This is a standalone MIT BlueMap add-on, not a NeoForge mod and
not part of the root orchestration repository.

## Exact baseline

| Component | Identity |
| --- | --- |
| All the Mons | `1.2.0`, pack commit `c7bb230f21d14d26859d0b92548f089b3a493ad9` |
| Minecraft / NeoForge / Java | `1.21.1` / `21.1.248` / `21` |
| BlueMap | `5.22-agent.backport-5.22-mc1.21.1-2`, commit `9be321df995a1103808621d529eb72773e719d4d` |
| LogisticsNetworks | `logisticsnetworks-1.21.1-1.10.1.jar`, 988,995 bytes, SHA-256 `d94395da601ce93d8d7c9ffc434a018f6f46488303c654f6d6d5747961f56187` |

## Boundaries

- Own only `logisticsnetworks:logistics_node`. The computer block always uses
  BlueMap's stock path.
- Decode only `AttachedPos`, `Valid`, and `RenderVisible` beyond BlueMap's
  ordinary base entity fields.
- Render only conservatively proven full-cube hosts selected by one unique
  blockstate variant set. A bounded weighted set is admissible only when every
  alternative independently resolves to the same full-cube occupied geometry.
- Hidden nodes emit no geometry. Every malformed, duplicate, unsupported,
  ambiguous, capacity-exceeded, missing-resource, or wrong-artifact case is
  fail-closed.
- Preserve the exact visible-invalid adjacency rule: a visible invalid node is
  rendered, but it is not eligible as another node's neighbor.
- Never copy or adapt LogisticsNetworks renderer algorithms, source, classes,
  models, textures, captures, or meshes. The operator-installed node texture
  is referenced at runtime and never packaged.
- Keep `gallery/**` deterministic and synthetic. It is the accepted disposable
  staging fixture, but it is not natural saved-NBT or exact-client evidence.

## Minimum gate

```bash
gradle --no-daemon \
  -PlogisticsNetworksJar=/absolute/path/logisticsnetworks-1.21.1-1.10.1.jar \
  -PreleaseTag=v0.1.0-alpha.1 \
  clean check build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyPublicationArtifacts \
  verifyReleaseCandidate
```

Do not claim natural-fixture behavior, client parity, publication, or
deployment unless that exact gate runs. Release promotion also requires the
independent audit, hosted CI, exact annotated tag, and publication checks in
`docs/RELEASING.md`.
