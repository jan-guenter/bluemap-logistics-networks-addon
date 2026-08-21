# LogisticsNetworks synthetic node prototype gallery

This directory defines a tiny deterministic datapack for the exact
LogisticsNetworks `1.10.1` prototype. The required operator artifact is
`logisticsnetworks-1.21.1-1.10.1.jar`, 988,995 bytes, with SHA-256
`d94395da601ce93d8d7c9ffc434a018f6f46488303c654f6d6d5747961f56187`.
The gallery placements and block edits are confined to inclusive x `160..191`,
y `99..108`, z `160..191` in a disposable staging world. Its cleanup is not
spatially bounded: see the destructive-operation warning below. Never install
or invoke this datapack in production or in a shared dimension.

This is deliberately **synthetic prototype evidence, not a natural
saved-fixture release proof**. The node entities are created with bounded
`summon` commands using the exact narrow persisted-data contract. The owner
accepted the resulting disposable BlueMap staging render for the bounded
`0.1.0-alpha.1` release. A fixture placed through the real LogisticsNetworks
item flow, its sanitized saved entity-region NBT, and an exact-client
comparison remain unclaimed follow-up evidence.

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

`build` is a durable, two-stage build-once guard. When no build, clear, or
verification lifecycle is pending and `#builds = 0`, it marks the build
pending, acquires the exact four-chunk x/z `160..191` forceload, and schedules
the guarded internal build stage after a conservative 20-tick loading window.
That loaded stage performs cleanup, increments `#builds` exactly once, places
the fixtures, and starts verification. Repeated `build` calls while preparation
is pending or after the gallery is built do not schedule or mutate another
build.

**Destructive prototype-only operation:** after its 20-tick chunk-loading
window, the loaded build stage runs the exact supported
`/logisticsnetworks removeNodes` command before any summon. That command
targets **every currently loaded LogisticsNetworks node anywhere in
the command's current dimension**, including nodes outside the reserved
envelope; it is dimension-global, not gallery-bounded. The shared loaded
cleanup helper then clears only the bounded block envelope. This destructive
command is intentional because LogisticsNetworks rejects ordinary `/kill`;
use these functions only in a dedicated disposable prototype dimension with no
state worth retaining.

`clear` is also a guarded two-stage lifecycle. Its preparation cancels any
pending build and delayed verification, acquires the same four-chunk forceload,
and waits 20 ticks before the loaded stage runs the same dimension-global
removal and bounded block clear. The loaded clear resets the build and pending
guards and only then removes the forceload ticket. Repeated `clear` calls while
one is pending do not reschedule it. After clear completes, `build` may be
called directly for a deliberate fresh disposable run.

The verifier runs immediately after the final summon and again at 20 and 100
ticks relative to that loaded build stage. Every phase performs 94 retained
assertions: the build counter; four exact computer states; eleven exact stone
hosts; the exact total node count; and, for each node, an exact tag count,
centered identity/state NBT, plus absence of synthetic network ID, network
name, owner, label, and upgrades. Require:

```text
#immediate_checked = 94   #immediate_failures = 0
#20t_checked       = 94   #20t_failures       = 0
#100t_checked      = 94   #100t_failures      = 0
```

Each assertion has a stable semantic failure ID. A failed assertion alone
runs `say LN_GALLERY_FAIL:<id>` before incrementing `#failures`. Minecraft's
dedicated server records `/say` output in its server log even when no players
are connected, so the aggregate score contract remains compact while the log
identifies the exact failed assertion. Passing assertions emit no log entry.

`release` fails closed unless the build is complete, no lifecycle is pending,
the gallery ticket is still held, and all three retained phase snapshots are
exactly 94 checked with zero failures. Only then does it remove this gallery's
bounded forceload ticket. It never cancels pending checks and deliberately
retains the controls and synthetic nodes for BlueMap prototype inspection. If
any phase fails, inspect the stable failure IDs and use `clear`; the loaded
clear performs destructive cleanup before it removes the ticket.
