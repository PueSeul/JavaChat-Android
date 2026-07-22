# ViaVersion Android compatibility module

This module repackages ViaVersion `5.11.1-SNAPSHOT` from upstream commit
`432918645ed8d419e160bc35d10a027649a7d37e` and replaces three classes that call
desktop-only Java runtime APIs:

- `TagType` always uses ViaVersion's existing `FastByteBufInputStream` path.
- `HashFunction` always uses ViaVersion's existing table-based CRC32C fallback.
- `ViaManagerImpl` uses the app's fixed Java 17/desugared runtime level for its
  optional compatibility warning.

The original jar is retained in `libs` so the Android-compatible jar is
reproducible by Gradle. Corresponding upstream and modified source is included
under `third_party/ViaVersion` in source distributions. License: GNU GPL v3.
