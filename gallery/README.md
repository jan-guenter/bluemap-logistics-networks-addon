# LogisticsNetworks synthetic node prototype gallery

This directory defines a tiny deterministic datapack for the exact
LogisticsNetworks `1.10.1` prototype. The required operator artifact is
`logisticsnetworks-1.21.1-1.10.1.jar`, 988,995 bytes, with SHA-256
`d94395da601ce93d8d7c9ffc434a018f6f46488303c654f6d6d5747961f56187`.
The gallery is confined to inclusive x `160..191`, y `99..108`, z `160..191`
in a disposable staging world. It does not touch production or cluster state.

This is deliberately **synthetic prototype evidence, not a natural
saved-fixture release proof**. The node entities are created with bounded
`summon` commands using the exact narrow persisted-data contract. A fixture
placed through the real LogisticsNetworks item flow, its sanitized saved
entity-region NBT, an exact-client comparison, and owner acceptance remain
release blockers.

## Cells

| Cell | Anchors | Fixture intent |
| --- | --- | --- |
| A | `164/170/176/182 100 164` | stock `logisticsnetworks:computer` controls facing north/east/south/west |
| B | `164 100 171`, `170 100 171` | isolated visible-valid node and isolated hidden-valid zero-geometry control |
| C | `176..177 100 171` | adjacent visible-valid straight pair |
| D | `164..165 100 179`, `164 100 180` | visible-valid L triad |
| E | `176..177 100 179..180` | complete visible-valid 2x2 topology |

Every node is attached to an ordinary `minecraft:stone` full-cube
resource-model host. The four computers are stock controls and receive no
custom block-entity data. `placements.tsv` records every exact block position,
packed `AttachedPos` long, deterministic UUID, visibility/validity byte, and
intended six-face connection direction.

Each synthetic node supplies ordinary base `UUID`, `Motion`, `Rotation`, and
two selector tags plus the exact custom fields `AttachedPos` (long), `Valid`
(byte), `RenderVisible` (byte), and `Highlighted:0b`. `Pos` is supplied by the
summon coordinate at the exact host center `(x+0.5, y+0.5, z+0.5)` and is
checked in the live entity NBT. The exact summon entity type supplies the
saved `id=logisticsnetworks:logistics_node`; the verifier proves that identity
with type-restricted selectors because Minecraft's live entity data accessor
does not expose a separately editable `id` field.

No network UUID/name, owner, node label, upgrade, channel contents, item
contents, highlight, activity, status label, particle, or animation state is
injected. Exact 1.10.1 may serialize its own empty default channel compound on
a later save; that runtime default is not synthetic content and is not treated
as byte-for-byte omission evidence.

## Generate, lint, and package

Run from the repository root:

```text
PYTHONDONTWRITEBYTECODE=1 python3 gallery/generate.py --check
PYTHONDONTWRITEBYTECODE=1 python3 gallery/lint.py
bash gallery/package.sh /tmp/bluemap-logistics-networks-gallery.zip
```

Running `gallery/generate.py` without `--check` rewrites only the generated
ledger, datapack files, and `SHA256SUMS`. Packaging uses sorted paths, fixed
file modes, stripped ZIP metadata, and a fixed DOS epoch. It bundles no
LogisticsNetworks, Minecraft, or BlueMap code, model, texture, or other
resource.

## Staging functions and retained checks

```text
/function logisticsnetworks_gallery:build
/function logisticsnetworks_gallery:verify
/function logisticsnetworks_gallery:clear
/function logisticsnetworks_gallery:release
```

`build` is a strong build-once guard. Only `#builds = 0` may call the internal
`build_once` mutator. `clear` cancels delayed checks, kills only
`logisticsnetworks:logistics_node` entities inside the reserved envelope, and
clears only that envelope. For a deliberate fresh disposable run, call
`clear`, set `#builds` in objective `ln_gallery` back to zero, then call
`build` again.

The verifier runs immediately and at 20 and 100 ticks. Every phase performs
94 retained assertions: the build counter; four exact computer states; eleven
exact stone hosts; the exact total node count; and, for each node, an exact
tag count, centered identity/state NBT, plus absence of synthetic network ID,
network name, owner, label, and upgrades. Require:

```text
#immediate_checked = 94   #immediate_failures = 0
#20t_checked       = 94   #20t_failures       = 0
#100t_checked      = 94   #100t_failures      = 0
```

`release` cancels delayed checks and removes only this gallery's bounded
forceload ticket. It deliberately retains the controls and synthetic nodes for
BlueMap prototype inspection.
