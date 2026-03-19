{
  "forge_marker": 1,
  "parent": "neoforge:item/default",
  "loader": "neoforge:obj",
<#if var_item??> <#-- used by armor where item type is specified (helmet, body, ...) -->
  "model": "${modid}:models/item/${data.getItemCustomModelNameFor(var_item)}.obj",
  "textures": {
    <@textures data.getItemModelTextureMap(var_item)/>
    "particle": "${data.getItemTextureFor(var_item).formatWithCategory("%s:%s/%s", "item")}"
<#else>
  "model": "${modid}:models/item/${data.customModelName.split(":")[0]}.obj",
  "textures": {
    <@textures data.getTextureMap()/>
    "particle": "${data.getTexture().formatWithCategory("%s:%s/%s", "item")}"
</#if>
  }
}

<#macro textures textureMap>
    <#if textureMap??>
        <#list textureMap.entrySet() as texture>
            "${texture.getKey()}": "${texture.getValue().formatWithCategory("%s:%s/%s", "block")}",
        </#list>
    </#if>
</#macro>