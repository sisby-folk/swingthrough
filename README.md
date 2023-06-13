<center>Allows targeting living entities through transparent blocks.
</center>

---

<center><b>Packs:</b> <a href="https://modrinth.com/modpack/tinkerers-quilt">Tinkerer's Quilt</a> (<a href="https://modrinth.com/modpack/tinkerers-silk">Silk</a>) - <a href="https://modrinth.com/modpack/switchy-pack">Switchy Pack</a></center>
<center><b>Mods:</b> <a href="https://modrinth.com/mod/switchy">Switchy</a> - <a href="https://modrinth.com/mod/origins-minus">Origins Minus</a> (<a href="https://modrinth.com/mod/tinkerers-statures">Statures</a>) - <a href="https://modrinth.com/mod/tinkerers-smithing">Tinkerer's Smithing</a></center>

---

This mod discards the block crosshair target to allow an entity to be targeted instead, when:
   - The block has empty collision
   - The entity is living and attackable
   - You're not directly riding the entity
   - The entity isn't your own tamed entity
   - The entity is within reach (*slightly* longer than actual attack range, but still usable)

Due to the way this is implemented, the block outline will also visually disappear when you're targeting the entity instead.


#### Technical Note

This mod is comprised of a [single mixin](https://github.com/sisby-folk/swingthrough/blob/1.19/src/main/java/folk/sisby/swingthrough/mixin/client/GameRendererMixin.java) containing 4 lines of meaningful code **total**. Including:

 - 2 Lines of code to make entity targeting always use the full reach instead of shortening when a block target is found. 
 - 2 Lines of code to discard the block target if the block and entity meet the conditions

It performs no additional raycasts or radius searches.

## Afterword

All mods are built on the work of many others.

We made this mod for [Tinkerer's Quilt](https://modrinth.com/modpack/tinkerers-quilt) - our modpack about ease of play and vanilla content.
