package com.batal.elyoum.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batal.elyoum.ui.DeedWithStatus
import com.batal.elyoum.ui.HeroViewModel
import com.batal.elyoum.ui.theme.HeroGold
import com.batal.elyoum.ui.theme.HeroGoldDark
import com.batal.elyoum.ui.theme.HeroGoldLight
import com.batal.elyoum.ui.theme.HeroGreen
import com.batal.elyoum.ui.theme.HeroGreenContainer

@Composable
fun DailyDeedsScreen(
  viewModel: HeroViewModel,
  modifier: Modifier = Modifier
) {
  val deedsWithStatus by viewModel.dailyDeeds.collectAsState()
  val totalDaysActive by viewModel.totalDaysActive.collectAsState()

  val completedCount = deedsWithStatus.count { it.isCompleted }
  val totalCount = deedsWithStatus.size
  val totalPoints = deedsWithStatus.filter { it.isCompleted }.sumOf { it.deed.points }
  val maxPoints = deedsWithStatus.sumOf { it.deed.points }
  val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
  val isAllCompleted = completedCount == totalCount && totalCount > 0

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(12.dp))
      // Title Header
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "تحدي بطولتي اليومي",
          style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center
        )
        Text(
          text = "كل يوم فرصة لتكون بطلاً في أفعالك وأخلاقك وأثرك الطيب",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
      }
    }

    // Hero Progress Summary Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("deeds_summary_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(HeroGold, HeroGoldLight))
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Points indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(HeroGold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = null,
                  tint = HeroGoldDark,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "$totalPoints / $maxPoints",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "نقاط البطولة اليوم",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Streak indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.LocalFireDepartment,
                  contentDescription = null,
                  tint = Color(0xFFDC2626),
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "$totalDaysActive أيام",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "سجل الإنجاز",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Linear progress
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "إنجاز مهام اليوم ($completedCount من $totalCount)",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${(progress * 100).toInt()}%",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = HeroGoldDark
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(RoundedCornerShape(5.dp)),
            color = HeroGold,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
          )
        }
      }
    }

    // Celebratory All-Completed Golden Badge
    item {
      AnimatedVisibility(
        visible = isAllCompleted,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("golden_badge_celebration"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(
            containerColor = HeroGreenContainer.copy(alpha = 0.6f)
          ),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(HeroGreen, HeroGold))
          )
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = null,
              tint = HeroGoldDark,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "👑 بطل اليوم المتوّج!",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF065F46)
              )
              Text(
                text = "أتممت جميع المهام البطولية لليوم. فخورون بأثرك وعطائك النبيل!",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = Color(0xFF047857)
              )
            }
          }
        }
      }
    }

    // Deeds Checklist Items
    items(deedsWithStatus, key = { it.deed.id }) { item ->
      DeedCard(
        item = item,
        onToggle = { viewModel.toggleDeed(item.deed.id, item.isCompleted) }
      )
    }

    item {
      Spacer(modifier = Modifier.height(12.dp))
      // Encouragement Footer Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
      ) {
        Text(
          text = "« لا تحقرنّ من المعروف شيئاً، فكل بذرة خير تزرعها تصنع بطلاً في أعين من يحتاجك. »",
          style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        )
      }
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun DeedCard(
  item: DeedWithStatus,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isCompleted = item.isCompleted
  val deed = item.deed

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable { onToggle() }
      .testTag("deed_item_${deed.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isCompleted) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      } else {
        MaterialTheme.colorScheme.surface
      }
    ),
    border = if (isCompleted) {
      CardDefaults.outlinedCardBorder().copy(
        brush = Brush.linearGradient(listOf(HeroGreen.copy(alpha = 0.5f), Color.Transparent))
      )
    } else {
      CardDefaults.outlinedCardBorder()
    }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.Top
    ) {
      // Checkbox icon
      Icon(
        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
        contentDescription = if (isCompleted) "مكتمل" else "غير مكتمل",
        tint = if (isCompleted) HeroGreen else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
          .size(28.dp)
          .padding(top = 2.dp)
      )

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = deed.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold
            ),
            color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
          )

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isCompleted) HeroGreenContainer else HeroGold.copy(alpha = 0.15f)
          ) {
            Text(
              text = "+${deed.points} نقطة",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (isCompleted) Color(0xFF065F46) else HeroGoldDark,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = deed.description,
          style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
