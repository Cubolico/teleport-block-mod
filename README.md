## Readme
[README: Italiano](./README_IT.md)

[README: English](./README.md)


# TeleportBlock Mod

## Overview
The TeleportBlock Mod is a Fabric Minecraft mod that allows administrators to create teleport links between two blocks using a honeycomb item. Players can be teleported by stepping on one of the linked blocks. Only administrators have permission to create and remove teleport links.

## Features
- Set teleport links between two blocks
- Only administrators can create and remove teleport links
- Players will be teleported when stepping on a linked block
- Supports permissions and configurable language strings

## Installation
1. Place the mod JAR file in the `mods` folder of your Minecraft installation directory.
2. Run the game and the mod will automatically generate the necessary configuration files.

## Commands

### `/tport set1 <name>`
- Use this command after selecting the first block with the honeycomb to set the name of the first block (Block A).

### `/tport set2 <name>`
- Use this command after selecting the second block with the honeycomb to set the name of the second block (Block B) and complete the teleport link between Block A and Block B.

### `/tport cancel`
- Cancel the current teleport setup.

### `/tport reload`
- Reload the teleport links and language configuration files.

### `/tport usage`
- Display help information for using the mod, including steps to set up teleport links.

## Usage

![usage](https://raw.githubusercontent.com/Cubolico/teleport-block-mod/refs/heads/main/gif-example/usage.gif)

1. Select the first block (Block A) by right-clicking on it with the honeycomb.
2. Run the command `/tport set1 <name>` to set the name for Block A.
3. Select the second block (Block B) by right-clicking on it with the honeycomb.
4. Run the command `/tport set2 <name>` to link Block A and Block B.
5. Once linked, players stepping on one block will be teleported to the other.
6. Destroying either of the two blocks will remove the link.

## Configuration
- The mod automatically generates a configuration directory at `config/teleportblock` with two important files:
  - `teleport_links.json`: Contains the data for teleport links.
  - `language.txt`: Contains customizable language strings for the mod's messages.

## Permissions
- Only players with admin-level permissions (level 4) can create and remove teleport links.
- Non-admin players are prevented from breaking teleport-linked blocks.

## Language Customization
- You can customize the mod's messages by editing the `language.txt` file. Each line corresponds to a specific message in the mod, and you can modify these to suit your server's language or style.


