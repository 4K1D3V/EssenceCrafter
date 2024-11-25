### EssenceCrafter
EssenceCrafter is a Minecraft plugin that allows players to craft special "Essence Items" by interacting with custom entities. Admins can configure the crafting recipes, item abilities, and entity types using a configuration file (config.yml). Players can then use commands to manage the available Essence recipes, and obtain crafted Essence Items with special effects.

### Features
**Custom Commands and Arguments:**

`/essence create <name> <entity>`: Create a new Essence recipe tied to a specific entity type.
`/essence list: List all available` Essence recipes.
`/essence delete <name>`: Remove an Essence recipe.
`/essence give <name> [player]`: Give a player a crafted Essence Item.

**Entities and Interactions:**
Players can interact with specific entities (e.g., Villager, Enderman) to craft Essence Items.
Entity types and interactions are configurable in the config.yml file.

**Custom Items:**
Essence Items have custom names, lore, and enchantments, all of which are defined in the configuration.
Each Essence Item is unique, with metadata distinguishing it from regular items.

**Installation:**
Download the latest version of EssenceCrafter.
Place the plugin file (EssenceCrafter.jar) into the plugins folder of your Minecraft server.
Restart or reload the server.

**Configuration (config.yml)**
The plugin reads from the config.yml to fetch recipe requirements, item attributes, and associated entity types.
Here's an example configuration:
```
essences:
  healing_essence:
    entity: "VILLAGER"
    item:
      name: "&aHealing Essence"
      lore:
        - "&7Restores health when used"
      enchantments:
        - "DURABILITY:1"
    recipe:
      - "DIAMOND:2"
      - "GOLD_INGOT:4"
  strength_essence:
    entity: "IRON_GOLEM"
    item:
      name: "&cStrength Essence"
      lore:
        - "&7Increases damage for 2 minutes"
      enchantments:
        - "SHARPNESS:3"
    recipe:
      - "IRON_BLOCK:3"
      - "BLAZE_POWDER:5"
```
essences: Contains all Essence recipes.
entity: The type of entity that the player interacts with to craft the Essence Item (e.g., VILLAGER, IRON_GOLEM).
item: Defines the properties of the crafted Essence Item:
name: The display name of the item (supports color codes).
lore: A list of lore for the item.
enchantments: A list of enchantments to apply to the item.
recipe: The required items and quantities for crafting the Essence Item.

**Commands**
`/essence create <name> <entity>`: Creates a new Essence recipe.
`/essence list`: Lists all available Essence recipes.
`/essence delete <name>`: Deletes an existing Essence recipe.
`/essence give <name> [player]`: Gives a player a crafted Essence Item.
### How It Works

**Crafting Essence Items:**
When a player interacts with a specified entity (e.g., a Villager or an Iron Golem), the plugin checks if the player has the required items in their inventory.
If the player has the correct items, the plugin removes them and crafts the corresponding Essence Item.

**Customizable Recipes:**
Admins can easily configure and add new Essence recipes by editing the config.yml file.

**Contributor:**
 - Aki (Kit)
 - 
**License:**
This plugin is open-source under the MIT License.
