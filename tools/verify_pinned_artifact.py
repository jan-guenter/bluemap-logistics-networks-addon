#!/usr/bin/env python3
"""Verify the one exact operator-installed LogisticsNetworks artifact."""

from __future__ import annotations

import argparse
import hashlib
from pathlib import Path
import sys
import tomllib
import zipfile

EXPECTED_SIZE = 988_995
EXPECTED_SHA256 = "d94395da601ce93d8d7c9ffc434a018f6f46488303c654f6d6d5747961f56187"
TEXTURE = "assets/logisticsnetworks/textures/entity/node.png"
TEXTURE_SIZE = 4_291
TEXTURE_SHA256 = "03194c53acc840f953e44838e1086a350f27036a0fcb44165bb5a1786ae7885e"
DESCRIPTOR = "META-INF/neoforge.mods.toml"
UNEXPECTED_ENTITY_STATE = "assets/logisticsnetworks/entitystates/logistics_node.json"


def digest(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def verify(path: Path) -> None:
    if not path.is_file():
        raise ValueError(f"artifact is not a regular file: {path}")
    data = path.read_bytes()
    if len(data) != EXPECTED_SIZE or digest(data) != EXPECTED_SHA256:
        raise ValueError("artifact size/SHA-256 does not match the ATM 1.2.0 pin")

    with zipfile.ZipFile(path) as archive:
        names = set(archive.namelist())
        if DESCRIPTOR not in names or TEXTURE not in names:
            raise ValueError("artifact is missing required metadata or node texture")
        if UNEXPECTED_ENTITY_STATE in names:
            raise ValueError("unexpected entity-state route would duplicate the custom pass")

        metadata = tomllib.loads(archive.read(DESCRIPTOR).decode("utf-8"))
        mods = metadata.get("mods", [])
        matched = [entry for entry in mods if entry.get("modId") == "logisticsnetworks"]
        if len(matched) != 1 or matched[0].get("version") != "1.10.1":
            raise ValueError("mod ID/version metadata mismatch")
        if metadata.get("license") != "All Rights Reserved":
            raise ValueError("license metadata mismatch")

        texture = archive.read(TEXTURE)
        if len(texture) != TEXTURE_SIZE or digest(texture) != TEXTURE_SHA256:
            raise ValueError("operator node texture identity mismatch")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", required=True, type=Path)
    args = parser.parse_args()
    try:
        verify(args.jar)
    except (OSError, ValueError, zipfile.BadZipFile, tomllib.TOMLDecodeError) as error:
        print(f"pinned artifact verification failed: {error}", file=sys.stderr)
        return 1
    print(f"verified LogisticsNetworks 1.10.1: {EXPECTED_SHA256}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
