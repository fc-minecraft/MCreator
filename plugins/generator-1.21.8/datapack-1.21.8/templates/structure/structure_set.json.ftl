{
  "structures": [
    {
      "structure": "${modid}:${registryname}",
      "weight": 1
    }
  ],
  "placement": {
    "type": "minecraft:random_spread",
    "spacing": ${data.spacing?c},
    "separation": ${data.separation?c},
    "spread_type": "${data.spreadType!"linear"}",
    <#if (data.frequency!1.0) != 1.0>
    "frequency": ${data.frequency?c},
    "frequency_reduction_method": "${data.frequencyReductionMethod!"default"}",
    </#if>
    "salt": ${((data.salt != -1)?then(data.salt, thelper.randompositiveint(registryname)))?c}
  }
}