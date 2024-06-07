package com.eygraber.json.api.leg

import com.eygraber.json.api.ResourceId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
