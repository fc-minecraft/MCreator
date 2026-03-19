{
  "parent": "item/generated",
  "textures": {
    <#if data.itemTexture?has_content>
    "layer0": "${data.itemTexture.formatWithCategory("%s:%s/%s", "item")}"
    <#else>
    "layer0": "${data.getTexture().formatWithCategory("%s:%s/%s", "block")}"
    </#if>
  }
}