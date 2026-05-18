package com.gamevault.presentation.vault

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.repository.VaultRepository
import com.gamevault.domain.model.HiddenApp
import com.gamevault.domain.model.VaultItem
import com.gamevault.domain.model.VaultItemType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: VaultRepository
) : ViewModel() {

    private val _vaultItems = MutableStateFlow<List<VaultItem>>(emptyList())
    val vaultItems: StateFlow<List<VaultItem>> = _vaultItems.asStateFlow()

    private val _filteredItems = MutableStateFlow<List<VaultItem>>(emptyList())
    val filteredItems: StateFlow<List<VaultItem>> = _filteredItems.asStateFlow()

    private val _hiddenApps = MutableStateFlow<List<HiddenApp>>(emptyList())
    val hiddenApps: StateFlow<List<HiddenApp>> = _hiddenApps.asStateFlow()

    private val _installedApps = MutableStateFlow<List<ApplicationInfo>>(emptyList())
    val installedApps: StateFlow<List<ApplicationInfo>> = _installedApps.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _storageInfo = MutableStateFlow("0 B")
    val storageInfo: StateFlow<String> = _storageInfo.asStateFlow()

    // App lock state
    private val _lockedApps = MutableStateFlow<Set<String>>(emptySet())
    val lockedApps: StateFlow<Set<String>> = _lockedApps.asStateFlow()

    init {
        loadSampleItems()
        updateStorageInfo()
        loadLockedApps()
    }

    private fun loadSampleItems() {
        _vaultItems.value = listOf(
            VaultItem(
                id = 1,
                name = "Summer Vacation 2024.jpg",
                type = VaultItemType.PHOTO,
                path = "/storage/emulated/0/DCIM/vacation.jpg",
                size = 2_500_000,
                dateAdded = System.currentTimeMillis()
            ),
            VaultItem(
                id = 2,
                name = "Important Document.pdf",
                type = VaultItemType.DOCUMENT,
                path = "/storage/emulated/0/Documents/important.pdf",
                size = 450_000,
                dateAdded = System.currentTimeMillis()
            ),
            VaultItem(
                id = 3,
                name = "Family Video.mp4",
                type = VaultItemType.VIDEO,
                path = "/storage/emulated/0/Movies/family.mp4",
                size = 85_000_000,
                dateAdded = System.currentTimeMillis()
            ),
            VaultItem(
                id = 4,
                name = "Screenshot.png",
                type = VaultItemType.PHOTO,
                path = "/storage/emulated/0/Pictures/Screenshots/screenshot.png",
                size = 1_200_000,
                dateAdded = System.currentTimeMillis()
            )
        )
        _filteredItems.value = _vaultItems.value
    }

    private fun loadLockedApps() {
        val locked = context.getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)
            .getStringSet("locked_apps", emptySet()) ?: emptySet()
        _lockedApps.value = locked
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
        _searchQuery.value = "" // Clear search on tab change
        when (index) {
            0 -> loadFiles()
            1 -> loadApps()
        }
    }

    private fun loadFiles() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getVaultItems().collect { items ->
                _vaultItems.value = items.ifEmpty { _vaultItems.value }
                _filteredItems.value = if (_searchQuery.value.isBlank()) {
                    _vaultItems.value
                } else {
                    repository.searchVaultItems(_searchQuery.value, _vaultItems.value)
                }
            }
            _isLoading.value = false
        }
    }

    fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                    .filter { it.packageName != context.packageName }
                    .sortedBy { it.loadLabel(pm).toString().lowercase() }

                _installedApps.value = apps
            }
            _isLoading.value = false
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        _filteredItems.value = repository.searchVaultItems(query, _vaultItems.value)
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _filteredItems.value = _vaultItems.value
    }

    fun hideApp(app: ApplicationInfo) {
        viewModelScope.launch {
            val pm = context.packageManager
            val hiddenApp = HiddenApp(
                packageName = app.packageName,
                appName = app.loadLabel(pm).toString()
            )
            repository.hideApp(hiddenApp)
            val current = _hiddenApps.value.toMutableList()
            current.add(hiddenApp)
            _hiddenApps.value = current
        }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            repository.unhideApp(packageName)
            val current = _hiddenApps.value.toMutableList()
            current.removeAll { it.packageName == packageName }
            _hiddenApps.value = current
        }
    }

    fun lockApp(packageName: String) {
        val prefs = context.getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)
        val current = prefs.getStringSet("locked_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(packageName)
        prefs.edit().putStringSet("locked_apps", current).apply()
        _lockedApps.value = current
    }

    fun unlockApp(packageName: String) {
        val prefs = context.getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)
        val current = prefs.getStringSet("locked_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(packageName)
        prefs.edit().putStringSet("locked_apps", current).apply()
        _lockedApps.value = current
    }

    fun isAppLocked(packageName: String): Boolean {
        return _lockedApps.value.contains(packageName)
    }

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteItem(item: VaultItem) {
        viewModelScope.launch {
            repository.deleteVaultItem(item.id)
            val current = _vaultItems.value.toMutableList()
            current.removeAll { it.id == item.id }
            _vaultItems.value = current
            _filteredItems.value = repository.searchVaultItems(_searchQuery.value, _vaultItems.value)
            updateStorageInfo()
        }
    }

    fun addFileToVault(filePath: String, fileName: String, type: VaultItemType, size: Long) {
        viewModelScope.launch {
            val newItem = VaultItem(
                id = System.currentTimeMillis(),
                name = fileName,
                type = type,
                path = filePath,
                size = size,
                dateAdded = System.currentTimeMillis()
            )
            repository.addVaultItem(newItem)
            val current = _vaultItems.value.toMutableList()
            current.add(0, newItem)
            _vaultItems.value = current
            _filteredItems.value = repository.searchVaultItems(_searchQuery.value, _vaultItems.value)
            updateStorageInfo()
        }
    }

    suspend fun generateThumbnail(item: VaultItem): String? {
        return repository.generateThumbnail(item)
    }

    fun updateStorageInfo() {
        viewModelScope.launch {
            val used = repository.getVaultStorageUsed()
            _storageInfo.value = repository.formatFileSize(used)
        }
    }

    fun formatFileSize(bytes: Long): String {
        return repository.formatFileSize(bytes)
    }

    fun isAppHidden(packageName: String): Boolean {
        return _hiddenApps.value.any { it.packageName == packageName }
    }

    // Auto-lock
    fun updateLastActive() {
        repository.updateLastActiveTime()
    }

    fun shouldAutoLock(): Boolean {
        return repository.shouldAutoLock()
    }

    fun getAutoLockTimeout(): Int {
        return repository.getAutoLockTimeout()
    }

    fun setAutoLockTimeout(seconds: Int) {
        repository.setAutoLockTimeout(seconds)
    }
}