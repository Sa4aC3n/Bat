package com.batal.elyoum.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batal.elyoum.data.AgeGroup
import com.batal.elyoum.data.AvatarPresets
import com.batal.elyoum.data.ChildProfileEntity
import com.batal.elyoum.data.ParentTaskEntity
import com.batal.elyoum.data.RecurrenceType
import com.batal.elyoum.data.TaskOccurrenceEntity
import com.batal.elyoum.ui.HeroViewModel
import com.batal.elyoum.ui.theme.HeroGold
import com.batal.elyoum.ui.theme.HeroGoldDark
import com.batal.elyoum.ui.theme.HeroGoldLight
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ParentSectionTab(val title: String) {
  CHILDREN("الأطفال"),
  TASKS("إدارة المهام"),
  APPROVALS("طلبات الاعتماد"),
  SECURITY("الأمان والرمز")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentsSectionScreen(
  viewModel: HeroViewModel,
  onNavigateBack: () -> Unit
) {
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val tabs = ParentSectionTab.values()

  val activeChildren by viewModel.activeChildren.collectAsState()
  val allTasks by viewModel.allTasks.collectAsState()
  val pendingApprovals by viewModel.pendingApprovalOccurrences.collectAsState()

  // State for Dialogs
  var showAddChildDialog by remember { mutableStateOf(false) }
  var childToEdit by remember { mutableStateOf<ChildProfileEntity?>(null) }
  var showAddTaskDialog by remember { mutableStateOf(false) }
  var taskToEdit by remember { mutableStateOf<ParentTaskEntity?>(null) }
  var occurrenceToRetry by remember { mutableStateOf<TaskOccurrenceEntity?>(null) }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = "قسم الوالدين",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("parent_back_btn")
          ) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = "رجوع"
            )
          }
        },
        actions = {
          IconButton(
            onClick = {
              viewModel.lockParentSession()
              onNavigateBack()
            },
            modifier = Modifier.testTag("parent_lock_session_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "قفل الجلسة",
              tint = HeroGoldDark
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
      ) {
        tabs.forEachIndexed { index, tab ->
          val isSelected = selectedTabIndex == index
          Tab(
            selected = isSelected,
            onClick = { selectedTabIndex = index },
            modifier = Modifier.testTag("parent_tab_${tab.name.lowercase()}"),
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = tab.title,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  ),
                  color = if (isSelected) HeroGoldDark else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (tab == ParentSectionTab.APPROVALS && pendingApprovals.isNotEmpty()) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Box(
                    modifier = Modifier
                      .size(18.dp)
                      .clip(CircleShape)
                      .background(HeroGoldDark),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "${pendingApprovals.size}",
                      color = Color.White,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }
          )
        }
      }

      Box(modifier = Modifier.fillMaxSize()) {
        when (tabs[selectedTabIndex]) {
          ParentSectionTab.CHILDREN -> {
            ChildrenTabContent(
              children = activeChildren,
              onAddClick = { showAddChildDialog = true },
              onEditClick = { childToEdit = it },
              onArchiveClick = { viewModel.archiveChild(it.id) }
            )
          }
          ParentSectionTab.TASKS -> {
            TasksTabContent(
              tasks = allTasks,
              children = activeChildren,
              onAddTaskClick = { showAddTaskDialog = true },
              onEditTaskClick = { taskToEdit = it },
              onArchiveTaskClick = { viewModel.archiveParentTask(it.id) }
            )
          }
          ParentSectionTab.APPROVALS -> {
            ApprovalsTabContent(
              pendingApprovals = pendingApprovals,
              children = activeChildren,
              onApprove = { viewModel.approveTaskOccurrence(it.id) },
              onRetry = { occurrenceToRetry = it }
            )
          }
          ParentSectionTab.SECURITY -> {
            SecurityTabContent(
              viewModel = viewModel,
              onSessionLock = {
                viewModel.lockParentSession()
                onNavigateBack()
              }
            )
          }
        }
      }
    }
  }

  // --- Add / Edit Child Dialog ---
  if (showAddChildDialog || childToEdit != null) {
    ChildEditorDialog(
      initialChild = childToEdit,
      onDismiss = {
        showAddChildDialog = false
        childToEdit = null
      },
      onSave = { alias, ageGroup, avatarId ->
        if (childToEdit == null) {
          viewModel.createChild(alias, ageGroup, avatarId)
        } else {
          viewModel.updateChild(childToEdit!!.id, alias, ageGroup, avatarId)
        }
        showAddChildDialog = false
        childToEdit = null
      }
    )
  }

  // --- Add / Edit Task Dialog ---
  if (showAddTaskDialog || taskToEdit != null) {
    TaskEditorDialog(
      viewModel = viewModel,
      initialTask = taskToEdit,
      children = activeChildren,
      onDismiss = {
        showAddTaskDialog = false
        taskToEdit = null
      },
      onSave = { title, desc, reqApproval, recurrence, days, assignedKids, startDate ->
        if (taskToEdit == null) {
          viewModel.createParentTask(title, desc, reqApproval, recurrence, days, assignedKids, startDate)
        } else {
          viewModel.updateParentTask(taskToEdit!!.id, title, desc, reqApproval, recurrence, days, assignedKids, startDate)
        }
        showAddTaskDialog = false
        taskToEdit = null
      }
    )
  }

  // --- Retry Occurrence Dialog with gentle feedback note ---
  occurrenceToRetry?.let { occ ->
    var gentleNote by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { occurrenceToRetry = null },
      title = {
        Text("طلب إعادة المحاولة بلطف", style = MaterialTheme.typography.titleMedium)
      },
      text = {
        Column {
          Text(
            text = "المهمة: ${occ.snapshotTitle}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "ستعود المهمة لحالة «لم تبدأ» لدى الطفل مع عبارة تشجيعية، حتى يتمكن من إتمامها براحة.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = gentleNote,
            onValueChange = { gentleNote = it },
            label = { Text("ملاحظة تشجيعية لطيفة (اختياري)") },
            placeholder = { Text("مثال: محاولة رائعة، تعال نرتب الكتب سويًا") },
            modifier = Modifier.fillMaxWidth().testTag("retry_gentle_note_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.retryTaskOccurrence(occ.id, gentleNote)
            occurrenceToRetry = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = HeroGoldDark),
          modifier = Modifier.testTag("confirm_retry_btn")
        ) {
          Text("نحاول مرة تانية")
        }
      },
      dismissButton = {
        TextButton(onClick = { occurrenceToRetry = null }) {
          Text("إلغاء")
        }
      }
    )
  }
}

// ==========================================
// --- Tab 1: Children Profiles ---
// ==========================================

@Composable
private fun ChildrenTabContent(
  children: List<ChildProfileEntity>,
  onAddClick: () -> Unit,
  onEditClick: (ChildProfileEntity) -> Unit,
  onArchiveClick: (ChildProfileEntity) -> Unit
) {
  Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    if (children.isEmpty()) {
      Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.ChildCare,
          contentDescription = null,
          tint = HeroGoldDark,
          modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "لم تتم إضافة أي أطفال بعد",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "أضف أبطالك الصغار لتخصيص مهامهم اليومية ومتابعة إنجازاتهم بأمان وخصوصية.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = onAddClick,
          colors = ButtonDefaults.buttonColors(containerColor = HeroGoldDark),
          modifier = Modifier.testTag("add_first_child_btn")
        ) {
          Icon(Icons.Default.Add, contentDescription = null)
          Spacer(modifier = Modifier.width(6.dp))
          Text("إضافة طفل")
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize().padding(bottom = 70.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(children, key = { it.id }) { child ->
          ChildProfileCard(
            child = child,
            onEdit = { onEditClick(child) },
            onArchive = { onArchiveClick(child) }
          )
        }
      }
    }

    FloatingActionButton(
      onClick = onAddClick,
      containerColor = HeroGoldDark,
      contentColor = Color.White,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .testTag("add_child_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = "إضافة طفل")
    }
  }
}

@Composable
private fun ChildProfileCard(
  child: ChildProfileEntity,
  onEdit: () -> Unit,
  onArchive: () -> Unit
) {
  val ageGroup = AgeGroup.fromCode(child.ageGroup)
  var showArchiveConfirm by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier.fillMaxWidth().testTag("child_card_${child.id}"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    shape = RoundedCornerShape(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Avatar placeholder or preset
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(CircleShape)
          .background(HeroGold.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = HeroGoldDark,
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = child.alias,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = ageGroup.label,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_child_${child.id}")) {
        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = HeroGoldDark)
      }

      IconButton(onClick = { showArchiveConfirm = true }, modifier = Modifier.testTag("archive_child_${child.id}")) {
        Icon(Icons.Default.Archive, contentDescription = "أرشفة", tint = MaterialTheme.colorScheme.outline)
      }
    }
  }

  if (showArchiveConfirm) {
    AlertDialog(
      onDismissRequest = { showArchiveConfirm = false },
      title = { Text("أرشفة ملف الطفل") },
      text = {
        Text("هل تريد أرشفة ملف «${child.alias}»؟ ستبقى إنجازاته السابقة محفوظة في السجل، لكنه لن يظهر في قائمة الأبطال النشطين.")
      },
      confirmButton = {
        Button(
          onClick = {
            showArchiveConfirm = false
            onArchive()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("تأكيد الأرشفة")
        }
      },
      dismissButton = {
        TextButton(onClick = { showArchiveConfirm = false }) {
          Text("إلغاء")
        }
      }
    )
  }
}

// ==========================================
// --- Tab 2: Task Management ---
// ==========================================

@Composable
private fun TasksTabContent(
  tasks: List<ParentTaskEntity>,
  children: List<ChildProfileEntity>,
  onAddTaskClick: () -> Unit,
  onEditTaskClick: (ParentTaskEntity) -> Unit,
  onArchiveTaskClick: (ParentTaskEntity) -> Unit
) {
  Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    if (tasks.isEmpty()) {
      Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.Assignment,
          contentDescription = null,
          tint = HeroGoldDark,
          modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "لم يتم إنشاء أي مهام بعد",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "أنشئ مهامًا مخصصة لأطفالك (مثل ترتيب الغرفة، القراءة، مساعدة الإخوة) وحدد وتيرة تكرارها.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = onAddTaskClick,
          colors = ButtonDefaults.buttonColors(containerColor = HeroGoldDark),
          modifier = Modifier.testTag("add_first_task_btn")
        ) {
          Icon(Icons.Default.Add, contentDescription = null)
          Spacer(modifier = Modifier.width(6.dp))
          Text("إنشاء مهمة")
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize().padding(bottom = 70.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(tasks, key = { it.id }) { task ->
          ParentTaskCard(
            task = task,
            onEdit = { onEditTaskClick(task) },
            onArchive = { onArchiveTaskClick(task) }
          )
        }
      }
    }

    FloatingActionButton(
      onClick = onAddTaskClick,
      containerColor = HeroGoldDark,
      contentColor = Color.White,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .testTag("add_task_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = "إنشاء مهمة")
    }
  }
}

@Composable
private fun ParentTaskCard(
  task: ParentTaskEntity,
  onEdit: () -> Unit,
  onArchive: () -> Unit
) {
  var showArchiveConfirm by remember { mutableStateOf(false) }
  val recurrence = RecurrenceType.fromCode(task.recurrenceType)

  Card(
    modifier = Modifier.fillMaxWidth().testTag("parent_task_card_${task.id}"),
    colors = CardDefaults.cardColors(
      containerColor = if (task.isArchived) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = task.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          if (task.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = task.description,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        if (!task.isArchived) {
          IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_task_${task.id}")) {
            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = HeroGoldDark)
          }
          IconButton(onClick = { showArchiveConfirm = true }, modifier = Modifier.testTag("archive_task_${task.id}")) {
            Icon(Icons.Default.Archive, contentDescription = "أرشفة", tint = MaterialTheme.colorScheme.outline)
          }
        } else {
          Text(
            text = "مؤرشفة",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 8.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Recurrence badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(HeroGold.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = recurrence.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = HeroGoldDark
          )
        }

        // Requires approval badge
        if (task.requiresApproval) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "تتطلب موافقة الوالدين",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }
  }

  if (showArchiveConfirm) {
    AlertDialog(
      onDismissRequest = { showArchiveConfirm = false },
      title = { Text("أرشفة المهمة") },
      text = {
        Text("هل تريد أرشفة مهمة «${task.title}»؟ لن تظهر في الأيام القادمة، ويبقى سجل إنجازاتها السابقة محفوظًا بدقة.")
      },
      confirmButton = {
        Button(
          onClick = {
            showArchiveConfirm = false
            onArchive()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("تأكيد الأرشفة")
        }
      },
      dismissButton = {
        TextButton(onClick = { showArchiveConfirm = false }) {
          Text("إلغاء")
        }
      }
    )
  }
}

// ==========================================
// --- Tab 3: Pending Approvals ---
// ==========================================

@Composable
private fun ApprovalsTabContent(
  pendingApprovals: List<TaskOccurrenceEntity>,
  children: List<ChildProfileEntity>,
  onApprove: (TaskOccurrenceEntity) -> Unit,
  onRetry: (TaskOccurrenceEntity) -> Unit
) {
  if (pendingApprovals.isEmpty()) {
    Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = Icons.Default.FactCheck,
        contentDescription = null,
        tint = HeroGoldDark,
        modifier = Modifier.size(64.dp)
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "لا توجد مهام بانتظار الاعتماد",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "عندما يُتم أحد الأطفال مهمة تتطلب تأكيدًا، ستظهر هنا لمراجعتها وتشجيعه.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
    }
  } else {
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(pendingApprovals, key = { it.id }) { occ ->
        val child = children.find { it.id == occ.childId }
        val childAlias = child?.alias ?: "البطل الصغير"

        Card(
          modifier = Modifier.fillMaxWidth().testTag("pending_approval_card_${occ.id}"),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(HeroGold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = HeroGoldDark)
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = childAlias,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "تاريخ المهمة: ${occ.dateStr}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = occ.snapshotTitle,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            if (occ.snapshotDescription.isNotBlank()) {
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = occ.snapshotDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = { onApprove(occ) },
                colors = ButtonDefaults.buttonColors(containerColor = HeroGoldDark),
                modifier = Modifier.weight(1f).testTag("approve_task_btn_${occ.id}")
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تأكيد الإنجاز")
              }

              OutlinedButton(
                onClick = { onRetry(occ) },
                modifier = Modifier.weight(1f).testTag("retry_task_btn_${occ.id}")
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("نحاول مرة تانية")
              }
            }
          }
        }
      }
    }
  }
}

// ==========================================
// --- Tab 4: Security Settings ---
// ==========================================

@Composable
private fun SecurityTabContent(
  viewModel: HeroViewModel,
  onSessionLock: () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  var showChangePinDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Security, contentDescription = null, tint = HeroGoldDark)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "الأمان وحماية الخصوصية",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "• رمز المرور مكون من ٦ أرقام ويتم تشفيره محليًا باستخدام تجزئة مملحة آمنة (Salted Hash).\n" +
              "• لا يتم إرسال أي بيانات أو أسماء أو رمز دخول إلى الإنترنت أو أي خوادم خارجية.\n" +
              "• يتم تفعيل قفل تدريجي عند تكرار المحاولات الخاطئة لحماية إعدادات الأسرة.\n" +
              "• يُقفل قسم الوالدين تلقائيًا بمجرد خروج التطبيق إلى الخلفية.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Button(
      onClick = { showChangePinDialog = true },
      colors = ButtonDefaults.buttonColors(containerColor = HeroGoldDark),
      modifier = Modifier.fillMaxWidth().testTag("change_pin_btn")
    ) {
      Icon(Icons.Default.LockReset, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("تغيير رمز المرور (٦ أرقام)")
    }

    OutlinedButton(
      onClick = onSessionLock,
      modifier = Modifier.fillMaxWidth().testTag("lock_now_btn")
    ) {
      Icon(Icons.Default.Lock, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("قفل قسم الوالدين الآن")
    }
  }

  if (showChangePinDialog) {
    ChangePinDialog(
      viewModel = viewModel,
      onDismiss = { showChangePinDialog = false }
    )
  }
}

@Composable
private fun ChangePinDialog(
  viewModel: HeroViewModel,
  onDismiss: () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  var currentPin by remember { mutableStateOf("") }
  var newPin by remember { mutableStateOf("") }
  var confirmPin by remember { mutableStateOf("") }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var successMsg by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("تغيير رمز المرور") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = currentPin,
          onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) currentPin = it },
          label = { Text("الرمز الحالي (٦ أرقام)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth().testTag("current_pin_input")
        )

        OutlinedTextField(
          value = newPin,
          onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newPin = it },
          label = { Text("الرمز الجديد (٦ أرقام)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth().testTag("new_pin_input")
        )

        OutlinedTextField(
          value = confirmPin,
          onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) confirmPin = it },
          label = { Text("تأكيد الرمز الجديد") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth().testTag("confirm_pin_input")
        )

        if (errorMsg != null) {
          Text(text = errorMsg ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        if (successMsg != null) {
          Text(text = successMsg ?: "", color = HeroGoldDark, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (currentPin.length != 6 || newPin.length != 6) {
            errorMsg = "يجب أن يتكون الرمز من ٦ أرقام بالضبط"
            return@Button
          }
          if (newPin != confirmPin) {
            errorMsg = "الرمز الجديد وتأكيده غير متطابقين"
            return@Button
          }
          coroutineScope.launch {
            val ok = viewModel.changePin(currentPin, newPin)
            if (ok) {
              successMsg = "تم تغيير الرمز بنجاح!"
              errorMsg = null
              kotlinx.coroutines.delay(1000)
              onDismiss()
            } else {
              errorMsg = "الرمز الحالي غير صحيح"
            }
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = HeroGoldDark),
        modifier = Modifier.testTag("save_pin_change_btn")
      ) {
        Text("حفظ التغيير")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("إلغاء") }
    }
  )
}

// ==========================================
// --- Dialogs: Child & Task Editors ---
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChildEditorDialog(
  initialChild: ChildProfileEntity?,
  onDismiss: () -> Unit,
  onSave: (alias: String, ageGroup: AgeGroup, avatarId: String) -> Unit
) {
  var alias by remember { mutableStateOf(initialChild?.alias ?: "") }
  var selectedAgeGroup by remember {
    mutableStateOf(initialChild?.let { AgeGroup.fromCode(it.ageGroup) } ?: AgeGroup.AGE_7_9)
  }
  var selectedAvatarId by remember { mutableStateOf(initialChild?.avatarId ?: AvatarPresets.PRESETS[0].id) }
  var errorText by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(if (initialChild == null) "إضافة بطل صغير" else "تعديل ملف البطل")
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "لا يُطلب الاسم الحقيقي أو أي بيانات شخصية، فقط اسم مستعار محبب.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
          value = alias,
          onValueChange = {
            alias = it
            errorText = null
          },
          label = { Text("الاسم المستعار (مثال: أحمد، بطل القراءة)") },
          modifier = Modifier.fillMaxWidth().testTag("child_alias_input")
        )

        if (errorText != null) {
          Text(text = errorText ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "الفئة العمرية:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

        AgeGroup.values().forEach { ag ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedAgeGroup = ag }
              .padding(vertical = 4.dp)
          ) {
            Checkbox(
              checked = selectedAgeGroup == ag,
              onCheckedChange = { selectedAgeGroup = ag },
              colors = CheckboxDefaults.colors(checkedColor = HeroGoldDark)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = ag.label, style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (alias.isBlank()) {
            errorText = "يرجى كتابة الاسم المستعار"
            return@Button
          }
          onSave(alias.trim(), selectedAgeGroup, selectedAvatarId)
        },
        colors = ButtonDefaults.buttonColors(containerColor = HeroGoldDark),
        modifier = Modifier.testTag("save_child_btn")
      ) {
        Text("حفظ")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("إلغاء") }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TaskEditorDialog(
  viewModel: HeroViewModel,
  initialTask: ParentTaskEntity?,
  children: List<ChildProfileEntity>,
  onDismiss: () -> Unit,
  onSave: (
    title: String,
    description: String,
    requiresApproval: Boolean,
    recurrence: RecurrenceType,
    days: List<Int>,
    assignedKids: List<String>,
    startDate: String
  ) -> Unit
) {
  val defaultDate = remember {
    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
  }
  val coroutineScope = rememberCoroutineScope()
  var title by remember { mutableStateOf(initialTask?.title ?: "") }
  var description by remember { mutableStateOf(initialTask?.description ?: "") }
  var requiresApproval by remember { mutableStateOf(initialTask?.requiresApproval ?: false) }
  var startDate by remember { mutableStateOf(initialTask?.startDate ?: defaultDate) }
  var recurrence by remember {
    mutableStateOf(initialTask?.let { RecurrenceType.fromCode(it.recurrenceType) } ?: RecurrenceType.DAILY)
  }
  val assignedKids = remember {
    mutableStateListOf<String>().apply {
      if (initialTask == null) {
        // By default assign to all active children
        addAll(children.map { it.id })
      }
    }
  }

  // Load existing assigned kids on edit
  if (initialTask != null) {
    androidx.compose.runtime.LaunchedEffect(initialTask.id) {
      val ids = viewModel.getAssignedChildIds(initialTask.id)
      assignedKids.clear()
      assignedKids.addAll(ids)
    }
  }

  var errorText by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(if (initialTask == null) "إنشاء مهمة جديدة" else "تعديل المهمة")
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = {
            title = it
            errorText = null
          },
          label = { Text("عنوان المهمة (مثال: ترتيب السرير)") },
          modifier = Modifier.fillMaxWidth().testTag("task_title_input")
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("وصف تشجيعي لطيف") },
          placeholder = { Text("مثال: رتّب غطائك ووسادتك لبدء يوم مريح") },
          modifier = Modifier.fillMaxWidth().testTag("task_desc_input")
        )

        OutlinedTextField(
          value = startDate,
          onValueChange = { startDate = it },
          label = { Text("تاريخ بدء المهمة (YYYY-MM-DD)") },
          placeholder = { Text(defaultDate) },
          modifier = Modifier.fillMaxWidth().testTag("task_start_date_input")
        )

        if (errorText != null) {
          Text(text = errorText ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "تكرار المهمة:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

        RecurrenceType.values().forEach { r ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { recurrence = r }
              .padding(vertical = 2.dp)
          ) {
            Checkbox(
              checked = recurrence == r,
              onCheckedChange = { recurrence = r },
              colors = CheckboxDefaults.colors(checkedColor = HeroGoldDark)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = r.label, style = MaterialTheme.typography.bodyMedium)
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Requires approval toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "تتطلب تأكيد ولي الأمر",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "لا تُعتبر مكتملة حتى يؤكدها أحد الوالدين",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = requiresApproval,
            onCheckedChange = { requiresApproval = it },
            colors = SwitchDefaults.colors(checkedThumbColor = HeroGoldDark),
            modifier = Modifier.testTag("task_requires_approval_switch")
          )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "تعيين المهمة للأبطال:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

        if (children.isEmpty()) {
          Text(
            text = "لا يوجد أطفال نشطون حاليًا. يرجى إضافة طفل أولاً.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
          )
        } else {
          children.forEach { child ->
            val isAssigned = assignedKids.contains(child.id)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  if (isAssigned) assignedKids.remove(child.id) else assignedKids.add(child.id)
                }
                .padding(vertical = 2.dp)
            ) {
              Checkbox(
                checked = isAssigned,
                onCheckedChange = { checked ->
                  if (checked) assignedKids.add(child.id) else assignedKids.remove(child.id)
                },
                colors = CheckboxDefaults.colors(checkedColor = HeroGoldDark)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(text = child.alias, style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isBlank()) {
            errorText = "يرجى كتابة عنوان المهمة"
            return@Button
          }
          if (assignedKids.isEmpty()) {
            errorText = "يرجى اختيار طفل واحد على الأقل للمهمة"
            return@Button
          }
          val cleanDate = if (startDate.isBlank()) defaultDate else startDate.trim()
          onSave(
            title.trim(),
            description.trim(),
            requiresApproval,
            recurrence,
            emptyList(),
            assignedKids.toList(),
            cleanDate
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = HeroGoldDark),
        modifier = Modifier.testTag("save_task_btn")
      ) {
        Text("حفظ المهمة")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("إلغاء") }
    }
  )
}
