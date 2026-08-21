#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Generate the bounded LogisticsNetworks synthetic prototype gallery."""

from __future__ import annotations

import argparse
from dataclasses import dataclass
import hashlib
import json
from pathlib import Path
import sys
import uuid


ROOT = Path(__file__).resolve().parent
NAMESPACE = "logisticsnetworks_gallery"
OBJECTIVE = "ln_gallery"
NODE_TYPE = "logisticsnetworks:logistics_node"
HOST_BLOCK = "minecraft:stone"
FAILURE_LOG_PREFIX = "LN_GALLERY_FAIL:"
ENVELOPE = {
    "min_x": 160,
    "max_x": 191,
    "min_y": 99,
    "max_y": 108,
    "min_z": 160,
    "max_z": 191,
}
FORCELOAD = "160 160 191 191"
FLOOR = "160 99 160 191 99 191"
STATE_SCORES = (
    "#builds",
    "#build_pending",
    "#clear_pending",
    "#verification_pending",
    "#ticket_held",
)
UUID_NAMESPACE = uuid.UUID("743cbf8c-bce6-4ba6-99ad-577c1bc80c3c")
ABSENT_DYNAMIC_PATHS = (
    "NetworkId",
    "NetworkName",
    "OwnerUUID",
    "NodeLabel",
    "Upgrades",
)
DIRECTION_OFFSETS = {
    "down": (0, -1, 0),
    "up": (0, 1, 0),
    "north": (0, 0, -1),
    "south": (0, 0, 1),
    "west": (-1, 0, 0),
    "east": (1, 0, 0),
}


@dataclass(frozen=True)
class Computer:
    cell: str
    label: str
    x: int
    y: int
    z: int
    facing: str

    @property
    def block_spec(self) -> str:
        return f"logisticsnetworks:computer[facing={self.facing}]"


@dataclass(frozen=True)
class Node:
    cell: str
    topology: str
    label: str
    tag: str
    x: int
    y: int
    z: int
    valid: bool
    visible: bool
    connections: tuple[str, ...]

    @property
    def attached_long(self) -> int:
        return pack_block_pos(self.x, self.y, self.z)

    @property
    def entity_uuid(self) -> uuid.UUID:
        identity = f"{NODE_TYPE}:{self.cell}@{self.x},{self.y},{self.z}"
        return uuid.uuid5(UUID_NAMESPACE, identity)

    @property
    def uuid_words(self) -> tuple[int, int, int, int]:
        words = tuple(
            (self.entity_uuid.int >> shift) & 0xFFFFFFFF
            for shift in (96, 64, 32, 0)
        )
        return tuple(word if word < 0x80000000 else word - 0x100000000 for word in words)

    @property
    def center(self) -> tuple[float, float, float]:
        return self.x + 0.5, self.y + 0.5, self.z + 0.5


COMPUTERS = (
    Computer("A1", "stock computer north", 164, 100, 164, "north"),
    Computer("A2", "stock computer east", 170, 100, 164, "east"),
    Computer("A3", "stock computer south", 176, 100, 164, "south"),
    Computer("A4", "stock computer west", 182, 100, 164, "west"),
)

NODES = (
    Node("B1", "isolated", "visible isolated", "ln_b1", 164, 100, 171, True, True, ()),
    Node("B2", "hidden-control", "hidden zero-geometry control", "ln_b2", 170, 100, 171, True, False, ()),
    Node("C1", "straight", "straight west", "ln_c1", 176, 100, 171, True, True, ("east",)),
    Node("C2", "straight", "straight east", "ln_c2", 177, 100, 171, True, True, ("west",)),
    Node("D1", "l-triad", "L corner", "ln_d1", 164, 100, 179, True, True, ("south", "east")),
    Node("D2", "l-triad", "L east arm", "ln_d2", 165, 100, 179, True, True, ("west",)),
    Node("D3", "l-triad", "L south arm", "ln_d3", 164, 100, 180, True, True, ("north",)),
    Node("E1", "square-2x2", "square north-west", "ln_e1", 176, 100, 179, True, True, ("south", "east")),
    Node("E2", "square-2x2", "square north-east", "ln_e2", 177, 100, 179, True, True, ("south", "west")),
    Node("E3", "square-2x2", "square south-west", "ln_e3", 176, 100, 180, True, True, ("north", "east")),
    Node("E4", "square-2x2", "square south-east", "ln_e4", 177, 100, 180, True, True, ("north", "west")),
)


def pack_block_pos(x: int, y: int, z: int) -> int:
    """Return the signed vanilla BlockPos packed long."""
    value = ((x & 0x3FFFFFF) << 38) | ((z & 0x3FFFFFF) << 12) | (y & 0xFFF)
    return value if value < (1 << 63) else value - (1 << 64)


def text_bytes(text: str) -> bytes:
    return (text.rstrip() + "\n").encode("utf-8")


def json_bytes(value: object) -> bytes:
    return (json.dumps(value, indent=2, sort_keys=True) + "\n").encode("utf-8")


def bool_byte(value: bool) -> str:
    return "1b" if value else "0b"


def format_float(value: float, suffix: str) -> str:
    return f"{value:.1f}{suffix}"


def uuid_int_array(node: Node) -> str:
    return "[I;" + ",".join(str(word) for word in node.uuid_words) + "]"


def summon_nbt(node: Node) -> str:
    return (
        "{UUID:" + uuid_int_array(node)
        + ",Motion:[0.0d,0.0d,0.0d]"
        + ",Rotation:[0.0f,0.0f]"
        + f',Tags:["ln_gallery","{node.tag}"]'
        + f",AttachedPos:{node.attached_long}L"
        + f",Valid:{bool_byte(node.valid)}"
        + f",RenderVisible:{bool_byte(node.visible)}"
        + ",Highlighted:0b}"
    )


def expected_entity_nbt(node: Node) -> str:
    center = node.center
    return (
        "{UUID:" + uuid_int_array(node)
        + ",Motion:[0.0d,0.0d,0.0d]"
        + ",Rotation:[0.0f,0.0f]"
        + f",AttachedPos:{node.attached_long}L"
        + f",Valid:{bool_byte(node.valid)}"
        + f",RenderVisible:{bool_byte(node.visible)}"
        + ",Highlighted:0b"
        + ",Pos:["
        + ",".join(format_float(value, "d") for value in center)
        + "]}"
    )


def envelope_selector(*extra: str) -> str:
    terms = [
        f"type={NODE_TYPE}",
        f'x={ENVELOPE["min_x"]}',
        f'y={ENVELOPE["min_y"]}',
        f'z={ENVELOPE["min_z"]}',
        f'dx={ENVELOPE["max_x"] - ENVELOPE["min_x"]}',
        f'dy={ENVELOPE["max_y"] - ENVELOPE["min_y"]}',
        f'dz={ENVELOPE["max_z"] - ENVELOPE["min_z"]}',
    ]
    terms.extend(extra)
    return "@e[" + ",".join(terms) + "]"


def node_selector(node: Node, *, single: bool = False) -> str:
    extra = [f"tag={node.tag}"]
    if single:
        extra.extend(("sort=arbitrary", "limit=1"))
    return envelope_selector(*extra)


def placements_tsv() -> bytes:
    lines = [
        "cell\tkind\ttopology\tlabel\tx\ty\tz\tblock_state\tentity_tag\t"
        "attached_pos_long\tuuid\tvalid\trender_visible\tconnections"
    ]
    for computer in COMPUTERS:
        lines.append(
            "\t".join(
                (
                    computer.cell,
                    "stock-computer",
                    "control",
                    computer.label,
                    str(computer.x),
                    str(computer.y),
                    str(computer.z),
                    computer.block_spec,
                    "-",
                    "-",
                    "-",
                    "-",
                    "-",
                    "-",
                )
            )
        )
    for node in NODES:
        lines.append(
            "\t".join(
                (
                    node.cell,
                    "synthetic-node",
                    node.topology,
                    node.label,
                    str(node.x),
                    str(node.y),
                    str(node.z),
                    HOST_BLOCK,
                    node.tag,
                    str(node.attached_long),
                    str(node.entity_uuid),
                    "1" if node.valid else "0",
                    "1" if node.visible else "0",
                    ",".join(node.connections) if node.connections else "-",
                )
            )
        )
    return text_bytes("\n".join(lines))


def build_wrapper_function() -> bytes:
    built_message = json.dumps(
        [{"text": "LogisticsNetworks gallery already built; guard retained it."}],
        separators=(",", ":"),
    )
    pending_message = json.dumps(
        [{"text": "LogisticsNetworks gallery lifecycle already pending."}],
        separators=(",", ":"),
    )
    return text_bytes(
        "# Generated by gallery/generate.py; do not edit.\n"
        f"execute if score #builds {OBJECTIVE} matches 1.. run tellraw @a "
        f"{built_message}\n"
        f"execute if score #build_pending {OBJECTIVE} matches 1 if score "
        f"#builds {OBJECTIVE} matches 0 run tellraw @a {pending_message}\n"
        f"execute if score #clear_pending {OBJECTIVE} matches 1 run tellraw @a "
        f"{pending_message}\n"
        f"execute if score #verification_pending {OBJECTIVE} matches 1 if score "
        f"#builds {OBJECTIVE} matches 0 run tellraw @a {pending_message}\n"
        f"execute if score #builds {OBJECTIVE} matches 0 if score "
        f"#build_pending {OBJECTIVE} matches 0 if score #clear_pending "
        f"{OBJECTIVE} matches 0 if score #verification_pending {OBJECTIVE} "
        f"matches 0 run function {NAMESPACE}:prepare_build"
    )


def build_once_function() -> bytes:
    return text_bytes(
        "# Generated by gallery/generate.py; do not edit.\n"
        f"execute if score #build_pending {OBJECTIVE} matches 1 if score "
        f"#builds {OBJECTIVE} matches 0 if score #clear_pending {OBJECTIVE} "
        f"matches 0 run function {NAMESPACE}:build_loaded"
    )


def build_loaded_function() -> bytes:
    lines = [
        "# Generated by gallery/generate.py; do not edit.",
        f"function {NAMESPACE}:loaded_cleanup",
        f"scoreboard players add #builds {OBJECTIVE} 1",
        f"fill {FLOOR} minecraft:smooth_stone",
    ]
    for computer in COMPUTERS:
        lines.extend(
            (
                f"# {computer.cell}: {computer.label}; stock control",
                f"setblock {computer.x} {computer.y} {computer.z} {computer.block_spec}",
            )
        )
    for node in NODES:
        center = " ".join(format_float(value, "") for value in node.center)
        lines.extend(
            (
                f"# {node.cell}: {node.label}; synthetic prototype only",
                f"setblock {node.x} {node.y} {node.z} {HOST_BLOCK}",
                f"summon {NODE_TYPE} {center} {summon_nbt(node)}",
            )
        )
    lines.extend(
        (
            f"scoreboard players set #build_pending {OBJECTIVE} 0",
            f"scoreboard players set #verification_pending {OBJECTIVE} 1",
            f"function {NAMESPACE}:verify_immediate",
            f"schedule function {NAMESPACE}:verify_20t 20t replace",
            f"schedule function {NAMESPACE}:verify_100t 100t replace",
        )
    )
    return text_bytes("\n".join(lines))


def append_assertion(lines: list[str], failure_id: str, command: str) -> None:
    lines.append(f"{command} run say {FAILURE_LOG_PREFIX}{failure_id}")
    lines.append(f"{command} run scoreboard players add #failures {OBJECTIVE} 1")
    lines.append(f"scoreboard players add #checked {OBJECTIVE} 1")


def append_score_match(lines: list[str], failure_id: str, expected: int) -> None:
    append_assertion(
        lines,
        failure_id,
        f"execute unless score #observed {OBJECTIVE} matches {expected}",
    )


def verify_function() -> bytes:
    lines = [
        "# Generated by gallery/generate.py; do not edit.",
        f"scoreboard players set #failures {OBJECTIVE} 0",
        f"scoreboard players set #checked {OBJECTIVE} 0",
    ]
    append_assertion(
        lines,
        "build_count_exact_1",
        f"execute unless score #builds {OBJECTIVE} matches 1",
    )
    for computer in COMPUTERS:
        append_assertion(
            lines,
            f"computer_{computer.cell}_exact_state",
            f"execute unless block {computer.x} {computer.y} {computer.z} "
            f"{computer.block_spec}",
        )
    for node in NODES:
        append_assertion(
            lines,
            f"host_{node.cell}_stone",
            f"execute unless block {node.x} {node.y} {node.z} {HOST_BLOCK}",
        )

    lines.append(
        f"execute store result score #observed {OBJECTIVE} run execute if entity "
        f"{envelope_selector()}"
    )
    append_score_match(lines, "node_envelope_count_11", len(NODES))

    for node in NODES:
        lines.append(f"# {node.cell}: exact live synthetic entity checks")
        lines.append(
            f"execute store result score #observed {OBJECTIVE} run execute if entity "
            f"{node_selector(node)}"
        )
        append_score_match(lines, f"node_{node.cell}_tag_count_1", 1)
        append_assertion(
            lines,
            f"node_{node.cell}_identity_state_nbt",
            f"execute unless data entity {node_selector(node, single=True)} "
            f"{expected_entity_nbt(node)}",
        )
        for path in ABSENT_DYNAMIC_PATHS:
            append_assertion(
                lines,
                f"node_{node.cell}_absent_{path}",
                f"execute if data entity {node_selector(node, single=True)} {path}",
            )
    return text_bytes("\n".join(lines))


def phase_function(phase: str, announce: bool) -> bytes:
    lines = [
        "# Generated by gallery/generate.py; do not edit.",
        f"function {NAMESPACE}:verify",
        f"scoreboard players operation #{phase}_failures {OBJECTIVE} = "
        f"#failures {OBJECTIVE}",
        f"scoreboard players operation #{phase}_checked {OBJECTIVE} = "
        f"#checked {OBJECTIVE}",
    ]
    if announce:
        payload = [
            {"text": f"LogisticsNetworks gallery {phase}: "},
            {"score": {"name": f"#{phase}_checked", "objective": OBJECTIVE}},
            {"text": " checks, "},
            {"score": {"name": f"#{phase}_failures", "objective": OBJECTIVE}},
            {"text": " failures"},
        ]
        lines.append("tellraw @a " + json.dumps(payload, separators=(",", ":")))
    if phase == "100t":
        lines.append(
            f"scoreboard players set #verification_pending {OBJECTIVE} 0"
        )
    return text_bytes("\n".join(lines))


def release_guard() -> str:
    requirements = (
        ("#builds", "1"),
        ("#build_pending", "0"),
        ("#clear_pending", "0"),
        ("#verification_pending", "0"),
        ("#ticket_held", "1"),
        ("#immediate_checked", "94"),
        ("#immediate_failures", "0"),
        ("#20t_checked", "94"),
        ("#20t_failures", "0"),
        ("#100t_checked", "94"),
        ("#100t_failures", "0"),
    )
    conditions = " ".join(
        f"if score {name} {OBJECTIVE} matches {value}"
        for name, value in requirements
    )
    return f"execute {conditions} run function {NAMESPACE}:release_once"


def base_files() -> dict[Path, bytes]:
    function_root = Path(f"datapack/data/{NAMESPACE}/function")
    return {
        Path("placements.tsv"): placements_tsv(),
        Path("datapack/pack.mcmeta"): json_bytes(
            {
                "pack": {
                    "description": (
                        "ATM 1.2.0 LogisticsNetworks 1.10.1 synthetic "
                        "BlueMap prototype gallery; not natural-fixture proof"
                    ),
                    "pack_format": 48,
                }
            }
        ),
        Path("datapack/data/minecraft/tags/function/load.json"): json_bytes(
            {"values": [f"{NAMESPACE}:load"]}
        ),
        function_root / "load.mcfunction": text_bytes(
            "# Generated by gallery/generate.py; do not edit.\n"
            f"scoreboard objectives add {OBJECTIVE} dummy\n"
            + "\n".join(
                f"scoreboard players add {score} {OBJECTIVE} 0"
                for score in STATE_SCORES
            )
        ),
        function_root / "build.mcfunction": build_wrapper_function(),
        function_root / "prepare_build.mcfunction": text_bytes(
            "# Generated by gallery/generate.py; do not edit.\n"
            f"scoreboard players set #build_pending {OBJECTIVE} 1\n"
            f"forceload add {FORCELOAD}\n"
            f"scoreboard players set #ticket_held {OBJECTIVE} 1\n"
            f"schedule function {NAMESPACE}:build_once 20t replace"
        ),
        function_root / "build_once.mcfunction": build_once_function(),
        function_root / "build_loaded.mcfunction": build_loaded_function(),
        function_root / "clear.mcfunction": text_bytes(
            "# Generated by gallery/generate.py; do not edit.\n"
            f"execute if score #clear_pending {OBJECTIVE} matches 0 run function "
            f"{NAMESPACE}:prepare_clear"
        ),
        function_root / "prepare_clear.mcfunction": text_bytes(
            "# Generated by gallery/generate.py; do not edit.\n"
            f"schedule clear {NAMESPACE}:build_once\n"
            f"schedule clear {NAMESPACE}:verify_20t\n"
            f"schedule clear {NAMESPACE}:verify_100t\n"
            f"scoreboard players set #build_pending {OBJECTIVE} 0\n"
            f"scoreboard players set #verification_pending {OBJECTIVE} 0\n"
            f"scoreboard players set #clear_pending {OBJECTIVE} 1\n"
            f"forceload add {FORCELOAD}\n"
            f"scoreboard players set #ticket_held {OBJECTIVE} 1\n"
            f"schedule function {NAMESPACE}:clear_once 20t replace"
        ),
        function_root / "clear_once.mcfunction": text_bytes(
            "# Generated by gallery/generate.py; do not edit.\n"
            f"execute if score #clear_pending {OBJECTIVE} matches 1 run function "
            f"{NAMESPACE}:clear_loaded"
        ),
        function_root / "clear_loaded.mcfunction": text_bytes(
            "# Generated by gallery/generate.py; do not edit.\n"
            f"function {NAMESPACE}:loaded_cleanup\n"
            f"scoreboard players set #builds {OBJECTIVE} 0\n"
            f"scoreboard players set #build_pending {OBJECTIVE} 0\n"
            f"scoreboard players set #verification_pending {OBJECTIVE} 0\n"
            f"scoreboard players set #clear_pending {OBJECTIVE} 0\n"
            f"forceload remove {FORCELOAD}\n"
            f"scoreboard players set #ticket_held {OBJECTIVE} 0"
        ),
        function_root / "loaded_cleanup.mcfunction": text_bytes(
            "# Generated by gallery/generate.py; do not edit.\n"
            "# PROTOTYPE ONLY: destructively targets every loaded "
            "LogisticsNetworks node dimension-wide.\n"
            "logisticsnetworks removeNodes\n"
            "fill 160 99 160 191 108 191 minecraft:air"
        ),
        function_root / "verify.mcfunction": verify_function(),
        function_root / "verify_immediate.mcfunction": phase_function(
            "immediate", True
        ),
        function_root / "verify_20t.mcfunction": phase_function("20t", False),
        function_root / "verify_100t.mcfunction": phase_function("100t", True),
        function_root / "release.mcfunction": text_bytes(
            "# Generated by gallery/generate.py; do not edit.\n"
            + release_guard()
        ),
        function_root / "release_once.mcfunction": text_bytes(
            "# Generated by gallery/generate.py; do not edit.\n"
            f"forceload remove {FORCELOAD}\n"
            f"scoreboard players set #ticket_held {OBJECTIVE} 0"
        ),
    }


def sha256(payload: bytes) -> str:
    return hashlib.sha256(payload).hexdigest()


def generated_files() -> dict[Path, bytes]:
    files = base_files()
    checksums = "".join(
        f"{sha256(files[path])}  {path.as_posix()}\n" for path in sorted(files)
    )
    files[Path("SHA256SUMS")] = checksums.encode("ascii")
    return files


def write_or_check(check: bool) -> int:
    failures: list[str] = []
    files = generated_files()
    for relative, payload in files.items():
        path = ROOT / relative
        if check:
            if not path.is_file():
                failures.append(f"missing generated file: {relative}")
            elif path.read_bytes() != payload:
                failures.append(f"generated file differs: {relative}")
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(payload)
    if failures:
        print("\n".join(failures), file=sys.stderr)
        return 1
    action = "checked" if check else "wrote"
    print(f"{action} {len(files)} deterministic gallery files")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    return write_or_check(args.check)


if __name__ == "__main__":
    raise SystemExit(main())
