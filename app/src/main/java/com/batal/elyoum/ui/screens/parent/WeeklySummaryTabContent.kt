package com.batal.elyoum.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batal.elyoum.data.ChildProfileEntity
import com.batal.elyoum.ui.HeroViewModel
import com.batal.elyoum.ui.theme.HeroGold
import com.batal.elyoum.ui.theme.HeroGoldDark
import com.batal.elyoum.ui.theme.HeroGoldLight
import com.batal.elyoum.ui.theme.HeroGreen
import com.batal.elyoum.ui.theme.HeroGreenContainer
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeeklySummaryTabContent(
  viewModel: HeroViewModel,
  children: List<ChildProfileEntity>,
  modifier: Modifier = Modifier
) {
  val selectedChild by viewModel.selectedChild.collectAsState()
  val summary by viewModel.weeklySummary.collectAsState()

  // 0 = Current week (last 7 days), 1 = Previous week
  var weekOffset by remember { mutableIntStateOf(0) }

  fun reloadSummary(childId: String, offset: Int) {
    val today = LocalDate.now()
    val end = today.minusDays((offset * 7).toLong())
    val start = end.minusDays(6)
    viewModel.loadWeeklySummary(childId, start.toString(), end.toString())
  }

  LaunchedEffect(selectedChild?.id, weekOffset) {
    selectedChild?.let { child ->
      reloadSummary(child.id, weekOffset)
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(10.dp))
      // Pedagogical Philosophy Banner
      Card(
        modifier = Modifier.fillMaxWidth().testTag("summary_philosophy_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HeroGold.copy(alpha = 0.1f))
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "رؤية الملخص الأسبوعي التربوي",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = HeroGoldDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "نحتفي بالجهد والسعي المستمر والتعاون الأسري. لا نلجأ إلى المقارنة بين الإخوة أو فرض نسب صارمة، بل نوثق الأثر الإيجابي بكل محبة ورفق.",
              style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Child Selector
    if (children.isNotEmpty()) {
      item {
        Column {
          Text(
            text = "اختر البطل الصغير:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            children.forEach { child ->
              val isSelected = child.id == selectedChild?.id
              FilterChip(
                selected = isSelected,
                onClick = { viewModel.selectChild(child.id) },
                label = { Text(child.alias) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = HeroGold.copy(alpha = 0.25f),
                  selectedLabelColor = HeroGoldDark
                ),
                modifier = Modifier.testTag("summary_child_chip_${child.id}")
              )
            }
          }
        }
      }

      // Week Selector
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          FilterChip(
            selected = weekOffset == 0,
            onClick = { weekOffset = 0 },
            label = { Text("الأسبوع الحالي") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = HeroGreen.copy(alpha = 0.2f),
              selectedLabelColor = Color(0xFF065F46)
            ),
            modifier = Modifier.testTag("summary_current_week_chip")
          )
          FilterChip(
            selected = weekOffset == 1,
            onClick = { weekOffset = 1 },
            label = { Text("الأسبوع السابق") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = HeroGreen.copy(alpha = 0.2f),
              selectedLabelColor = Color(0xFF065F46)
            ),
            modifier = Modifier.testTag("summary_previous_week_chip")
          )
        }
      }
    }

    if (children.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
          Text(
            text = "يرجى إضافة ملف طفل أولاً لعرض ملخصه الأسبوعي.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(24.dp)
          )
        }
      }
    } else if (summary != null) {
      val data = summary!!

      // Summary Header Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth().testTag("summary_metrics_card"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(HeroGold, HeroGoldLight))
          )
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = "حصاد إنجازات «${data.childAlias}»",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "من ${data.startDate} إلى ${data.endDate}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              MetricItem(
                count = data.completedCount,
                label = "مهام منجزة",
                iconColor = HeroGreen,
                textColor = Color(0xFF065F46)
              )
              MetricItem(
                count = data.postponedCount,
                label = "مؤجلة برفق",
                iconColor = HeroGoldDark,
                textColor = HeroGoldDark
              )
              MetricItem(
                count = data.skippedCount,
                label = "مُتخطاة بلا لوم",
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant
              )
              MetricItem(
                count = data.fulfilledRewardsCount,
                label = "لحظات محققة",
                iconColor = HeroGold,
                textColor = HeroGoldDark
              )
            }
          }
        }
      }

      // Gentle Support Suggestion
      item {
        Card(
          modifier = Modifier.fillMaxWidth().testTag("gentle_support_card"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = HeroGreenContainer.copy(alpha = 0.45f)),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(HeroGreen, HeroGold))
          )
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "رسالة تشجيع واقتراح تربوي",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF065F46)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = data.gentleSupportSuggestion,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = Color(0xFF047857)
              )
            }
          }
        }
      }

      // Fulfilled rewards list if any
      if (data.fulfilledRewards.isNotEmpty()) {
        item {
          Text(
            text = "اللحظات الجميلة التي تم عيشها هذا الأسبوع:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 6.dp)
          )
        }

        items(data.fulfilledRewards.size) { index ->
          val reward = data.fulfilledRewards[index]
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Star, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = reward.title,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (reward.description.isNotBlank()) {
                  Text(
                    text = reward.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }

      // Disclaimer Note
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
        ) {
          Text(
            text = data.disclaimer,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}

@Composable
fun MetricItem(
  count: Int,
  label: String,
  iconColor: Color,
  textColor: Color
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = "$count",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = textColor
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
