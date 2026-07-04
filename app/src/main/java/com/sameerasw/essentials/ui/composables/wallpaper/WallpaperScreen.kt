package com.sameerasw.essentials.ui.composables.wallpaper

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.SubcomposeAsyncImage
import com.sameerasw.essentials.R
import com.sameerasw.essentials.ui.components.EssentialsFloatingToolbar
import com.sameerasw.essentials.ui.components.containers.RoundedCardContainer
import com.sameerasw.essentials.ui.modifiers.BlurDirection
import com.sameerasw.essentials.ui.modifiers.progressiveBlur
import com.sameerasw.essentials.utils.HapticUtil
import com.sameerasw.essentials.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WallpaperScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val view = LocalView.current
    val wallpaperInfo by viewModel.dailyWallpaperInfo
    val isLoading by viewModel.isWallpaperLoading
    val isBlurEnabled by viewModel.isBlurEnabled
    val isAutoUpdateEnabled by viewModel.isDailyWallpaperAutoUpdateEnabled

    var showHelpSheet by remember { mutableStateOf(false) }
    var showSettingsCard by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchTodayWallpaper(context)
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBlurHeightPx = with(density) { (statusBarHeight * 1.6f).toPx() }

    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomBlurHeightPx = with(density) { (220.dp + bottomPadding).toPx() }

    val pullRefreshState = rememberPullToRefreshState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (wallpaperInfo != null) {
            SubcomposeAsyncImage(
                model = wallpaperInfo!!.urlMobile,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .progressiveBlur(
                        blurRadius = if (isBlurEnabled) 45f else 0f,
                        height = topBlurHeightPx,
                        direction = BlurDirection.TOP,
                        showGradientOverlay = true
                    )
                    .progressiveBlur(
                        blurRadius = if (isBlurEnabled) 45f else 0f,
                        height = bottomBlurHeightPx,
                        direction = BlurDirection.BOTTOM,
                        showGradientOverlay = true
                    ),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.rounded_draw_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    LoadingIndicator()
                }
            }
        }

        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = {
                HapticUtil.performUIHaptic(view)
                viewModel.fetchTodayWallpaper(context)
            },
            state = pullRefreshState,
            indicator = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = statusBarHeight + 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val scale = if (isLoading) 1f else pullRefreshState.distanceFraction.coerceIn(0f, 1f)
                    if (scale > 0f) {
                        Card(
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            modifier = Modifier
                                .size(48.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    alpha = scale
                                }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                LoadingIndicator(
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AnimatedVisibility(
                    visible = wallpaperInfo != null,
                    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400)) { it / 2 },
                    exit = fadeOut(animationSpec = tween(400)) + slideOutVertically(animationSpec = tween(400)) { it / 2 },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 96.dp + bottomPadding)
                        .zIndex(2f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AnimatedVisibility(
                            visible = showSettingsCard,
                            enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.label_wallpaper_auto_update),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Switch(
                                        checked = isAutoUpdateEnabled,
                                        onCheckedChange = { checked ->
                                            HapticUtil.performUIHaptic(view)
                                            viewModel.setDailyWallpaperAutoUpdate(checked, context)
                                        }
                                    )
                                }
                            }
                        }

                        SplitButtonLayout(
                            leadingButton = {
                                SplitButtonDefaults.LeadingButton(
                                    onClick = {
                                        HapticUtil.performHeavyHaptic(view)
                                        wallpaperInfo?.urlMobile?.let { url ->
                                            viewModel.applyWallpaper(context, url) { success ->
                                                if (!success) {
                                                    Toast.makeText(context, R.string.label_wallpaper_apply_failed, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.rounded_open_in_new_24),
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.label_wallpaper_apply),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            },
                            trailingButton = {
                                SplitButtonDefaults.TrailingButton(
                                    onClick = {
                                        HapticUtil.performUIHaptic(view)
                                        showSettingsCard = !showSettingsCard
                                    },
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (showSettingsCard) R.drawable.rounded_keyboard_arrow_down_24 else R.drawable.rounded_keyboard_arrow_up_24),
                                        contentDescription = "Options",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        )
                    }
                }

                EssentialsFloatingToolbar(
                    title = stringResource(R.string.feat_daily_wallpaper_title),
                    onBackClick = {
                        HapticUtil.performUIHaptic(view)
                        onBack()
                    },
                    onHelpClick = {
                        HapticUtil.performUIHaptic(view)
                        showHelpSheet = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(1f)
                )
            }
        }

        if (showHelpSheet) {
            WallpaperHelpBottomSheet(
                onDismissRequest = { showHelpSheet = false },
                wallpaperInfo = wallpaperInfo,
                onViewImage = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(wallpaperInfo?.photoLink ?: ""))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                onViewAuthor = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(wallpaperInfo?.authorLink ?: ""))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                onViewCollection = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://unsplash.com/collections/LqO9knU9z2A/Likes"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperHelpBottomSheet(
    onDismissRequest: () -> Unit,
    wallpaperInfo: com.sameerasw.essentials.domain.model.WallpaperInfo?,
    onViewImage: () -> Unit,
    onViewAuthor: () -> Unit,
    onViewCollection: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.feat_daily_wallpaper_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = stringResource(R.string.feat_daily_wallpaper_desc),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (wallpaperInfo != null) {
                RoundedCardContainer {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceBright, shape = MaterialTheme.shapes.extraSmall)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Current Photo Credits",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onViewImage,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(text = "View Photo")
                            }

                            Button(
                                onClick = onViewAuthor,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(
                                    text = wallpaperInfo.authorName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            RoundedCardContainer {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceBright, shape = MaterialTheme.shapes.extraSmall)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onViewCollection,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(text = "View Sameera's Collection")
                    }
                }
            }
        }
    }
}
