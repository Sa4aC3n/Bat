package com.batal.elyoum.ui.screens.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batal.elyoum.ui.HeroViewModel
import com.batal.elyoum.ui.theme.HeroGold
import com.batal.elyoum.ui.theme.HeroGoldDark

@Composable
fun SettingsTabContent(
  viewModel: HeroViewModel,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.appSettings.collectAsState()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(10.dp))
      // Header Info
      Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_header_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HeroGold.copy(alpha = 0.1f))
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(Icons.Default.Tune, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "تفضيلات وإعدادات التطبيق",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = HeroGoldDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "تحكم في تجربة الاستخدام، الإشعارات الهادئة، والخصوصية لتناسب إيقاع أسرتك اليومي.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // SECTION: THEME MODE
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("theme_settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DarkMode, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "المظهر ونمط الألوان",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          Text(
            text = "اختر مظهر التطبيق المفضل لديك أو اتركه يتبع إعداد النظام تلقائياً.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val currentMode = settings.themeMode
            FilterChip(
              selected = currentMode == "SYSTEM",
              onClick = { viewModel.updateThemeMode("SYSTEM") },
              label = { Text("تلقائي النظام") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = HeroGold.copy(alpha = 0.25f),
                selectedLabelColor = HeroGoldDark
              ),
              modifier = Modifier.testTag("theme_system_chip")
            )
            FilterChip(
              selected = currentMode == "LIGHT",
              onClick = { viewModel.updateThemeMode("LIGHT") },
              label = { Text("فاتح ناصع") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = HeroGold.copy(alpha = 0.25f),
                selectedLabelColor = HeroGoldDark
              ),
              modifier = Modifier.testTag("theme_light_chip")
            )
            FilterChip(
              selected = currentMode == "DARK",
              onClick = { viewModel.updateThemeMode("DARK") },
              label = { Text("داكن مريح") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = HeroGold.copy(alpha = 0.25f),
                selectedLabelColor = HeroGoldDark
              ),
              modifier = Modifier.testTag("theme_dark_chip")
            )
          }
        }
      }
    }

    // SECTION: NOTIFICATIONS
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("notification_settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "الإشعارات الهادئة",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "تفعيل إشعارات التذكير اليومية",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
              )
              Text(
                text = "تذكير لطيف للأطفال بمتابعة أعمالهم ومهامهم اليومية.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = settings.notificationsEnabled,
              onCheckedChange = { checked ->
                viewModel.updateNotificationSettings(
                  enabled = checked,
                  startHour = settings.notificationQuietHourStart,
                  endHour = settings.notificationQuietHourEnd,
                  hideOnLock = settings.hideTaskDetailsOnLockScreen
                )
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = HeroGoldDark,
                checkedTrackColor = HeroGold.copy(alpha = 0.4f)
              ),
              modifier = Modifier.testTag("notifications_toggle")
            )
          }

          // Quiet Hours Info
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Bedtime, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "ساعات الهدوء التلقائية: من 9:00 مساءً حتى 7:00 صباحاً (لا ترسل أي إشعارات خلال نوم الطفل).",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Lock screen privacy
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "إخفاء تفاصيل المهام في شاشة القفل",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
              )
              Text(
                text = "تعزيز خصوصية الأسرة بعدم عرض نصوص المهام على الشاشة المغلقة.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = settings.hideTaskDetailsOnLockScreen,
              onCheckedChange = { checked ->
                viewModel.updateNotificationSettings(
                  enabled = settings.notificationsEnabled,
                  startHour = settings.notificationQuietHourStart,
                  endHour = settings.notificationQuietHourEnd,
                  hideOnLock = checked
                )
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = HeroGoldDark,
                checkedTrackColor = HeroGold.copy(alpha = 0.4f)
              ),
              modifier = Modifier.testTag("hide_lock_details_toggle")
            )
          }
        }
      }
    }

    // SECTION: REDUCE MOTION
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("motion_settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MotionPhotosOff, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "سهولة الاستخدام وتقليل الحركة",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "تقليل الحركات والمؤثرات البصرية",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
              )
              Text(
                text = "تقليل احتفاليات الألوان والحركات البصرية الكثيفة لتوفير بيئة هادئة وأداء خفيف.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = settings.reduceMotionCelebration,
              onCheckedChange = { viewModel.updateReduceMotion(it) },
              colors = SwitchDefaults.colors(
                checkedThumbColor = HeroGoldDark,
                checkedTrackColor = HeroGold.copy(alpha = 0.4f)
              ),
              modifier = Modifier.testTag("reduce_motion_toggle")
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}
