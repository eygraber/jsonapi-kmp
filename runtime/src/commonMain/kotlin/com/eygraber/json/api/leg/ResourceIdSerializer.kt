package com.eygraber.json.api.leg

// internal object ResourceIdSerializer : KSerializer<ResourceId> {
//   override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ResourceId", PrimitiveKind.STRING)
//
//   override fun serialize(encoder: Encoder, value: ResourceId) {
//     require(encoder is JsonEncoder)
//     val jsonObject = if(value.isLocal) {
//       buildJsonObject {
//         put("lid", value.id.removePrefix("lid:"))
//       }
//     } else {
//       buildJsonObject {
//         put("id", value.id)
//       }
//     }
//     encoder.encodeJsonElement(jsonObject)
//   }
//
//   override fun deserialize(decoder: Decoder): ResourceId {
//     val jsonDecoder = decoder as? JsonDecoder ?: throw IllegalStateException("Only JsonDecoder is supported")
//     val jsonObject = jsonDecoder.decodeJsonElement().jsonObject
//     val id = jsonObject["id"]?.jsonPrimitive?.content
//     return when(val lid = jsonObject["lid"]?.jsonPrimitive?.content) {
//       null -> when(id) {
//         null -> ResourceId.NoId
//         else -> ResourceId(id)
//       }
//
//       else -> ResourceId("lid:$lid")
//     }
//   }
// }
