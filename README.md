# Raft Starter (Forge 1.20.1)

This project is a Forge mod for **Minecraft 1.20.1** that creates a raft-style start:

- On first join, the player is moved to a starter raft made from **4 oak planks**.
- The spawn area is converted into a broad, deep water zone.
- Floating **oak plank items** are spawned to the **north** and drift south toward the raft.

## Build the mod JAR

From the project root:

```bash
gradle build
```

Expected output jar:

- `build/libs/raftstarter-1.0.0.jar`

## Install into CurseForge profile

1. Open your custom Forge **1.20.1** profile in CurseForge.
2. Click the profile menu and open the **mods folder**.
3. Copy `raftstarter-1.0.0.jar` into that mods folder.
4. Launch Minecraft.

## Notes

- The spawn setup runs once per player (first login) in the Overworld.
- This is v1 with only plank drift. We can add barrels, leaves, plastic-like custom items, hooks, and progression in the next pass.
