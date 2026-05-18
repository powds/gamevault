package com.gamevault.presentation.vault

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.repository.VaultRepository
import com.gamevault.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
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

    private val _folders = MutableStateFlow<List<VaultFolder>>(emptyList())
    val folders: StateFlow<List<VaultFolder>> = _folders.asStateFlow()

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

    private val _currentFolderId = MutableStateFlow<Long?>(null)
    val currentFolderId: StateFlow<Long?> = _currentFolderId.asStateFlow()

    private val _sortConfig = MutableStateFlow(VaultSortConfig())
    val sortConfig: StateFlow<VaultSortConfig> = _sortConfig.asStateFlow()

    // App lock state
    private val _lockedApps = MutableStateFlow<Set<String>>(emptySet())
    val lockedApps: StateFlow<Set<String>> = _lockedApps.asStateFlow()

    init {
        loadFolders()
        loadFiles()
        loadHiddenApps()
        updateStorageInfo()
        loadLockedApps()
    }

    private fun loadFolders() {
        viewModelScope.launch {
            repository.getFolders().collect { folderList ->
                _folders.value = folderList
            }
        }
    }

    private fun loadFiles() {
        viewModelScope.launch {
            _isLoading.value = true
            val flow = if (_searchQuery.value.isNotBlank()) {
                repository.searchVaultItems(_searchQuery.value)
            } else {
                repository.getVaultItems()
            }
            flow.collect { items ->
                _vaultItems.value = items
                _filteredItems.value = repository.sortItems(items, _sortConfig.value)
            }
            _isLoading.value = false
        }
    }

    private fun loadHiddenApps() {
        viewModelScope.launch {
            repository.getHiddenApps().collect { apps ->
                _hiddenApps.value = apps
            }
        }
    }

    private fun loadLockedApps() {
        val locked = context.getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)
            .getStringSet("locked_apps", emptySet()) ?: emptySet()
        _lockedApps.value = locked
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
        _searchQuery.value = ""
        _currentFolderId.value = null
        when (index) {
            0 -> loadFiles()
            1 -> loadApps()
        }
    }

    fun openFolder(folderId: Long) {
        _currentFolderId.value = folderId
        viewModelScope.launch {
            _isLoading.value = true
            repository.getItemsInFolder(folderId).collect { items ->
                _filteredItems.value = repository.sortItems(items, _sortConfig.value)
            }
            _isLoading.value = false
        }
    }

    fun goBackToRoot() {
        _currentFolderId.value = null
        loadFiles()
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
        viewModelScope.launch {
            if (query.isBlank()) {
                repository.getVaultItems().collect { items ->
                    _filteredItems.value = repository.sortItems(items, _sortConfig.value)
                }
            } else {
                repository.searchVaultItems(query).collect { items ->
                    _filteredItems.value = repository.sortItems(items, _sortConfig.value)
                }
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        loadFiles()
    }

    fun setSortConfig(config: VaultSortConfig) {
        _sortConfig.value = config
        _filteredItems.value = repository.sortItems(_filteredItems.value, config)
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createFolder(name)
            loadFolders()
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            repository.deleteFolder(folderId)
            loadFolders()
            if (_currentFolderId.value == folderId) {
                goBackToRoot()
            }
        }
    }

    fun renameFolder(folderId: Long, newName: String) {
        viewModelScope.launch {
            repository.renameFolder(folderId, newName)
            loadFolders()
        }
    }

    fun moveItemToFolder(itemId: Long, folderId: Long?) {
        viewModelScope.launch {
            repository.moveItemToFolder(itemId, folderId)
            loadFiles()
        }
    }

    fun hideApp(app: ApplicationInfo) {
        viewModelScope.launch {
            val pm = context.packageManager
            val hiddenApp = HiddenApp(
                packageName = app.packageName,
                appName = app.loadLabel(pm).toString()
            )
            repository.hideApp(hiddenApp)
            loadHiddenApps()
        }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            repository.unhideApp(packageName)
            loadHiddenApps()
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

    fun isAppLocked(packageName: String): Boolean = _lockedApps.value.contains(packageName)

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) { /* Handle error */ }
        }
    }

    fun deleteItem(item: VaultItem) {
        viewModelScope.launch {
            repository.deleteVaultItem(item.id)
            loadFiles()
            updateStorageInfo()
        }
    }

    fun addFileToVault(filePath: String, fileName: String, type: VaultItemType, size: Long, folderId: Long? = null) {
        viewModelScope.launch {
            val newItem = VaultItem(
                id = System.currentTimeMillis(),
                name = fileName,
                type = type,
                path = filePath,
                size = size,
                dateAdded = System.currentTimeMillis(),
                folderId = folderId ?: _currentFolderId.value
            )
            repository.addVaultItem(newItem)
            loadFiles()
            updateStorageInfo()
        }
    }

    suspend fun generateThumbnail(item: VaultItem): String? = repository.generateThumbnail(item)

    fun updateStorageInfo() {
        viewModelScope.launch {
            val used = repository.getVaultStorageUsed()
            _storageInfo.value = repository.formatFileSize(used)
        }
    }

    fun formatFileSize(bytes: Long): String = repository.formatFileSize(bytes)

    fun isAppHidden(packageName: String): Boolean = _hiddenApps.value.any { it.packageName == packageName }

    // Auto-lock
    fun updateLastActive() = repository.updateLastActiveTime()
    fun shouldAutoLock(): Boolean = repository.shouldAutoLock()
    fun getAutoLockTimeout(): Int = repository.getAutoLockTimeout()
    fun setAutoLockTimeout(seconds: Int) = repository.setAutoLockTimeout(seconds)
}