# REST API

## Habit

create new habit
```http
POST habit/
// owner = principal
HabitDetails
```

Update/ Delete habit
```http
GET / PUT / DELETE habit/{id}
// check owner == principal
HabitDetails
````

### Modify a view of a habit

No need for providing the view user; there can only be one view entry per principal!
```http
POST / PUT / DELETE habit/{id}/view
-> HabitView
```


### Tracking data

Create or Modify one TrackEntry
```http
POST / PUT / DELETE habit/{id}/tracking/{trackEntryId}
// check if principal == owner of habit
```

Get all tracking entries for some time frame.
Needs default values if time frame parameters are omitted.
Proposal: Last 30 days? (calculate frame accordingly)
```http
GET habit/{id}/tracking?start=YYYY-MM-DD&days=int
-> List<TrackEntry>
```

## Views

Get all habits as view for one user (needed for dashboard config)
```http
GET views
// filtered user == principal
-> List<HabitView>
```

Get all active habits as view for one user (needed for dashboard)
```http
GET views/active
// filtered by user == principal
-> List<HabitView>
```

# WebSocket

```
(Client) --[TimeFrame, habitRef]--> (Server) // we need two variants for subscribe / unsubscribe

(Server) --[List<DailyAssessment>]--> (Client)
```
