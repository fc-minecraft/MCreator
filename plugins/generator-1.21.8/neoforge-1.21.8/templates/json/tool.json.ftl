{
    "parent": "item/handheld",
    "textures": {
        <#if var_item??>
            "layer0": "${data.getItemTextureFor(var_item).formatWithCategory("%s:%s/%s", "item")}"
        <#else>
            "layer0": "${data.getTexture().formatWithCategory("%s:%s/%s", "item")}"
        </#if>
    }
}