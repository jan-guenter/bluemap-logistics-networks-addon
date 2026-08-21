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
FAILURE_ID_PATTERN = re.compile(r"[A-Za-z0-9_]+")


def fail(message: str) -> None:
    raise ValueError(message)


def command_lines(function: str) -> list[str]:
    return [
        line
        for line in function.splitlines()
        if line and not line.startswith("#")
    ]


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
    prepare_build = (function_root / "prepare_build.mcfunction").read_text(
        encoding="utf-8"
    )
    build_once = (function_root / "build_once.mcfunction").read_text(
        encoding="utf-8"
    )
    build_loaded = (function_root / "build_loaded.mcfunction").read_text(
        encoding="utf-8"
    )
    clear = (function_root / "clear.mcfunction").read_text(encoding="utf-8")
    prepare_clear = (function_root / "prepare_clear.mcfunction").read_text(
        encoding="utf-8"
    )
    clear_once = (function_root / "clear_once.mcfunction").read_text(
        encoding="utf-8"
    )
    clear_loaded = (function_root / "clear_loaded.mcfunction").read_text(
        encoding="utf-8"
    )
    loaded_cleanup = (function_root / "loaded_cleanup.mcfunction").read_text(
        encoding="utf-8"
    )
    load = (function_root / "load.mcfunction").read_text(encoding="utf-8")
    release = (function_root / "release.mcfunction").read_text(encoding="utf-8")
    release_once = (function_root / "release_once.mcfunction").read_text(
        encoding="utf-8"
    )
    verify = (function_root / "verify.mcfunction").read_text(encoding="utf-8")
    verify_100t = (function_root / "verify_100t.mcfunction").read_text(
        encoding="utf-8"
    )
    all_functions = "\n".join(
        path.read_text(encoding="utf-8")
        for path in sorted(function_root.glob("*.mcfunction"))
    )

    if len(re.findall(r"^setblock ", build_loaded, re.MULTILINE)) != 15:
        fail("build_loaded must set exactly four computers and eleven hosts")
    summon_lines = re.findall(r"^summon .*", build_loaded, re.MULTILINE)
    if len(summon_lines) != len(generate.NODES):
        fail("build_loaded must summon exactly eleven nodes")
    for node in generate.NODES:
        center = " ".join(generate.format_float(value, "") for value in node.center)
        expected_summon = (
            f"summon {generate.NODE_TYPE} {center} {generate.summon_nbt(node)}"
        )
        if build_loaded.count(expected_summon) != 1:
            fail(f"missing exact synthetic summon: {node.cell}")
    if build_loaded.count("Highlighted:0b") != len(generate.NODES):
        fail("every synthetic node must explicitly disable highlight")
    if "Highlighted:1b" in build_loaded:
        fail("highlighted synthetic nodes are forbidden")
    for path in generate.ABSENT_DYNAMIC_PATHS + ("Channels",):
        if path in build_loaded:
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
    expected_failure_ids = [
        "build_count_exact_1",
        *(f"computer_{computer.cell}_exact_state" for computer in generate.COMPUTERS),
        *(f"host_{node.cell}_stone" for node in generate.NODES),
        "node_envelope_count_11",
    ]
    for node in generate.NODES:
        expected_failure_ids.extend(
            (
                f"node_{node.cell}_tag_count_1",
                f"node_{node.cell}_identity_state_nbt",
                *(
                    f"node_{node.cell}_absent_{path}"
                    for path in generate.ABSENT_DYNAMIC_PATHS
                ),
            )
        )
    failure_ids = re.findall(
        rf" run say {re.escape(generate.FAILURE_LOG_PREFIX)}([^\n ]+)$",
        verify,
        re.MULTILINE,
    )
    if failure_ids != expected_failure_ids:
        fail("verify failure IDs differ from the stable semantic assertion order")
    if len(set(failure_ids)) != EXPECTED_CHECKS:
        fail("every retained assertion must have one unique failure ID")
    if any(FAILURE_ID_PATTERN.fullmatch(value) is None for value in failure_ids):
        fail("failure IDs may contain only ASCII letters, digits, and underscores")
    log_lines = [line for line in verify.splitlines() if " run say " in line]
    failure_lines = [
        line
        for line in verify.splitlines()
        if f"run scoreboard players add #failures {generate.OBJECTIVE} 1" in line
    ]
    if len(log_lines) != EXPECTED_CHECKS or len(failure_lines) != EXPECTED_CHECKS:
        fail("every retained assertion must pair one failure log with one increment")
    all_say_lines = [
        line for line in all_functions.splitlines() if "say " in line.lower()
    ]
    if all_say_lines != log_lines or any(
        not line.startswith(("execute if ", "execute unless "))
        for line in log_lines
    ):
        fail("failure IDs must be the only /say output and log only on failures")
    for index, (log_line, failure_line) in enumerate(
        zip(log_lines, failure_lines, strict=True)
    ):
        log_condition, separator, failure_id = log_line.partition(" run say ")
        failure_condition, separator_increment, increment = failure_line.partition(
            " run scoreboard players add #failures "
        )
        if (
            separator != " run say "
            or separator_increment != " run scoreboard players add #failures "
            or log_condition != failure_condition
            or failure_id
            != generate.FAILURE_LOG_PREFIX + failure_ids[index]
            or increment != f"{generate.OBJECTIVE} 1"
        ):
            fail("failure log and aggregate increment conditions must be identical")
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
        if verify.count(generate.expected_entity_nbt(node)) != 2:
            fail(f"verify lost exact centered identity/state NBT: {node.cell}")
        for path in generate.ABSENT_DYNAMIC_PATHS:
            expected_absence = (
                f"execute if data entity {generate.node_selector(node, single=True)} "
                f"{path}"
            )
            if verify.count(expected_absence) != 2:
                fail(f"verify lost dynamic-field absence check: {node.cell}/{path}")

    expected_load_commands = [
        f"scoreboard objectives add {generate.OBJECTIVE} dummy",
        *(
            f"scoreboard players add {score} {generate.OBJECTIVE} 0"
            for score in generate.STATE_SCORES
        ),
    ]
    if command_lines(load) != expected_load_commands or "forceload" in load:
        fail("load must only initialize the durable lifecycle scores")

    build_guard = (
        f"execute if score #builds {generate.OBJECTIVE} matches 0 if score "
        f"#build_pending {generate.OBJECTIVE} matches 0 if score #clear_pending "
        f"{generate.OBJECTIVE} matches 0 if score #verification_pending "
        f"{generate.OBJECTIVE} matches 0 run function "
        f"{generate.NAMESPACE}:prepare_build"
    )
    if command_lines(build)[-1:] != [build_guard]:
        fail("build must use the complete durable prepare guard")
    if build.count(f"run function {generate.NAMESPACE}:prepare_build") != 1:
        fail("build must invoke prepare_build at most once")
    if "matches 1.. run tellraw @a" not in build:
        fail("build must announce the already-built guard")
    forbidden_wrapper_mutators = (
        "setblock ",
        "summon ",
        "kill ",
        "fill ",
        "forceload ",
        "schedule function ",
        "scoreboard players set ",
    )
    if any(token in build for token in forbidden_wrapper_mutators):
        fail("guarded build wrapper must not mutate or schedule directly")

    expected_prepare_build = [
        f"scoreboard players set #build_pending {generate.OBJECTIVE} 1",
        f"forceload add {generate.FORCELOAD}",
        f"scoreboard players set #ticket_held {generate.OBJECTIVE} 1",
        f"schedule function {generate.NAMESPACE}:build_once 20t replace",
    ]
    if command_lines(prepare_build) != expected_prepare_build:
        fail("prepare_build must guard, forceload, and wait exactly 20 ticks")

    expected_build_once = (
        f"execute if score #build_pending {generate.OBJECTIVE} matches 1 "
        f"if score #builds {generate.OBJECTIVE} matches 0 if score "
        f"#clear_pending {generate.OBJECTIVE} matches 0 run function "
        f"{generate.NAMESPACE}:build_loaded"
    )
    if command_lines(build_once) != [expected_build_once]:
        fail("scheduled build_once must recheck every build lifecycle guard")

    expected_cleanup = [
        "logisticsnetworks removeNodes",
        "fill 160 99 160 191 108 191 minecraft:air",
    ]
    if command_lines(loaded_cleanup) != expected_cleanup:
        fail("loaded_cleanup must contain only exact global and bounded cleanup")
    if (
        "PROTOTYPE ONLY" not in loaded_cleanup
        or "every loaded LogisticsNetworks node dimension-wide"
        not in loaded_cleanup
    ):
        fail("loaded_cleanup must carry the destructive dimension-global warning")
    if "kill " in all_functions:
        fail("ordinary kill is ineffective for LogisticsNetworks nodes")
    if all_functions.count("logisticsnetworks removeNodes") != 1:
        fail("the exact supported global removal command must exist only in cleanup")

    build_loaded_commands = command_lines(build_loaded)
    cleanup_call = f"function {generate.NAMESPACE}:loaded_cleanup"
    increment = f"scoreboard players add #builds {generate.OBJECTIVE} 1"
    if build_loaded_commands[:2] != [cleanup_call, increment]:
        fail("build_loaded must clean loaded chunks before its one build increment")
    if all_functions.count(increment) != 1:
        fail("build counter must increment exactly once in the complete datapack")
    last_summon_index = max(
        index
        for index, line in enumerate(build_loaded_commands)
        if line.startswith("summon ")
    )
    post_summon_commands = [
        f"scoreboard players set #build_pending {generate.OBJECTIVE} 0",
        f"scoreboard players set #verification_pending {generate.OBJECTIVE} 1",
        f"function {generate.NAMESPACE}:verify_immediate",
        f"schedule function {generate.NAMESPACE}:verify_20t 20t replace",
        f"schedule function {generate.NAMESPACE}:verify_100t 100t replace",
    ]
    if build_loaded_commands[last_summon_index + 1 :] != post_summon_commands:
        fail("verification phases must be scheduled relative to the final summon")
    if "forceload" in build_loaded or f"function {generate.NAMESPACE}:clear" in build_loaded:
        fail("build_loaded must use only the state-free loaded cleanup helper")

    expected_clear_guard = (
        f"execute if score #clear_pending {generate.OBJECTIVE} matches 0 run "
        f"function {generate.NAMESPACE}:prepare_clear"
    )
    if command_lines(clear) != [expected_clear_guard]:
        fail("public clear must be a repeat-safe prepare guard")

    expected_prepare_clear = [
        f"schedule clear {generate.NAMESPACE}:build_once",
        f"schedule clear {generate.NAMESPACE}:verify_20t",
        f"schedule clear {generate.NAMESPACE}:verify_100t",
        f"scoreboard players set #build_pending {generate.OBJECTIVE} 0",
        f"scoreboard players set #verification_pending {generate.OBJECTIVE} 0",
        f"scoreboard players set #clear_pending {generate.OBJECTIVE} 1",
        f"forceload add {generate.FORCELOAD}",
        f"scoreboard players set #ticket_held {generate.OBJECTIVE} 1",
        f"schedule function {generate.NAMESPACE}:clear_once 20t replace",
    ]
    if command_lines(prepare_clear) != expected_prepare_clear:
        fail("prepare_clear must cancel, forceload, and wait exactly 20 ticks")

    expected_clear_once = (
        f"execute if score #clear_pending {generate.OBJECTIVE} matches 1 run "
        f"function {generate.NAMESPACE}:clear_loaded"
    )
    if command_lines(clear_once) != [expected_clear_once]:
        fail("scheduled clear_once must recheck its durable pending guard")

    expected_clear_loaded = [
        cleanup_call,
        f"scoreboard players set #builds {generate.OBJECTIVE} 0",
        f"scoreboard players set #build_pending {generate.OBJECTIVE} 0",
        f"scoreboard players set #verification_pending {generate.OBJECTIVE} 0",
        f"scoreboard players set #clear_pending {generate.OBJECTIVE} 0",
        f"forceload remove {generate.FORCELOAD}",
        f"scoreboard players set #ticket_held {generate.OBJECTIVE} 0",
    ]
    if command_lines(clear_loaded) != expected_clear_loaded:
        fail("clear_loaded must clean/reset before removing its bounded ticket")

    phase_complete = (
        f"scoreboard players set #verification_pending {generate.OBJECTIVE} 0"
    )
    if command_lines(verify_100t)[-1:] != [phase_complete]:
        fail("100t verification must durably complete the verification lifecycle")

    release_commands = command_lines(release)
    if release_commands != [generate.release_guard()]:
        fail("release must use the complete fail-closed lifecycle/phase guard")
    for score, value in (
        ("#builds", 1),
        ("#build_pending", 0),
        ("#clear_pending", 0),
        ("#verification_pending", 0),
        ("#ticket_held", 1),
        ("#immediate_checked", EXPECTED_CHECKS),
        ("#immediate_failures", 0),
        ("#20t_checked", EXPECTED_CHECKS),
        ("#20t_failures", 0),
        ("#100t_checked", EXPECTED_CHECKS),
        ("#100t_failures", 0),
    ):
        condition = (
            f"if score {score} {generate.OBJECTIVE} matches {value}"
        )
        if release.count(condition) != 1:
            fail(f"release lost its fail-closed condition: {score}")
    expected_release_once = [
        f"forceload remove {generate.FORCELOAD}",
        f"scoreboard players set #ticket_held {generate.OBJECTIVE} 0",
    ]
    if command_lines(release_once) != expected_release_once:
        fail("release_once must only remove and record the bounded ticket")
    if any(token in release for token in ("kill ", "fill ", "forceload ", "schedule ")):
        fail("release wrapper must not mutate or cancel lifecycle work directly")

    if len(
        re.findall(
            rf"^forceload add {generate.FORCELOAD}$",
            all_functions,
            re.MULTILINE,
        )
    ) != 2:
        fail("only build and clear preparation may acquire the four-chunk ticket")
    if len(
        re.findall(
            rf"^forceload remove {generate.FORCELOAD}$",
            all_functions,
            re.MULTILINE,
        )
    ) != 2:
        fail("only loaded clear and verified release may remove the ticket")
    if all_functions.count(cleanup_call) != 2:
        fail("loaded cleanup must be shared only by build and clear")

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
    normalized_readme = " ".join(readme.split())
    if "not a natural saved-fixture release proof" not in normalized_readme:
        fail("README must retain the natural-fixture release blocker")
    if (
        "d94395da601ce93d8d7c9ffc434a018f6f46488303c654f6d6d5747961f56187"
        not in readme
    ):
        fail("README must pin the exact LogisticsNetworks artifact")
    for warning in (
        "Destructive prototype-only operation",
        "exact supported `/logisticsnetworks removeNodes` command",
        "every currently loaded LogisticsNetworks node anywhere in "
        "the command's current dimension",
        "it is dimension-global, not gallery-bounded",
        "Never install or invoke this datapack in production",
    ):
        if warning not in normalized_readme:
            fail("README must document dimension-global destructive cleanup")
    for lifecycle_contract in (
        "durable, two-stage build-once guard",
        "exact four-chunk x/z `160..191` forceload",
        "conservative 20-tick loading window",
        "Repeated `build` calls",
        "`clear` is also a guarded two-stage lifecycle",
        "Repeated `clear` calls",
        "`release` fails closed",
        "all three retained phase snapshots",
        "It never cancels pending checks",
    ):
        if lifecycle_contract not in normalized_readme:
            fail("README must document the guarded two-stage lifecycle")

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
