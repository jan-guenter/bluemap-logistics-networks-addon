# Third-party boundary

| Project | Exact identity | License / lane | Packaged |
| --- | --- | --- | --- |
| BlueMap | `5.22-agent.backport-5.22-mc1.21.1-2`, commit `9be321df995a1103808621d529eb72773e719d4d` | MIT, compile-only API | No |
| LogisticsNetworks | `logisticsnetworks-1.21.1-1.10.1.jar`, SHA-256 `d94395da601ce93d8d7c9ffc434a018f6f46488303c654f6d6d5747961f56187` | All Rights Reserved, exact runtime interoperability evidence only | No |
| BlueMap Pipez Add-on | `v0.1.0-alpha.1`, commit `fa3e773a7d1b7e9af52277bf104e70f704b0bb2a` | MIT, owner-authored exact-gate/adapter patterns | Source patterns adapted |
| BlueMap Integrated Dynamics Add-on | `v0.1.0-alpha.1`, commit `bbc8f5022499bc294754c38163099d21d7b2db3b` | MIT, owner-authored primitive-emission pattern | Source pattern adapted |

The add-on asks BlueMap to load the operator-installed
`logisticsnetworks:entity/node` texture. The texture is never copied into this
repository or production JAR.
