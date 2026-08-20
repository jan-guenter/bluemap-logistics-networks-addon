# Release gate

This repository is currently a local prototype. Release is blocked on a
natural exact-version entity fixture, matching modded-client comparison,
disposable BlueMap staging, owner visual acceptance, and the ordinary
independent release audit.

The minimum local candidate gate is:

```bash
gradle --no-daemon \
  -PlogisticsNetworksJar=/absolute/path/logisticsnetworks-1.21.1-1.10.1.jar \
  clean check build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication
```

Before any later release, inspect the production/sources JARs, POM and Gradle
module; confirm exact artifact detection in hosted CI; prove that no upstream
asset or class is packaged; and compare the exact candidate in the disposable
Minecraft/BlueMap lab. Publication never authorizes production deployment.
