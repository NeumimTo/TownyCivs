# TownyColonies

Towny addon that adds automated farms, factories and administrative buildings into towny town & nation system

The plugin has been inspired by the mod minecolonies (hence its name) and plugin civs

Basic gameplay:

 - Players buy structure blueprint from the ingame shop
 - Each blueprint has its own building requirements (specific block palette, required region size, biome placement, ....)
 - A town mayor/an assistant choose location for the structure within town claim
 - Residents proceeds to build the structure 
 - If minimal building requirements are met the structure becomes active (item rewards, permission node for town residents, ... )

The main idea is to encourage and reward players to build actual buildings instead of making one large cobblestone house in the middle of their town.

Everything is configurable - you can (and you should) create your own blueprints
For documentation check default config of cactus_farm.conf its all explained in comments

 - Region processing is done in an asychronous thread
 - Unlike civs TownyColonies wont load any addition chunks when distributing region production

**Alphabuilds are not suitable for production env.**

For any questions ping NeumimTo at the towny discord

## Requirements ##

Tested on:

- folia & paper 1.19.4  (might work on 1.17/1.18, wont work with anything below 1.16)
- towny 0.98.1.0 (might or might not work with older versions)

## Building from source

- `gradlew shadowJar`
- The jar is then located in path `build/libs/townycolonies-{version}-all.jar`

## Installation

- drop the jar into plugins folder
- default configs might not be balanced to suit yours server economy

- ~~If using towny SQL storage append sql database connection flags by `&allowMultiQueries=true`~~

## Commands

- Theres only one command - `/toco` - an entrypoint for opening an inventory menu 

## Permissions

- Permission: `townycolonies.administrative`
  - Ability to buy new blueprints

- Permission: `townycolonies.architect`
  - Ability to place blueprint

Both permission should be given to town co/mayor/assistant