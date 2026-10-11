# OBAMCraft WTF Survival

WTF Survival beta implementation, tracked in [Guidance47](https://github.com/KribKraf/Server-Guidance/issues/47). The beta is not open yet. Source candidates must pass isolated native tests before Operations exposes `/server wtf`.

The first foundation provides fixed80x80 spawn geometry, circular512-block no-claim reserve geometry and infinite seven-block-wide protected cardinal road corridors. It does not create highway terrain in new chunks. Default-off Paper26.2/Java25 plugin source adds spawn/road block protection and a console-confirmed disposable-world bootstrap: flat marked grass spawn, hollow glass/glowstone sphere and eight protected decorative Allays. The Allays orbit the sphere; they are not Fairy Essence rewards. Foundation status clearly says energy, claims and Trust are unavailable.

## Build

Use Java25 and the checked-in Gradle wrapper: `./gradlew test jar` (Windows: `gradlew.bat test jar`). Runtime API is pinned to the same Paper26.2 build121 as the existing network components; dependencies are locked. Four geometry regressions cover exact80-square bounds, overflow-safe road checks, full-chunk reserve exclusion and radial symmetry. These are not native permission/entity acceptance.

## Safe stage boundary

`enabled:false` and `allow-disposable-bootstrap:false` are the shipped defaults. No world changes on disabled startup. Never install enabled on existing SkyBlock, Builds, challenge-test or another production world. Operations must prepare a fresh separate WTF world/backend and verify its identity/admission first.

The console-only command `wtf beta prepare` prints the exact world UUID. `wtf beta prepare confirm <UUID>` works only for the existing world named `wtf_beta` with both flags explicitly enabled. It writes only spawn X/Z-40..39 and Yfloor..floor+11, pacing writes to256 per tick and a3ms sampling cutoff; chunk loading/world save can still exceed that cutoff and require measured native acceptance. Sphere and eight decorations finish afterward. A prepared marker prevents automatic rerun. There is no reset/delete command. Stop/snapshot before any disposable preparation; restore the disposable backup if preparation is interrupted. Do not use it as a general terrain editor.

Before a public preview: prove startup default-off, native listener registration, exact boundary/bucket/flow/piston/explosion denial, eight unique protected/noninteractable Allays, saved reload behavior, no duplicate preparation, bounded chunk/load/tick behavior and unchanged other backends. The source tests do not cover those gates. No server, proxy route, whitelist, world or player balance has changed from this repository creation.

## Remaining beta

See [BETA-CHECKLIST.md](BETA-CHECKLIST.md). [Trust45](https://github.com/KribKraf/Server-Guidance/issues/45) is shared authority but independent game-mode data. All energy/claim transactions must be durable, idempotent and authoritative before any playable beta. Do not simulate currencies with temporary scoreboard balances or bypass claims to advertise completion. A visible scoreboard may present authoritative energy; it must not be its only ledger.
