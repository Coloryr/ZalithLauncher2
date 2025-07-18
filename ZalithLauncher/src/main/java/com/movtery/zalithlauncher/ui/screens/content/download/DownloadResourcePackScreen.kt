package com.movtery.zalithlauncher.ui.screens.content.download

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.movtery.zalithlauncher.game.download.assets.downloadSingleForVersions
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.download.DownloadAssetsScreen
import com.movtery.zalithlauncher.ui.screens.content.download.assets.download.DownloadAssetsScreenKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadSingleOperation
import com.movtery.zalithlauncher.ui.screens.content.download.assets.search.SearchResourcePackScreen
import com.movtery.zalithlauncher.ui.screens.content.download.assets.search.SearchResourcePackScreenKey
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadModBackStack
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadResourcePackBackStack
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadResourcePackScreenKey
import com.movtery.zalithlauncher.ui.screens.content.downloadScreenKey
import com.movtery.zalithlauncher.ui.screens.navigateTo
import kotlinx.serialization.Serializable

@Serializable
data object DownloadResourcePackScreenKey: NestedNavKey {
    override fun isLastScreen(): Boolean = downloadResourcePackBackStack.size <= 1
}

@Composable
fun DownloadResourcePackScreen() {
    val currentKey = downloadResourcePackBackStack.lastOrNull()

    LaunchedEffect(currentKey) {
        downloadResourcePackScreenKey = currentKey
    }

    val context = LocalContext.current

    //下载资源操作
    var operation by remember { mutableStateOf<DownloadSingleOperation>(DownloadSingleOperation.None) }
    DownloadSingleOperation(
        operation = operation,
        changeOperation = { operation = it },
        doInstall = { info, versions ->
            downloadSingleForVersions(
                context = context,
                info = info,
                versions = versions,
                folder = "resourcepacks"
            )
        }
    )

    NavDisplay(
        backStack = downloadResourcePackBackStack,
        modifier = Modifier.fillMaxSize(),
        onBack = {
            val key = downloadResourcePackBackStack.lastOrNull()
            if (key is NestedNavKey && !key.isLastScreen()) return@NavDisplay
            downloadResourcePackBackStack.removeLastOrNull()
        },
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<SearchResourcePackScreenKey> {
                SearchResourcePackScreen { platform, projectId ->
                    downloadResourcePackBackStack.navigateTo(
                        DownloadAssetsScreenKey(platform, projectId, PlatformClasses.RESOURCE_PACK)
                    )
                }
            }
            entry<DownloadAssetsScreenKey> { key ->
                DownloadAssetsScreen(
                    parentScreenKey = DownloadResourcePackScreenKey,
                    parentCurrentKey = downloadScreenKey,
                    currentKey = downloadResourcePackScreenKey,
                    key = key,
                    onItemClicked = { info ->
                        operation = DownloadSingleOperation.SelectVersion(info)
                    },
                    onDependencyClicked = { dep ->
                        downloadModBackStack.navigateTo(
                            DownloadAssetsScreenKey(dep.platform, dep.projectID, PlatformClasses.RESOURCE_PACK)
                        )
                    }
                )
            }
        }
    )
}