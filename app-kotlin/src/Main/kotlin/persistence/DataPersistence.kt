package persistence

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import domain.*
import java.io.File
import java.time.LocalDateTime

class JsonDataPersistence(private val dataDirectory: String = "data") {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .setPrettyPrinting()
        .create()

    init {
        File(dataDirectory).mkdirs()
    }

    fun saveEvents(events: List<Event>): Result<Unit> {
        return try {
            val json = gson.toJson(events)
            File("$dataDirectory/events.json").writeText(json)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loadEvents(): Result<List<Event>> {
        return try {
            val file = File("$dataDirectory/events.json")
            if (!file.exists()) return Result.success(emptyList())

            val json = file.readText()
            val type = object : TypeToken<List<Event>>() {}.type
            val events: List<Event> = gson.fromJson(json, type)
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveVenues(venues: List<Venue>): Result<Unit> {
        return try {
            val json = gson.toJson(venues)
            File("$dataDirectory/venues.json").writeText(json)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loadVenues(): Result<List<Venue>> {
        return try {
            val file = File("$dataDirectory/venues.json")
            if (!file.exists()) return Result.success(emptyList())

            val json = file.readText()
            val type = object : TypeToken<List<Venue>>() {}.type
            val venues: List<Venue> = gson.fromJson(json, type)
            Result.success(venues)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class LocalDateTimeAdapter : com.google.gson.JsonSerializer<LocalDateTime>,
    com.google.gson.JsonDeserializer<LocalDateTime> {

    override fun serialize(src: LocalDateTime?, typeOfSrc: java.lang.reflect.Type?,
                           context: com.google.gson.JsonSerializationContext?): com.google.gson.JsonElement {
        return com.google.gson.JsonPrimitive(src.toString())
    }

    override fun deserialize(json: com.google.gson.JsonElement?, typeOfT: java.lang.reflect.Type?,
                             context: com.google.gson.JsonDeserializationContext?): LocalDateTime {
        return LocalDateTime.parse(json?.asString)
    }
}
