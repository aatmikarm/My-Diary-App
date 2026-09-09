package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DiaryDatabase
import com.example.data.DiaryEntry
import com.example.data.DiaryRepository
import com.example.data.SecurityManager
import com.example.util.DiaryUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class Screen {
    SPLASH,
    WELCOME,
    PIN_SETUP,
    LOCK,
    HOME,
    CALENDAR,
    SEARCH,
    CREATE_EDIT,
    DETAIL,
    DRAWING,
    SETTINGS
}

class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DiaryRepository
    val securityManager = SecurityManager(application)

    init {
        val dao = DiaryDatabase.getDatabase(application).diaryDao()
        repository = DiaryRepository(dao)
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<Screen>()

    // Security & Auth State
    private val _isUnlocked = MutableStateFlow(!securityManager.isAppLockEnabled)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _todayMood = MutableStateFlow(securityManager.todayMood)
    val todayMood: StateFlow<String> = _todayMood.asStateFlow()

    private val _themeMode = MutableStateFlow(securityManager.themeMode)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Entries Flow
    val allEntries: StateFlow<List<DiaryEntry>> = repository.allEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentEntries: StateFlow<List<DiaryEntry>> = repository.recentEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calendar State
    private val _selectedCalendarDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedCalendarDateMillis: StateFlow<Long> = _selectedCalendarDateMillis.asStateFlow()

    val entriesForSelectedDate: StateFlow<List<DiaryEntry>> = combine(
        allEntries,
        _selectedCalendarDateMillis
    ) { entries, selectedMillis ->
        entries.filter { DiaryUtils.isSameDay(it.dateMillis, selectedMillis) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeTagFilter = MutableStateFlow<String?>(null)
    val activeTagFilter: StateFlow<String?> = _activeTagFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<DiaryEntry>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.allEntries
        } else {
            repository.search(query.trim())
        }
    }.combine(_activeTagFilter) { results, filter ->
        if (filter == null) results
        else results.filter { it.tagsJson.contains(filter, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Entry for Create / Edit / Detail
    private val _activeEntryId = MutableStateFlow<Long?>(null)
    val activeEntryId: StateFlow<Long?> = _activeEntryId.asStateFlow()

    private val _currentDetailEntry = MutableStateFlow<DiaryEntry?>(null)
    val currentDetailEntry: StateFlow<DiaryEntry?> = _currentDetailEntry.asStateFlow()

    // Editing State (Temporary memory buffer while editing)
    var editEntryDraft: DiaryEntry? = null
    var pendingDoodlePath: String? = null

    init {
        // Seed initial welcoming entries if first launch
        viewModelScope.launch {
            if (securityManager.isFirstLaunch) {
                seedWelcomeEntries()
            }
        }
    }

    private suspend fun seedWelcomeEntries() {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // Entry 1: Today
        val entry1 = DiaryEntry(
            dateMillis = now,
            createdMillis = now - 3600000,
            title = "A peaceful evening walk",
            content = "Today felt like the first breath of autumn. I walked through the park on my way home, and the trees were just beginning to show hints of amber and copper.\n\nI sat on the wooden bench by the fountain listening to the gentle hum of the city fading into dusk. Sometimes the simplest moments hold the most quiet joy. The warm chamomile tea seemed to melt away the rush of the whole week.",
            mood = "Happy",
            tagsJson = DiaryUtils.toJsonList(listOf("#Peaceful", "#Walk", "#Bakery", "#Gratitude")),
            stickersJson = DiaryUtils.toJsonList(listOf("✨", "☕", "🍂")),
            isFavorite = true,
            location = "Greenwood Hill Park",
            weather = "21°C • Golden hour"
        )
        repository.insert(entry1)

        // Entry 2: Yesterday
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = cal.timeInMillis
        val entry2 = DiaryEntry(
            dateMillis = yesterday,
            createdMillis = yesterday,
            title = "Finished reading chapter 4",
            content = "Quiet afternoon reading on the balcony with iced peach tea. Feeling grateful for small peaceful moments and the gentle breeze fluttering through the pages of my favorite book.",
            mood = "Calm",
            tagsJson = DiaryUtils.toJsonList(listOf("#Reading", "#Quiet", "#Peaceful")),
            stickersJson = DiaryUtils.toJsonList(listOf("📖", "🌸")),
            isFavorite = false,
            location = "Home Balcony",
            weather = "24°C • Sunny"
        )
        repository.insert(entry2)

        // Entry 3: Earlier this week
        cal.add(Calendar.DAY_OF_YEAR, -2)
        val threeDaysAgo = cal.timeInMillis
        val entry3 = DiaryEntry(
            dateMillis = threeDaysAgo,
            createdMillis = threeDaysAgo,
            title = "Rooftop picnic with friends",
            content = "Watching the pink and purple horizon unfold as the sun went down over the skyline. We talked about childhood memories and plans for next year. Life feels sweet when shared.",
            mood = "Loved",
            tagsJson = DiaryUtils.toJsonList(listOf("#Friends", "#Sunset", "#Celebration")),
            stickersJson = DiaryUtils.toJsonList(listOf("❤️", "🎉")),
            isFavorite = true,
            location = "City View Terrace",
            weather = "23°C • Clear dusk"
        )
        repository.insert(entry3)
    }

    // Navigation methods
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            val prev = screenBackStack.removeAt(screenBackStack.size - 1)
            _currentScreen.value = prev
            return true
        }
        return false
    }

    fun openCreateEntry(forDateMillis: Long = System.currentTimeMillis()) {
        editEntryDraft = DiaryEntry(
            dateMillis = forDateMillis,
            mood = _todayMood.value
        )
        _activeEntryId.value = null
        navigateTo(Screen.CREATE_EDIT)
    }

    fun openEditEntry(entry: DiaryEntry) {
        editEntryDraft = entry.copy()
        _activeEntryId.value = entry.id
        navigateTo(Screen.CREATE_EDIT)
    }

    fun openEntryDetail(entry: DiaryEntry) {
        _currentDetailEntry.value = entry
        _activeEntryId.value = entry.id
        navigateTo(Screen.DETAIL)
    }

    fun refreshDetail(id: Long) {
        viewModelScope.launch {
            val entry = repository.getEntryByIdSync(id)
            _currentDetailEntry.value = entry
        }
    }

    // Database Actions
    fun saveEntry(entry: DiaryEntry, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = if (entry.id == 0L) {
                repository.insert(entry)
            } else {
                repository.update(entry)
                entry.id
            }
            refreshDetail(id)
            onComplete(id)
        }
    }

    fun deleteEntry(entry: DiaryEntry, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.delete(entry)
            if (_currentDetailEntry.value?.id == entry.id) {
                _currentDetailEntry.value = null
            }
            onComplete()
        }
    }

    fun toggleFavorite(entry: DiaryEntry) {
        viewModelScope.launch {
            val updated = entry.copy(isFavorite = !entry.isFavorite)
            repository.update(updated)
            _currentDetailEntry.value = updated
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
            _currentDetailEntry.value = null
        }
    }

    // Search Actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setActiveTagFilter(tag: String?) {
        _activeTagFilter.value = if (_activeTagFilter.value == tag) null else tag
    }

    // Calendar Actions
    fun setSelectedCalendarDate(dateMillis: Long) {
        _selectedCalendarDateMillis.value = dateMillis
    }

    // Mood & Preferences Actions
    fun setTodayMood(mood: String) {
        _todayMood.value = mood
        securityManager.todayMood = mood
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        securityManager.themeMode = mode
    }

    fun setAppLockEnabled(enabled: Boolean) {
        securityManager.isAppLockEnabled = enabled
    }

    fun setBiometricEnabled(enabled: Boolean) {
        securityManager.isBiometricEnabled = enabled
    }

    fun completeFirstLaunch(pin: String?, biometric: Boolean) {
        securityManager.isFirstLaunch = false
        if (!pin.isNullOrBlank()) {
            securityManager.setPin(pin)
            securityManager.isAppLockEnabled = true
        } else {
            securityManager.isAppLockEnabled = false
        }
        securityManager.isBiometricEnabled = biometric
        _isUnlocked.value = true
        screenBackStack.clear()
        _currentScreen.value = Screen.HOME
    }

    fun unlockWithPin(pin: String): Boolean {
        if (securityManager.verifyPin(pin)) {
            _isUnlocked.value = true
            screenBackStack.clear()
            _currentScreen.value = Screen.HOME
            return true
        }
        return false
    }

    fun unlockBiometric() {
        _isUnlocked.value = true
        screenBackStack.clear()
        _currentScreen.value = Screen.HOME
    }

    fun lockApp() {
        if (securityManager.isAppLockEnabled) {
            _isUnlocked.value = false
            screenBackStack.clear()
            _currentScreen.value = Screen.LOCK
        }
    }
}
