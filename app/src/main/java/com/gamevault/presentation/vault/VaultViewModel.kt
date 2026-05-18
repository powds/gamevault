package com.gamevault.presentation.vault

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _vaultItems = MutableStateFlow<List<VaultItem>>(emptyList())
    val vaultItems: StateFlow<List<VaultItem>> = _vaultItems.asStateFlow()

    private val _hiddenApps = MutableStateFlow<List<HiddenApp>>(emptyList())
    val hiddenApps: StateFlow<List<HiddenApp>> = _hiddenApps.asStateFlow()

    private val _installedApps = MutableStateFlow<List<ApplicationInfo>>(emptyList())
    val installedApps: StateFlow<List<ApplicationInfo>> = _installedApps.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSampleItems()
    }

    private fun loadSampleItems() {
        // Sample items for demo - in production these would be loaded from encrypted storage
        _vaultItems.value = listOf(
            VaultItem(
                id = 1,
                name = "Summer Vacation 2024.jpg",
                type = VaultItemType.PHOTO,
                path = "/storage/emulated/0/DCIM/vacation.jpg",
                size = 2_500_000
            ),
            VaultItem(
                id = 2,
                name = "Important Document.pdf",
                type = VaultItemType.DOCUMENT,
                path = "/storage/emulated/0/Documents/important.pdf",
                size = 450_000
            ),
            VaultItem(
                id = 3,
                name = "Family Video.mp4",
                type = VaultItemType.VIDEO,
                path = "/storage/emulated/0/Movies/family.mp4",
                size = 85_000_000
            )
        )
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
        when (index) {
            0 -> loadFiles()
            1 -> loadApps()
        }
    }

    private fun loadFiles() {
        viewModelScope.launch {
            _isLoading.value = true
            // In production: load from encrypted Room database
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

    fun hideApp(app: ApplicationInfo) {
        viewModelScope.launch {
            val pm = context.packageManager
            val hiddenApp = HiddenApp(
                packageName = app.packageName,
                appName = app.loadLabel(pm).toString()
            )
            val current = _hiddenApps.value.toMutableList()
            current.add(hiddenApp)
            _hiddenApps.value = current
        }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            val current = _hiddenApps.value.toMutableList()
            current.removeAll { it.packageName == packageName }
            _hiddenApps.value = current
        }
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
            val current = _vaultItems.value.toMutableList()
            current.removeAll { it.id == item.id }
            _vaultItems.value = current
        }
    }

    fun addFileToVault(filePath: String, fileName: String, type: VaultItemType, size: Long) {
        viewModelScope.launch {
            val newItem = VaultItem(
                id = System.currentTimeMillis(),
                name = fileName,
                type = type,
                path = filePath,
                size = size
            )
            val current = _vaultItems.value.toMutableList()
            current.add(0, newItem)
            _vaultItems.value = current
        }
    }
}