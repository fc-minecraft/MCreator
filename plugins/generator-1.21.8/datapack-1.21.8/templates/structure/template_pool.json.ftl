<#include "../mcitems.ftl">
<#assign poolParts = (item.poolParts)!data.getPoolParts()>
<#assign poolName = item???then(registryname + "_" + (item.poolName)!"", registryname)>
<#assign fallback = (item.fallbackPool)! "minecraft:empty">
<#if !fallback?has_content><#assign fallback = "minecraft:empty"></#if>
{
  "name": "${modid}:${poolName}",
  "fallback": "${fallback}",
  "elements": [
    <#list poolParts as part>
    {
      "weight": ${part.weight?c},
      "element": {
        "element_type": "minecraft:single_pool_element",
        "location": "${modid}:${part.structure}",
        "projection": "${part.projection}",
        "processors": [
            <#if (part.ignoredBlocks?has_content)>
            {
              "processor_type": "minecraft:block_ignore",
              "blocks": [
                <#list part.ignoredBlocks as block>
                {
                  "Name": "${mappedMCItemToRegistryName(block)}"
                }<#sep>,
                </#list>
              ]
            }
            </#if>
            <#if (part.chestLootTables?has_content)>
            <#if (part.ignoredBlocks?has_content)>,</#if>
            {
              "processor_type": "minecraft:rule",
              "rules": [
                <#list ["minecraft:chest", "minecraft:trapped_chest", "minecraft:barrel"] as blockType>
                <#list part.chestLootTables as lootTable>
                {
                  "input_predicate": {
                    "block": "${blockType}",
                    "probability": ${(1.0 / (part.chestLootTables?size - lootTable?index))?string("0.0#")},
                    "predicate_type": "minecraft:random_block_match"
                  },
                  "location_predicate": {
                    "predicate_type": "minecraft:always_true"
                  },
                  "output_state": {
                    "Name": "${blockType}"
                  },
                  <#assign me = w.getWorkspace().getModElementByName(lootTable.value.replace("CUSTOM:", ""))!>
                  <#if me?has_content>
                  <#assign lpath = me.getGeneratableElement().getResourceLocation()>
                  <#else>
                  <#assign lpath = lootTable.value>
                  </#if>
                  "block_entity_modifier": {
                    "type": "minecraft:append_loot",
                    "loot_table": "${lpath}"
                  }
                }<#if blockType?has_next || lootTable?has_next>,</#if>
                </#list>
                </#list>
              ]
            }
            </#if>
          ]
      }
    }<#sep>,
    </#list>
  ]
}