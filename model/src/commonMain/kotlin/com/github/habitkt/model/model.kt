package com.github.habitkt.model

import kotlinx.datetime.LocalDate

/*
TODO: In general: We still have mismatches between the pure DTO layer, where references are only represented by id
  Strings and domain classes, where the reference is resolved. Currently we have mixtures, as the user is always the
  full object, whereas the habitRefs are only Strings holding the IDs of a habit.
  We should consider to harmonize this.
  IMHO the domain object should hold the full object, whereas the DTO class should only hold the reference IDs.
 */

/**
 * Helps to hold and also to create a pair of a full length value with a dedicated abbreviation for compact display
 * purposes.
 */
data class WithAbbreviation(val value: String, val shortcut: String) {
    companion object {
        fun of(value: String): WithAbbreviation = WithAbbreviation(value, value.take(2))
    }

    init {
        require(shortcut.length <= 2) { "At maximum two character are allowed. Got ${shortcut.length} characters." }
    }
}

/**
 * TODO: Currently this user model is totally technology aware. Necessary fields might be added for authentication
 *  purposes.
 */
data class UserData(
    val id: String,
    val screenName: WithAbbreviation,
)

/**
 * Defines a conceptual color for the visual representation.
 *
 * The UI can later map those values to actual styling information (like background- or text-colors, border, etc)
 *
 * TODO: finalize this; should be part of the frontend team, as they will investigate the representing color options.
 */
enum class HabitColor {
    Red,
    Blue,
    Green,
    Cyan,
    // etc.
}

/**
 * Defines the type of target.
 *
 * Examples from user story:
 * As a user I want to define the target value for a habit like
 * - eat meat at most 3 times per week
 * - run at least 2 times per week
 * - work exactly five times a week
 */
enum class Target {
    AtLeast,
    AtMost,
    Exact
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
     * Holds the owner of a habit. It is important to distinguish the own habits from habits that gets shared by
     * someone else
     */
    val owner: UserData,

    /**
     * TODO: Question: Should this data be hosted by a view? So might a follower modify those values in order to make
     *  them fit better into his own dashboard? If so, this must go into the `View` class.
     */
    val title: WithAbbreviation,

    val description: String,

    val target: Target,

    /**
     * Should be weekly for the MVP; later it might get a scale too.
     */
    val targetValue: UInt,

    /**
     * Holds all views of a habit. At least one item (for the owner) is always present.
     *
     * TODO: Might be possible to hold the data as a `Map<UserData, View>` in order to improve DB lookup / indexing!
     */
    val views: List<View>,
) {
    data class View(
        val user: UserData,

        /**
         *  `true` for the owner of the enclosing habit, else `false`.
         */
        val isOwner: Boolean,

        /**
         * This flag is useful for partitioning all habits in two disjunctive sets:
         * - all active (``true``) habits can be tracked and are shown in the dashboard.
         * - all inactive (``false``) habits cannot be tracked and are not shown in the dashboard.
         *
         * A foreign habit should be initialized with ``false`` in order to protect a user from being "habit bombed" by
         * others.
         */
        val active: Boolean,

        /**
         * Defines the order for all visual appearances.
         * Higher values have more importance and should appear at the "best" position.
         */
        val order: UInt,

        val color: HabitColor,
    )

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
 * Fits for the REST API for CRUD operations of the central core attributes of a habit.
 * So only the owner is allowed to modify the habit by this.
 */
data class HabitDetails(
    /**
     *  Holds the ID of the corresponding habit.
     */
    val habitRef: String,

    /**
     * Holds the owner of a habit. It is important to distinguish the own habits from habits that gets shared by
     * someone else
     */
    val owner: UserData,

    /**
     * TODO: Question: Should this data be hosted by a view? So might a follower modify those values in order to make
     *  them fit better into his own dashboard? If so, this must go into the `View` class.
     */
    val title: WithAbbreviation,

    val description: String,

    val target: Target,

    /**
     * Should be weekly for the MVP; later it might get a scale too.
     */
    val targetValue: UInt,

    /**
     * Holds all views of a habit. At least one item (for the owner) is always present.
     *
     * TODO: Might be possible to hold the data as a `Set<UserData>` in order to improve DB lookup / indexing!
     */
    val sharedWith: List<UserData>,

    )

/**
 * TODO: Investigate question about `title` attribute above. If that should be part of the `Habit.View` class,
 *  this one becomes obsolete and the other should become stand alone class.
 *
 *  Fits for the REST API for the configuration of the dashboard (list of all habits for activation and sorting)
 *  and for the dashboard (header) itself.
 */
data class HabitView(
    /**
     * Holds the ID of the corresponding habit.
     */
    val habitRef: String,

    val user: UserData,

    val title: WithAbbreviation,

    /**
     *  `true` for the owner of the enclosing habit, else `false`.
     */
    val isOwner: Boolean,

    /**
     * This flag is useful for partitioning all habits in two disjunctive sets:
     * - all active (``true``) habits can be tracked and are shown in the dashboard.
     * - all inactive (``false``) habits cannot be tracked and are not shown in the dashboard.
     *
     * A foreign habit should be initialized with ``false`` in order to protect a user from being "habit bombed" by
     * others.
     */
    val active: Boolean,

    /**
     * Defines the order for all visual appearances.
     * Higher values have more importance and should appear at the "best" position.
     */
    val order: UInt,


    val color: HabitColor,

    )

/**
 * This holds exactly one tracking data for a [Habit].
 *
 * There might be multiple [track entries][TrackEntry] per day, that is why the [id] is needed as unique identifier.
 *
 * Fits for the REST API for creating or modifying one tracking entry.
 * Also for getting a `List<TrackEntry>` of some timeframe for the details page.
 */
data class TrackEntry(
    /**
     * Unique identity in order to distinguish different entries.
     */
    val id: String,

    val habitRef: String,
    val date: LocalDate,
    val value: UInt,
    val additionalInformation: String = ""
)

/**
 * Encapsulates the needed information for a time frame. Can be used to define some range in time (with days as scale
 * value).
 *
 * This is commonly needed for querying [TrackEntry] from the service.
 */
data class TimeFrame(val start: LocalDate, val days: UInt)

/**
 * Holds the static and calculated target values.
 *
 * This class is needed for the websocket based Dashboard part.
 *
 * @property sum: All [Habit.targetValue] values summed up for one day.
 */
data class DailyAssessment(
    val habitRef: String, // TODO: optional belassen oder entfernen?
    val date: LocalDate,
    val sum: UInt,
    val rating: UInt, // TODO: Correct type for representing percent?
)