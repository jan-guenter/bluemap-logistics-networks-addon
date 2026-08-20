#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Lint the generated LogisticsNetworks gallery without starting Minecraft."""

from __future__ import annotations

from collections import Counter
import json
from pathlib import Path
import re
import sys

sys.dont_write_bytecode = True
import generate


ROOT = Path(__file__).resolve().parent
EXPECTED_CHECKS = (
    1
    + len(generate.COMPUTERS)
    + len(generate.NODES)
    + 1
    + len(generate.NODES) * (2 + len(generate.ABSENT_DYNAMIC_PATHS))
)


def fail(message: str) -> None:
    raise ValueError(message)


def actual_connections(node: generate.Node) -> tuple[str, ...]:
    positions = {
        (candidate.x, candidate.y, candidate.z): candidate
        for candidate in generate.NODES
    }
    result: list[str] = []
    for direction, (dx, dy, dz) in generate.DIRECTION_OFFSETS.items():
        neighbor = positions.get((node.x + dx, node.y + dy, node.z + dz))
        if neighbor is not None and neighbor.visible and neighbor.valid:
            result.append(direction)
    return tuple(result)


def main() -> int:
    expected = generate.generated_files()
    for relative, payload in expected.items():
        path = ROOT / relative
        if not path.is_file() or path.read_bytes() != payload:
            fail(f"generated file differs: {relative}")

    expected_datapack_files = {
        relative for relative in expected if relative.parts[0] == "datapack"
    }
    actual_datapack_files = {
        path.relative_to(ROOT)
        for path in (ROOT / "datapack").rglob("*")
        if path.is_file()
    }
    if actual_datapack_files != expected_datapack_files:
        fail("datapack contains unexpected or missing files")

    metadata = json.loads(
        (ROOT / "datapack/pack.mcmeta").read_text(encoding="utf-8")
    )
    if metadata["pack"].get("pack_format") != 48:
        fail("gallery must target the exact Minecraft 1.21.1 pack format")
    if "not natural-fixture proof" not in metadata["pack"].get("description", ""):
        fail("pack description must retain the synthetic-evidence warning")
    json.loads(
        (ROOT / "datapack/data/minecraft/tags/function/load.json").read_text(
            encoding="utf-8"
        )
    )

    if len(generate.COMPUTERS) != 4 or len(generate.NODES) != 11:
        fail("gallery must retain four computer controls and eleven nodes")
    if tuple(computer.facing for computer in generate.COMPUTERS) != (
        "north",
        "east",
        "south",
        "west",
    ):
        fail("stock computer controls must cover the four horizontal facings")
    topology_census = Counter(node.topology for node in generate.NODES)
    if topology_census != Counter(
        {
            "isolated": 1,
            "hidden-control": 1,
            "straight": 2,
            "l-triad": 3,
            "square-2x2": 4,
        }
    ):
        fail(f"unexpected topology census: {topology_census}")
    if Counter((node.valid, node.visible) for node in generate.NODES) != Counter(
        {(True, True): 10, (True, False): 1}
    ):
        fail("gallery must contain ten visible-valid and one hidden-valid node")

    coordinates = [
        (placement.x, placement.y, placement.z)
        for placement in (*generate.COMPUTERS, *generate.NODES)
    ]
    if len(set(coordinates)) != len(coordinates):
        fail("all computer and node host coordinates must be unique")
    envelope = generate.ENVELOPE
    for coordinate in coordinates:
        x, y, z = coordinate
        if not (
            envelope["min_x"] <= x <= envelope["max_x"]
            and envelope["min_y"] <= y <= envelope["max_y"]
            and envelope["min_z"] <= z <= envelope["max_z"]
        ):
            fail(f"placement escaped the safe envelope: {coordinate}")
        if y != 100:
            fail(f"gallery anchor escaped the canonical y=100 plane: {coordinate}")

    tags = [node.tag for node in generate.NODES]
    uuids = [node.entity_uuid for node in generate.NODES]
    if len(set(tags)) != len(tags) or not all(tag.startswith("ln_") for tag in tags):
        fail("node selector tags must be unique and gallery-scoped")
    if len(set(uuids)) != len(uuids):
        fail("node UUIDs must be unique")
    if any(value.version != 5 or value.variant != "specified in RFC 4122" for value in uuids):
        fail("node UUIDs must be deterministic RFC 4122 UUIDv5 values")
    for node in generate.NODES:
        if node.attached_long != generate.pack_block_pos(node.x, node.y, node.z):
            fail(f"noncanonical packed AttachedPos: {node.cell}")
        if actual_connections(node) != node.connections:
            fail(
                f"topology drift for {node.cell}: expected {node.connections}, "
                f"observed {actual_connections(node)}"
            )

    function_root = ROOT / f"datapack/data/{generate.NAMESPACE}/function"
    build = (function_root / "build.mcfunction").read_text(encoding="utf-8")
    build_once = (function_root / "build_once.mcfunction").read_text(
        encoding="utf-8"
    )
    clear = (function_root / "clear.mcfunction").read_text(encoding="utf-8")
    load = (function_root / "load.mcfunction").read_text(encoding="utf-8")
    release = (function_root / "release.mcfunction").read_text(encoding="utf-8")
    verify = (function_root / "verify.mcfunction").read_text(encoding="utf-8")
    all_functions = "\n".join(
        path.read_text(encoding="utf-8")
        for path in sorted(function_root.glob("*.mcfunction"))
    )

    if len(re.findall(r"^setblock ", build_once, re.MULTILINE)) != 15:
        fail("build_once must set exactly four computers and eleven hosts")
    summon_lines = re.findall(r"^summon .*", build_once, re.MULTILINE)
    if len(summon_lines) != len(generate.NODES):
        fail("build_once must summon exactly eleven nodes")
    for node in generate.NODES:
        center = " ".join(generate.format_float(value, "") for value in node.center)
        expected_summon = (
            f"summon {generate.NODE_TYPE} {center} {generate.summon_nbt(node)}"
        )
        if build_once.count(expected_summon) != 1:
            fail(f"missing exact synthetic summon: {node.cell}")
    if build_once.count("Highlighted:0b") != len(generate.NODES):
        fail("every synthetic node must explicitly disable highlight")
    if "Highlighted:1b" in build_once:
        fail("highlighted synthetic nodes are forbidden")
    for path in generate.ABSENT_DYNAMIC_PATHS + ("Channels",):
        if path in build_once:
            fail(f"synthetic node payload injected forbidden dynamic field: {path}")

    retained_checks = len(
        re.findall(
            rf"^scoreboard players add #checked {generate.OBJECTIVE} 1$",
            verify,
            re.MULTILINE,
        )
    )
    if retained_checks != EXPECTED_CHECKS or EXPECTED_CHECKS != 94:
        fail(
            f"verify must retain exactly 94 checks, observed {retained_checks}"
        )
    if len(
        re.findall(
            rf"^execute store result score #observed {generate.OBJECTIVE} "
            r"run execute if entity ",
            verify,
            re.MULTILINE,
        )
    ) != len(generate.NODES) + 1:
        fail("verify must count the envelope and every unique node tag")
    for node in generate.NODES:
        if verify.count(generate.expected_entity_nbt(node)) != 1:
            fail(f"verify lost exact centered identity/state NBT: {node.cell}")
        for path in generate.ABSENT_DYNAMIC_PATHS:
            expected_absence = (
                f"execute if data entity {generate.node_selector(node, single=True)} "
                f"{path}"
            )
            if verify.count(expected_absence) != 1:
                fail(f"verify lost dynamic-field absence check: {node.cell}/{path}")

    guard_call = (
        f"execute if score #builds {generate.OBJECTIVE} matches 0 run function "
        f"{generate.NAMESPACE}:build_once"
    )
    if build.count(guard_call) != 1 or "matches 1.. run tellraw @a" not in build:
        fail("build wrapper must enforce and announce the build-once guard")
    forbidden_wrapper_mutators = ("setblock ", "summon ", "kill ", "fill ")
    if any(token in build for token in forbidden_wrapper_mutators):
        fail("guarded build wrapper must not mutate the gallery directly")
    increment = f"scoreboard players add #builds {generate.OBJECTIVE} 1"
    if build_once.count(increment) != 1 or increment in build:
        fail("build counter must increment exactly once inside build_once")

    expected_kill = f"kill {generate.envelope_selector()}"
    if re.findall(r"^kill .*", clear, re.MULTILINE) != [expected_kill]:
        fail("clear must kill only exact-type nodes in the bounded envelope")
    expected_clear = "fill 160 99 160 191 108 191 minecraft:air"
    if re.findall(r"^fill .* minecraft:air$", clear, re.MULTILINE) != [
        expected_clear
    ]:
        fail("clear must cover the complete bounded envelope exactly once")
    if f"forceload add {generate.FORCELOAD}" not in build_once:
        fail("build_once must add the exact bounded forceload ticket")
    if f"forceload remove {generate.FORCELOAD}" not in release:
        fail("release must remove the exact bounded forceload ticket")
    if "forceload" in load:
        fail("datapack load must not create a forceload ticket")
    if "kill " in release or "fill " in release:
        fail("release must retain gallery controls and nodes")

    for phase, delay in (("20t", "20t"), ("100t", "100t")):
        schedule = (
            f"schedule function {generate.NAMESPACE}:verify_{phase} "
            f"{delay} replace"
        )
        if build_once.count(schedule) != 1:
            fail(f"missing exact {phase} retained-state schedule")
        clear_schedule = f"schedule clear {generate.NAMESPACE}:verify_{phase}"
        if clear.count(clear_schedule) != 1 or release.count(clear_schedule) != 1:
            fail(f"clear/release must cancel the {phase} retained check")

    forbidden_operations = (
        "particle ",
        "item replace ",
        "loot ",
        "give ",
        "effect ",
        "tp ",
        "teleport ",
        "data merge entity ",
    )
    for line in all_functions.lower().splitlines():
        command = line.lstrip()
        for token in forbidden_operations:
            if command.startswith(token) or f" run {token}" in command:
                fail(f"forbidden gallery operation present: {token}")
    for line in summon_lines:
        if not line.startswith(f"summon {generate.NODE_TYPE} "):
            fail("gallery may summon only logisticsnetworks:logistics_node")

    readme = (ROOT / "README.md").read_text(encoding="utf-8")
    if "not a natural\nsaved-fixture release proof" not in readme:
        fail("README must retain the natural-fixture release blocker")
    if (
        "d94395da601ce93d8d7c9ffc434a018f6f46488303c654f6d6d5747961f56187"
        not in readme
    ):
        fail("README must pin the exact LogisticsNetworks artifact")

    print(
        "LogisticsNetworks gallery lint passed: 4 stock controls, 11 synthetic "
        f"nodes, {EXPECTED_CHECKS} checks/phase, bounded prototype evidence only"
    )
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except ValueError as error:
        print(f"lint failed: {error}", file=sys.stderr)
        raise SystemExit(1)
