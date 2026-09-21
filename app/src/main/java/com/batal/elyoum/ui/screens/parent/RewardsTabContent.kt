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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batal.elyoum.data.ChildProfileEntity
import com.batal.elyoum.data.FamilyRewardEntity
import com.batal.elyoum.data.RewardGrantMode
import com.batal.elyoum.data.RewardStatus
import com.batal.elyoum.data.RewardType
import com.batal.elyoum.ui.HeroViewModel
import com.batal.elyoum.ui.theme.HeroGold
import com.batal.elyoum.ui.theme.HeroGoldDark
import com.batal.elyoum.ui.theme.HeroGoldLight
import com.batal.elyoum.ui.theme.HeroGreen
import com.batal.elyoum.ui.theme.HeroGreenContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RewardsTabContent(
  viewModel: HeroViewModel,
  children: List<ChildProfileEntity>,
  modifier: Modifier = Modifier
) {
  val selectedChild by viewModel.selectedChild.collectAsState()
  val childRewards by viewModel.childRewards.collectAsState()

  var showAddDialog by remember { mutableStateOf(false) }
  var rewardToFulfill by remember { mutableStateOf<FamilyRewardEntity?>(null) }
  var fulfillmentNote by remember { mutableStateOf("") }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(10.dp))
        // Guidance card
        Card(
          modifier = Modifier.fillMaxWidth().testTag("rewards_guidance_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = HeroGold.copy(alpha = 0.1f)
          )
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "مكافآت ولحظات الأسرة",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = HeroGoldDark
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "المكافآت هنا تجارب مشتركة ولحظات نوعية تعزز الأثر الإيجابي. لا تربط العبادات بمقابل مادي، ولا تشترط نسبة 100% لتشجيع الطفل.",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // Child selector filter
      if (children.isNotEmpty()) {
        item {
          Column {
            Text(
              text = "اختر الطفل:",
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
                  modifier = Modifier.testTag("reward_filter_child_${child.id}")
                )
              }
            }
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
              text = "يرجى إضافة ملف طفل أولاً من تبويب «الأطفال» لتخصيص المكافآت واللحظات الأسرية له.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth().padding(24.dp)
            )
          }
        }
      } else if (childRewards.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag("no_rewards_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(40.dp))
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "لا توجد مكافآت أو لحظات مسجلة لـ «${selectedChild?.alias ?: ""}» حالياً.",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "اضغط على زر الإضافة بالأسفل لإنشاء نشاط عائلي مميز أو تجربة مشتركة تبهج قلبه.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      } else {
        items(childRewards, key = { it.id }) { reward ->
          ParentRewardItemCard(
            reward = reward,
            onMakeAvailable = { viewModel.makeRewardAvailable(reward.id) },
            onFulfillClick = {
              fulfillmentNote = ""
              rewardToFulfill = reward
            },
            onCancelClick = {
              viewModel.cancelReward(reward.id, "إلغاء ودي بتوافق الأسرة")
            }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }

    // FAB to add reward
    if (children.isNotEmpty()) {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = HeroGoldDark,
        contentColor = Color.White,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(20.dp)
          .testTag("add_reward_fab")
      ) {
        Icon(Icons.Default.Add, contentDescription = "إضافة لحظة أو مكافأة")
      }
    }
  }

  // Dialog: Add Reward
  if (showAddDialog && selectedChild != null) {
    AddRewardDialog(
      child = selectedChild!!,
      onDismiss = { showAddDialog = false },
      onConfirm = { title, desc, type, grantMode, initialStatus ->
        viewModel.createFamilyReward(
          title = title,
          description = desc,
          rewardType = type,
          childId = selectedChild!!.id,
          grantMode = grantMode,
          initialStatus = initialStatus
        )
        showAddDialog = false
      }
    )
  }

  // Dialog: Fulfill Reward
  if (rewardToFulfill != null) {
    AlertDialog(
      onDismissRequest = { rewardToFulfill = null },
      title = { Text("إتمام اللحظة الأسرية 🎉") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "هل تمت مشاركة «${rewardToFulfill!!.title}» مع البطل الصغير؟",
            style = MaterialTheme.typography.bodyMedium
          )
          OutlinedTextField(
            value = fulfillmentNote,
            onValueChange = { fulfillmentNote = it },
            label = { Text("ذكرى جميلة أو ملاحظة تشجيعية (اختياري)") },
            modifier = Modifier.fillMaxWidth().testTag("fulfillment_note_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.fulfillReward(
              rewardId = rewardToFulfill!!.id,
              note = fulfillmentNote.ifBlank { null }
            )
            rewardToFulfill = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = HeroGreen),
          modifier = Modifier.testTag("confirm_fulfill_btn")
        ) {
          Text("تأكيد الإتمام المبارك")
        }
      },
      dismissButton = {
        TextButton(onClick = { rewardToFulfill = null }) {
          Text("إلغاء")
        }
      }
    )
  }
}

@Composable
fun ParentRewardItemCard(
  reward: FamilyRewardEntity,
  onMakeAvailable: () -> Unit,
  onFulfillClick: () -> Unit,
  onCancelClick: () -> Unit
) {
  val status = RewardStatus.fromCode(reward.status)
  val type = RewardType.fromCode(reward.rewardType)

  Card(
    modifier = Modifier.fillMaxWidth().testTag("parent_reward_${reward.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = when (status) {
        RewardStatus.CLAIMED -> Color(0xFFEFF6FF)
        RewardStatus.FULFILLED -> HeroGreenContainer.copy(alpha = 0.35f)
        RewardStatus.AVAILABLE -> HeroGold.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surface
      }
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(HeroGold.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when (type) {
                RewardType.SHARED_ACTIVITY -> Icons.Default.VolunteerActivism
                RewardType.SPECIAL_TIME -> Icons.Default.Star
                RewardType.NEW_EXPERIENCE -> Icons.Default.Explore
                RewardType.TOKEN_GIFT -> Icons.Default.WorkspacePremium
              },
              contentDescription = null,
              tint = HeroGoldDark,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = reward.title,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = type.label,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Status badge
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = when (status) {
            RewardStatus.AVAILABLE -> HeroGold.copy(alpha = 0.2f)
            RewardStatus.CLAIMED -> Color(0xFF2563EB).copy(alpha = 0.15f)
            RewardStatus.FULFILLED -> HeroGreen.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.surfaceVariant
          }
        ) {
          Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = when (status) {
              RewardStatus.AVAILABLE -> HeroGoldDark
              RewardStatus.CLAIMED -> Color(0xFF2563EB)
              RewardStatus.FULFILLED -> HeroGreen
              else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      if (reward.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = reward.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Actions based on state
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        when (status) {
          RewardStatus.PLANNED -> {
            Button(
              onClick = onMakeAvailable,
              colors = ButtonDefaults.buttonColors(containerColor = HeroGoldDark),
              modifier = Modifier.weight(1f).testTag("make_available_${reward.id}")
            ) {
              Text("إتاحة للطفل الآن")
            }
          }
          RewardStatus.CLAIMED -> {
            Button(
              onClick = onFulfillClick,
              colors = ButtonDefaults.buttonColors(containerColor = HeroGreen),
              modifier = Modifier.weight(1f).testTag("fulfill_btn_${reward.id}")
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("تم التنفيذ مع الطفل 🎉")
            }
          }
          RewardStatus.AVAILABLE -> {
            Text(
              text = "المكافأة ظاهرة في شاشة الطفل ليطلب تحقيقها في أي وقت.",
              style = MaterialTheme.typography.bodySmall,
              color = HeroGoldDark,
              modifier = Modifier.weight(1f)
            )
          }
          RewardStatus.FULFILLED -> {
            Text(
              text = "تمت مشاركة اللحظة بنجاح وتوثيق أثرها الطيب ❤️",
              style = MaterialTheme.typography.bodySmall,
              color = HeroGreen,
              modifier = Modifier.weight(1f)
            )
          }
          else -> {}
        }

        if (status != RewardStatus.FULFILLED && status != RewardStatus.CANCELLED) {
          TextButton(onClick = onCancelClick) {
            Text("إلغاء", color = MaterialTheme.colorScheme.error)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRewardDialog(
  child: ChildProfileEntity,
  onDismiss: () -> Unit,
  onConfirm: (String, String, RewardType, RewardGrantMode, RewardStatus) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var selectedType by remember { mutableStateOf(RewardType.SHARED_ACTIVITY) }
  var selectedGrantMode by remember { mutableStateOf(RewardGrantMode.UPON_CHILD_REQUEST) }
  var makeImmediatelyAvailable by remember { mutableStateOf(true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("إضافة لحظة أو مكافأة لـ «${child.alias}»") },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("عنوان اللحظة أو المكافأة *") },
          placeholder = { Text("مثال: جلسة حكايات خاصة، نزهة للدراجات") },
          modifier = Modifier.fillMaxWidth().testTag("reward_title_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("الوصف أو التفاصيل") },
          placeholder = { Text("مثال: وقت ممتع لمدة نصف ساعة لاختيار قصة مميزة وقراءتها معاً") },
          modifier = Modifier.fillMaxWidth().testTag("reward_desc_input"),
          maxLines = 3
        )

        Text(
          text = "نوع المكافأة التربوية:",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          RewardType.values().forEach { type ->
            FilterChip(
              selected = selectedType == type,
              onClick = { selectedType = type },
              label = { Text(type.label, fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = HeroGold.copy(alpha = 0.25f),
                selectedLabelColor = HeroGoldDark
              )
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          androidx.compose.material3.Checkbox(
            checked = makeImmediatelyAvailable,
            onCheckedChange = { makeImmediatelyAvailable = it }
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "إتاحة اللحظة فوراً في شاشة الطفل للطلب",
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            val initialStatus = if (makeImmediatelyAvailable) RewardStatus.AVAILABLE else RewardStatus.PLANNED
            onConfirm(title.trim(), description.trim(), selectedType, selectedGrantMode, initialStatus)
          }
        },
        enabled = title.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = HeroGoldDark),
        modifier = Modifier.testTag("confirm_add_reward_btn")
      ) {
        Text("حفظ اللحظة")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء")
      }
    }
  )
}
