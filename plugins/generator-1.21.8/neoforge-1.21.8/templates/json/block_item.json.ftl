<#-- @formatter:off -->
<#if data.itemTexture?has_content>
{
  "parent": "item/generated",
  "textures": {
    "layer0": "${data.itemTexture.formatWithCategory("%s:%s/%s", "item")}"
  },
  "render_type": "translucent"
}
<#else>
<#-- ... rest of file ... -->
{
    "parent": "${modid}:block/${registryname}",
    "display": {
      "thirdperson": {
        "rotation": [
          10,
          -45,
          170
        ],
        "translation": [
          0,
          1.5,
          -2.75
        ],
        "scale": [
          0.375,
          0.375,
          0.375
        ]
      }
    }
}
</#if>
<#-- @formatter:on -->