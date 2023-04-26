package ui.dialog.filter

import localized

enum class MatchType {
    EXACT, FUZZY, SEARCHABLE;
}

fun MatchType.toReadableString() = when (this) {
    MatchType.EXACT -> "Exact match".localized()
    MatchType.FUZZY -> "Fuzzy match".localized()
    MatchType.SEARCHABLE -> "Include in search field".localized()
}