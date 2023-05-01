# TownyColonies

Towny addon that adds automated farms, factories and administrative buildings into towny town & nation system

The plugin forces players to build houses from predefine block palletes

**Alphabuilds are not suitable for production env.**

For any questions ping NeumimTo at the towny discord

## Requirements ##

Tested on:

- folia 1.19.4 (might work on 1.17, wont work with anything below 1.16)
- towny 0.98.1.0 (might or might not work with older versions)

## Building from source

- `gradlew shadowJar`
- The jar is then located in path `build/libs/townycolonies-{version}-all.jar`

## Installation

- drop the jar into plugins folder
- default configs might not be balanced to suit yours server economy
- Towny must use mysql database
- Append sql database connection flags by `&allowMultiQueries=true`

## Gameplay

- Permission: `townycolonies.administrative`
  - Buys new blueprints

- Permission: `townycolonies.architect`
  - Chooses blueprint location
  - Can toggle edit mode for structure

- Players
  - Build structure according to the blueprint block requirements
  - Once the build is finish;ed they need to call the architect to toggle edit mode