package com.example.zeromanagement.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date

@Serializable
data class Subtask(
    val id: String,
    val title: String,
    var isCompleted: Boolean = false
)

@Entity
@Serializable
@TypeConverters(Converters::class)
data class Task(
    @PrimaryKey val id: String,
    val title: String,
    @Serializable(with = DateSerializer::class)
    val fromDate: Date,
    @Serializable(with = DateSerializer::class)
    val dueDate: Date,
    val description: String = "",
    val references: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val imageUris: List<String> = emptyList(),
    val subtasks: List<Subtask> = emptyList(),
    @Serializable(with = DateSerializer::class)
    val reminderDate: Date? = null,
    val isPinned: Boolean = false
)

enum class Priority {
    LOW, MEDIUM, HIGH
}

object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeLong(value.time)
    override fun deserialize(decoder: Decoder): Date = Date(decoder.decodeLong())
}

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val STRING_LIST_SEPARATOR = "<!!LIST_SEPARATOR!!>"
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        return value?.split(STRING_LIST_SEPARATOR)?.filter { it.isNotEmpty() } ?: emptyList()
    }

    @TypeConverter
    fun toStringList(value: List<String>?): String {
        return value?.joinToString(STRING_LIST_SEPARATOR) ?: ""
    }

    @TypeConverter
    fun fromSubtaskList(value: String?): List<Subtask> {
        return if (value.isNullOrEmpty()) emptyList() else json.decodeFromString(ListSerializer(Subtask.serializer()), value)
    }

    @TypeConverter
    fun toSubtaskList(value: List<Subtask>?): String {
        return if (value.isNullOrEmpty()) "" else json.encodeToString(ListSerializer(Subtask.serializer()), value)
    }
}
