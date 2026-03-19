{
  "parent": "item/generated",
  "textures": {
    <#if data.spawnEggTexture?has_content>
	"layer0": "${data.spawnEggTexture.formatWithCategory("%s:%s/%s", "item")}"
    <#else>
	"layer0": "${modid}:item/${registryname}_spawn_egg_generated"
    </#if>
  }
}