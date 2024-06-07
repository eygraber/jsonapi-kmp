package com.eygraber.json.api.leg

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

// public object JsonApiResourceSerializer : KSerializer<JsonApiResource> {
//   override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ResourceId", PrimitiveKind.STRING)
//
//   override fun serialize(encoder: Encoder, value: JsonApiResource) {
//     require(encoder is JsonEncoder)
//
//     val json = encoder.json
//     val attributes = json.encodeToJsonElement(value).jsonObject

    //    val relationships = buildJsonObject {
    //      attributes.entries.forEach { entry ->
    //        val hasId = entry.value.jsonObject.containsKey("id") || entry.value.jsonObject.containsKey("lid")
    //        if (hasId && ) {
    //          put(entry.key, buildJsonObject {
    //            put("data", buildJsonObject {
    //              put("id", entry.value.jsonObject["id"]!!.jsonPrimitive.content)
    //              put("type", entry.value.jsonObject["type"]!!.jsonPrimitive.content)
    //            })
    //          })
    //        }
    //      }
    //    }

//     val resource = Resource(
//       id = value.id.id,
//       type = value.jsonApiType(),
//       attributes = json.decodeFromJsonElement(ApiJsonSerializer, attributes),
//     )
//
//     val document = JsonApiObject(
//       type = JsonApiResponseType.Data(DataType.Single(resource))
//     )
//
//     val jsonElement = json.encodeToJsonElement(JsonApiObject.serializer(), document).jsonObject.toMutableMap()
//     if(value.id.isLocal) {
//       val data = jsonElement["data"]?.jsonObject?.toMutableMap()
//       data?.remove("id")
//       data?.put("lid", JsonPrimitive(value.id.id.removePrefix("lid:")))
//       jsonElement["data"] = json.parseToJsonElement(data.toString())
//     }
//
//     encoder.encodeJsonElement(json.parseToJsonElement(jsonElement.toString()))
//   }
//
//   override fun deserialize(decoder: Decoder): JsonApiResource {
//     error("Deserialization is not supported")
//   }
// }
