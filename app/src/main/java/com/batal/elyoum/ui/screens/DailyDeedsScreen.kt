package com.batal.elyoum.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.batal.elyoum.data.AgeGroup
import com.batal.elyoum.data.TaskOccurrenceEntity
import com.batal.elyoum.data.TaskOccurrenceStatus
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

  val activeChildren by viewModel.activeChildren.collectAsState()
  val selectedChild by viewModel.selectedChild.collectAsState()
  val todayOccurrences by viewModel.todayChildOccurrences.collectAsState()
  val latestPraise by viewModel.latestPraiseMessage.collectAsState()

  var showChildSwitchDialog by remember { mutableStateOf(false) }

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
          modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )
      }
    }

    // --- Effort-based Praise Banner ---
    item {
      AnimatedVisibility(visible = latestPraise != null) {
        latestPraise?.let { praise ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("effort_praise_banner"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HeroGreenContainer.copy(alpha = 0.85f)),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(listOf(HeroGreen, HeroGold))
            )
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = HeroGoldDark,
                modifier = Modifier.size(28.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = praise,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF065F46),
                modifier = Modifier.weight(1f)
              )
              IconButton(onClick = { viewModel.dismissPraiseMessage() }) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF065F46))
              }
            }
          }
        }
      }
    }

    // --- Child Selector Card ---
    item {
      if (activeChildren.isNotEmpty()) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = activeChildren.size > 1) { showChildSwitchDialog = true }
            .testTag("child_selector_card"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          )
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(HeroGold.copy(alpha = 0.25f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = HeroGoldDark,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = selectedChild?.alias ?: "اختر البطل الصغير",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = selectedChild?.let { AgeGroup.fromCode(it.ageGroup).label } ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            if (activeChildren.size > 1) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "تبديل",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = HeroGoldDark
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = HeroGoldDark)
              }
            }
          }
        }
      } else {
        // Welcome Card when no child profiles exist yet
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("no_children_card"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
          )
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Info, contentDescription = null, tint = HeroGoldDark)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "مرحبًا بكم في بطل اليوم!",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "يمكن لولي الأمر الضغط على زر «للأهل» بالأعلى لإنشاء ملفات الأطفال وتحديد مهامهم اليومية ومتابعتها بأمان.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // --- Section: Today's Parent Tasks ---
    if (selectedChild != null) {
      item {
        Text(
          text = "مهام اليوم الخاصة بـ «${selectedChild!!.alias}»",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(top = 4.dp)
        )
      }

      if (todayOccurrences.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("no_parent_tasks_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
          ) {
            Text(
              text = "لا توجد مهام إضافية مسندة لليوم من الوالدين. أحسنت يا بطل!",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            )
          }
        }
      } else {
        items(todayOccurrences, key = { it.id }) { occ ->
          ParentTaskOccurrenceCard(
            occurrence = occ,
            onSubmit = { viewModel.submitTaskCompletion(occ) },
            onCancel = { viewModel.cancelTaskPendingApproval(occ.id) },
            onSkip = { viewModel.skipTaskToday(occ.id) }
          )
        }
      }

      item {
        Divider(
          modifier = Modifier.padding(vertical = 4.dp),
          color = MaterialTheme.colorScheme.outlineVariant
        )
      }
    }

    // Section header for general deeds
    item {
      Text(
        text = "أعمال الخير والقيم العامة",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
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
                  text = "نقاط المبادرة",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Days indicator
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
              text = "إنجاز أعمال اليوم ($completedCount من $totalCount)",
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
      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // Switch Child Dialog
  if (showChildSwitchDialog) {
    AlertDialog(
      onDismissRequest = { showChildSwitchDialog = false },
      title = { Text("اختر البطل الصغير") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          activeChildren.forEach { child ->
            val isCurrent = child.id == selectedChild?.id
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isCurrent) HeroGold.copy(alpha = 0.2f) else Color.Transparent)
                .clickable {
                  viewModel.selectChild(child.id)
                  showChildSwitchDialog = false
                }
                .padding(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(HeroGoldDark.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = child.alias,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = AgeGroup.fromCode(child.ageGroup).label,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              if (isCurrent) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HeroGoldDark)
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showChildSwitchDialog = false }) { Text("إغلاق") }
      }
    )
  }
}

// ==========================================
// --- Parent Task Occurrence Card ---
// ==========================================

@Composable
fun ParentTaskOccurrenceCard(
  occurrence: TaskOccurrenceEntity,
  onSubmit: () -> Unit,
  onCancel: () -> Unit,
  onSkip: () -> Unit
) {
  val status = TaskOccurrenceStatus.fromCode(occurrence.status)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("occurrence_card_${occurrence.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = when (status) {
        TaskOccurrenceStatus.COMPLETED -> HeroGreenContainer.copy(alpha = 0.35f)
        TaskOccurrenceStatus.SKIPPED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        TaskOccurrenceStatus.PENDING_APPROVAL -> HeroGold.copy(alpha = 0.12f)
        TaskOccurrenceStatus.NOT_STARTED -> MaterialTheme.colorScheme.surface
      }
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = occurrence.snapshotTitle,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          if (occurrence.snapshotDescription.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = occurrence.snapshotDescription,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Status Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
              when (status) {
                TaskOccurrenceStatus.COMPLETED -> HeroGreen.copy(alpha = 0.15f)
                TaskOccurrenceStatus.PENDING_APPROVAL -> HeroGold.copy(alpha = 0.25f)
                TaskOccurrenceStatus.SKIPPED -> MaterialTheme.colorScheme.outlineVariant
                TaskOccurrenceStatus.NOT_STARTED -> MaterialTheme.colorScheme.surfaceVariant
              }
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = when (status) {
              TaskOccurrenceStatus.COMPLETED -> HeroGreen
              TaskOccurrenceStatus.PENDING_APPROVAL -> HeroGoldDark
              TaskOccurrenceStatus.SKIPPED -> MaterialTheme.colorScheme.outline
              TaskOccurrenceStatus.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant
            }
          )
        }
      }

      // Optional feedback note from parent
      if (!occurrence.parentFeedbackNote.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(HeroGold.copy(alpha = 0.12f))
            .padding(8.dp)
        ) {
          Text(
            text = "ملاحظة لطيفة من الأهل: ${occurrence.parentFeedbackNote}",
            style = MaterialTheme.typography.bodySmall,
            color = HeroGoldDark
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action buttons depending on state
      when (status) {
        TaskOccurrenceStatus.NOT_STARTED -> {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = onSubmit,
              colors = ButtonDefaults.buttonColors(containerColor = HeroGoldDark),
              modifier = Modifier.weight(1f).testTag("submit_occurrence_${occurrence.id}")
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("أنجزتها")
            }

            OutlinedButton(
              onClick = onSkip,
              modifier = Modifier.weight(1f).testTag("skip_occurrence_${occurrence.id}")
            ) {
              Text("هعدّيها النهارده")
            }
          }
        }
        TaskOccurrenceStatus.PENDING_APPROVAL -> {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.HourglassTop, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "بانتظار تأكيد ولي الأمر",
                style = MaterialTheme.typography.bodySmall,
                color = HeroGoldDark
              )
            }
            TextButton(
              onClick = onCancel,
              modifier = Modifier.testTag("cancel_pending_${occurrence.id}")
            ) {
              Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("تراجع")
            }
          }
        }
        TaskOccurrenceStatus.COMPLETED -> {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HeroGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "أحسنت! تم إنجاز المهمة بنجاح.",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
              color = HeroGreen
            )
          }
        }
        TaskOccurrenceStatus.SKIPPED -> {
          Text(
            text = "تم تخطي هذه المهمة اليوم بدون أي لوم، غدًا يوم جديد!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

// ==========================================
// --- General Deed Card ---
// ==========================================

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
