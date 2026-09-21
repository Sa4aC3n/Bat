package com.batal.elyoum.ui

import android.app.Application
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.batal.elyoum.R
import com.batal.elyoum.ui.components.ParentPinGateDialog
import com.batal.elyoum.ui.screens.DailyDeedsScreen
import com.batal.elyoum.ui.screens.HeroArchiveScreen
import com.batal.elyoum.ui.screens.HonorHeroScreen
import com.batal.elyoum.ui.screens.ParentsSectionScreen
import com.batal.elyoum.ui.screens.TodayHeroScreen
import com.batal.elyoum.ui.theme.HeroGold
import com.batal.elyoum.ui.theme.HeroGoldDark
import com.batal.elyoum.ui.theme.HeroGoldLight

enum class AppNavTab(val title: String, val testTag: String) {
  TODAY_HERO("بطل اليوم", "nav_today_hero"),
  MY_CHALLENGE("تحدي بطولتي", "nav_my_challenge"),
  HONOR_HERO("كرّم بطلك", "nav_honor_hero"),
  ARCHIVE("الأرشيف", "nav_archive")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  viewModel: HeroViewModel = viewModel(
    factory = HeroViewModel.Factory(
      LocalContext.current.applicationContext as Application
    )
  )
) {
  var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
  var isInParentSection by rememberSaveable { mutableStateOf(false) }
  var showPinGateDialog by remember { mutableStateOf(false) }

  val isParentSessionUnlocked by viewModel.isParentSessionUnlocked.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()
  val lifecycleOwner = LocalLifecycleOwner.current

  // Handle lifecycle: lock session on stop, refresh today's tasks on resume
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_STOP) {
        viewModel.lockParentSession()
      } else if (event == Lifecycle.Event.ON_RESUME) {
        viewModel.refreshTodayDate()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  // If session was locked while in parent section, navigate back out
  if (isInParentSection && !isParentSessionUnlocked) {
    isInParentSection = false
  }

  // Full Arabic RTL experience
  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    if (isInParentSection && isParentSessionUnlocked) {
      ParentsSectionScreen(
        viewModel = viewModel,
        onNavigateBack = { isInParentSection = false }
      )
    } else {
      Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
          CenterAlignedTopAppBar(
            title = {
              Row(
                verticalAlignment = Alignment.CenterVertically
              ) {
                Image(
                  painter = painterResource(id = R.drawable.batal_al_yawm_icon),
                  contentDescription = null,
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "بطل اليوم",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                  ),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            },
            actions = {
              TextButton(
                onClick = {
                  if (isParentSessionUnlocked) {
                    isInParentSection = true
                  } else {
                    showPinGateDialog = true
                  }
                },
                modifier = Modifier
                  .padding(end = 4.dp)
                  .testTag("parent_gate_button")
              ) {
                Icon(
                  imageVector = if (isParentSessionUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                  contentDescription = "قسم الوالدين",
                  tint = HeroGoldDark,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "للأهل",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = HeroGoldDark
                )
              }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
              containerColor = MaterialTheme.colorScheme.surface
            )
          )
        },
        bottomBar = {
          NavigationBar(
            modifier = Modifier
              .fillMaxWidth()
              .windowInsetsPadding(WindowInsets.navigationBars),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
          ) {
            AppNavTab.values().forEachIndexed { index, tab ->
              val isSelected = selectedTabIndex == index
              NavigationBarItem(
                modifier = Modifier.testTag(tab.testTag),
                selected = isSelected,
                onClick = { selectedTabIndex = index },
                icon = {
                  val iconVector = when (tab) {
                    AppNavTab.TODAY_HERO -> Icons.Default.WorkspacePremium
                    AppNavTab.MY_CHALLENGE -> Icons.Default.CheckCircle
                    AppNavTab.HONOR_HERO -> Icons.Default.EmojiEvents
                    AppNavTab.ARCHIVE -> Icons.Default.AutoStories
                  }
                  Icon(
                    imageVector = iconVector,
                    contentDescription = tab.title,
                    tint = if (isSelected) HeroGoldDark else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                },
                label = {
                  Text(
                    text = tab.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) HeroGoldDark else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  indicatorColor = HeroGold.copy(alpha = 0.2f)
                )
              )
            }
          }
        }
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
          Crossfade(targetState = selectedTabIndex, label = "TabCrossfade") { tabIdx ->
            when (tabIdx) {
              0 -> TodayHeroScreen(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
              )
              1 -> DailyDeedsScreen(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
              )
              2 -> HonorHeroScreen(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
              )
              3 -> HeroArchiveScreen(
                viewModel = viewModel,
                onHeroSelected = { hero ->
                  viewModel.selectHero(hero)
                  selectedTabIndex = 0
                },
                modifier = Modifier.fillMaxSize()
              )
            }
          }
        }
      }
    }

    if (showPinGateDialog) {
      ParentPinGateDialog(
        viewModel = viewModel,
        onDismiss = { showPinGateDialog = false },
        onSuccess = {
          showPinGateDialog = false
          isInParentSection = true
        }
      )
    }

    if (errorMessage != null) {
      AlertDialog(
        onDismissRequest = { viewModel.dismissErrorMessage() },
        title = {
          Text(
            text = "تنبيه",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Text(
            text = errorMessage ?: "",
            style = MaterialTheme.typography.bodyMedium
          )
        },
        confirmButton = {
          TextButton(onClick = { viewModel.dismissErrorMessage() }) {
            Text("حسناً")
          }
        }
      )
    }
  }
}
