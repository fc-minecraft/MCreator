<#if data.textureBucket?has_content>
{
  "parent": "item/generated",
  "textures": {
    "layer0": "${data.textureBucket.formatWithCategory("%s:%s/%s", "item")}"
  }
}
<#else>
<#-- ... rest of file ... -->
{
  "parent": "neoforge:items/fluid_container",
  "fluid": "${modid}:${registryname}"
}
</#if>