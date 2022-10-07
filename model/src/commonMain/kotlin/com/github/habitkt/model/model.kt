package com.github.habitkt.model

import kotlinx.datetime.LocalDate

/*
TODO:
 - klären mit Backend-Team ob und ggf. wie Referenzen in diesem Datenmodell hinterlegt werden müssen.
 */

/**
 * TODO:
 *  - Brauchen wir ein frei definierbares Kürzel für die Anzeige (z.B. im Kopf des Dashboards)?
 *  - Reicht ein abgeleitetes Kürzel aus dem Screennamen?
 */
data class UserData(
    /**
     * Unique identity in order to distinguish different habits.
     */
    val id: String,

    val screenName: String,

    /**
     * TODO: Klären, welcher Datentyp sinnvoll ist!
     *   - URL?
     *   - Bitmap?
     *   - Enum?
     * TODO: Laut UI-Team erst in Schritt 2 -> also fällt das weg?
     */
    val avatar: String
)

data class Abbreviation(val value: String) {
    init {
        require(value.length <= 2) { "At maximum two character are allowed. Got ${value.length} characters." }
    }
}

/**
 * Defines a conceptual color for the visual representation.
 *
 * The UI can later map those values to actual styling information (like background- or text-colors, border, etc)
 *
 * TODO: Muss noch finalisiert werden
 */
enum class HabitColor {
    Red,
    Blue,
    Green,
    Cyan,
    // etc.
}

/**
 * The core domain type, that represents one habit.
 */
data class Habit(
    /**
     * Unique identity in order to distinguish different habits.
     */
    val id: String,

    /**
     * TODO: Ableiten, ob eigens oder fremdes habit -> ``Boolean``
     */
    val owner: UserData,

    /**
     * In case that this is foreign habit, this list must remain empty.
     */
    val sharedWith: List<UserData>,

    val title: String = "",

    /**
     * TODO: Anstelle eines Icons haben wir basierend auf den UI-Skizzen die Abbreviation gewählt.
     */
    val abbreviation: Abbreviation = Abbreviation(""),

    val description: String = "",
    val color: HabitColor,

    /**
     * TODO: Ursprünglich: ``priority``: "for a start we use the amount of tracked activities as priority"
     */
    val weeklyTargetValue: Int,

    /**
     * Defines the order for all visual appearances.
     * Higher values have more importance and should appear at the "best" position.
     */
    val order: Int,

    /**
     * This flag is useful for partitioning all habits in two disjunctive sets:
     * - all active (``true``) habits can be tracked and are shown in the dashboard.
     * - all inactive (``false``) habits cannot be tracked and are not shown in the dashboard.
     *
     * A foreign habit should be initialized with ``false`` in order to protect a user from being "habit bombed" by
     * others.
     */
    val active: Boolean
) {
    /**
     * Convenience function to check, whether this habit is owned by the current principal.
     *
     * This might be useful for checking granted authorizations like tracking, deleting and so on.
     *
     * @see isForeign
     *
     * @param principal the current principal of the "session" or context
     * @return ``true`` if the [principal] is the same as the [owner], else ``false``
     */
    fun isOwn(principal: UserData): Boolean = owner == principal

    /**
     * Convenience function to check, whether this habit is owned by a different user than the current principal.
     *
     * This might be useful for checking granted authorizations like tracking, deleting and so on, which are not
     * allowed for foreign habits of course.
     *
     * @see isOwn
     *
     * @param principal the current principal of the "session" or context
     * @return ``true`` if the [principal] differs from the [owner], else ``false``
     */
    fun isForeign(principal: UserData): Boolean = owner != principal
}

/**
 * This holds exactly one tracking data for a [Habit].
 *
 * There might be multiple [track entries][TrackEntry] per day, that is why the [id] is needed as unique identifier.
 */
data class TrackEntry(
    /**
     * Unique identity in order to distinguish different entries.
     */
    val id: String,

    val habit: Habit,
    val date: LocalDate,
    val additionalInformation: String = ""
)

/**
 * Holds all tracked entries for one day and one habit.
 *
 * This might be useful for an overview of all tracking data of one habit for one day.
 */
data class TrackEntriesByDate(
    val date: LocalDate,
    val entries: List<TrackEntry>
)

/**
 * Holds all tracked habits for one day grouped by habit.
 *
 * This might be useful for showing a dashboard.
 */
data class TrackEntriesByDateAndHabit(
    val date: LocalDate,
    val entries: Map<Habit, List<TrackEntry>>
)


