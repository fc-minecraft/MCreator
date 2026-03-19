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
  "parent": "${modid}:block/${registryname}"
}
</#if>
<#-- @formatter:on -->