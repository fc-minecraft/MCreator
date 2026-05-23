{
  "parent": "item/generated",
  "textures": {
    <#if data.hasSpawnEgg>
    "layer0": "${modid}:item/${registryname}_spawn_egg_generated"
    <#else>
    "layer0": "${modid}:item/${data.itemTexture}"
    </#if>
  }
}
