{
  "structures": [
    {
      "structure": "${modid}:${registryname}",
      "weight": 1
    }
  ],
  "placement": {
    "type": "minecraft:random_spread",
    "spacing": ${data.spacing},
    "separation": ${data.separation},
    "spread_type": "${data.spreadType!"linear"}",
    <#if (data.frequency!1.0) != 1.0>"frequency": ${data.frequency},</#if>
    "salt": ${thelper.randompositiveint(registryname)}
  }
}