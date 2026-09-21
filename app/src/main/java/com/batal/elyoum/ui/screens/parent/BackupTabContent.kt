package com.batal.elyoum.ui.screens.parent

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batal.elyoum.data.HeroRepository
import com.batal.elyoum.ui.HeroViewModel
import com.batal.elyoum.ui.theme.HeroGold
import com.batal.elyoum.ui.theme.HeroGoldDark
import com.batal.elyoum.ui.theme.HeroGreen

@Composable
fun BackupTabContent(
  viewModel: HeroViewModel,
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current

  // Export State
  var exportPassword by remember { mutableStateOf("") }
  var exportPasswordConfirm by remember { mutableStateOf("") }
  var exportedData by remember { mutableStateOf<String?>(null) }
  var exportError by remember { mutableStateOf<String?>(null) }
  var exportCopied by remember { mutableStateOf(false) }

  // Import State
  var importPayload by remember { mutableStateOf("") }
  var importPassword by remember { mutableStateOf("") }
  var previewInfo by remember { mutableStateOf<HeroRepository.BackupPreviewInfo?>(null) }
  var importStatusMessage by remember { mutableStateOf<String?>(null) }
  var importErrorMessage by remember { mutableStateOf<String?>(null) }

  // Delete All State
  var showDeleteAllDialog by remember { mutableStateOf(false) }
  var deleteConfirmationWord by remember { mutableStateOf("") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(10.dp))
      // Privacy & Encryption Philosophy Card
      Card(
        modifier = Modifier.fillMaxWidth().testTag("backup_privacy_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HeroGold.copy(alpha = 0.1f))
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(Icons.Default.Security, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "حماية البيانات والتشفير المحلي (AES-256)",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = HeroGoldDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "بيانات أسرتك وأطفالك محفوظة محلياً على جهازك بالكامل دون إرسالها لأي خادم خارجي. يمكنك تصدير نسخة مشفرة بكلمة مرور خاصة بك لاستعادتها وقت الحاجة.",
              style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // SECTION: EXPORT
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("export_backup_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "تصدير نسخة احتياطية مشفرة",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          Text(
            text = "اختر كلمة مرور قوية لتشفير ملف النسخة الاحتياطية. احرص على تذكرها، فلن يمكن فك التشفير بدونها.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedTextField(
            value = exportPassword,
            onValueChange = {
              exportPassword = it
              exportError = null
            },
            label = { Text("كلمة مرور النسخة الاحتياطية (4 خانات فأكثر)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("export_password_input"),
            singleLine = true
          )

          OutlinedTextField(
            value = exportPasswordConfirm,
            onValueChange = {
              exportPasswordConfirm = it
              exportError = null
            },
            label = { Text("تأكيد كلمة المرور") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("export_password_confirm_input"),
            singleLine = true
          )

          if (exportError != null) {
            Text(
              text = exportError!!,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error
            )
          }

          Button(
            onClick = {
              if (exportPassword.length < 4) {
                exportError = "كلمة المرور يجب ألا تقل عن 4 خانات"
                return@Button
              }
              if (exportPassword != exportPasswordConfirm) {
                exportError = "كلمتا المرور غير متطابقتين"
                return@Button
              }
              exportError = null
              viewModel.exportBackup(
                password = exportPassword,
                onSuccess = { payload ->
                  exportedData = payload
                  exportCopied = false
                },
                onError = { err -> exportError = err }
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = HeroGoldDark),
            modifier = Modifier.fillMaxWidth().testTag("generate_export_btn")
          ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("إنشاء النسخة المشفرة الآن")
          }

          if (exportedData != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(HeroGold.copy(alpha = 0.15f))
                .padding(12.dp)
            ) {
              Column {
                Text(
                  text = "تم إنشاء النسخة المشفرة بنجاح بنظام AES-256-GCM!",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                  color = HeroGoldDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                  onClick = {
                    clipboardManager.setText(AnnotatedString(exportedData!!))
                    exportCopied = true
                  },
                  modifier = Modifier.fillMaxWidth().testTag("copy_backup_btn")
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(if (exportCopied) "تم نسخ النسخة إلى الحافظة! ✓" else "نسخ النص المشفر إلى الحافظة")
                }
              }
            }
          }
        }
      }
    }

    // SECTION: IMPORT / RESTORE
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("import_backup_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = HeroGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "استعادة البيانات من نسخة احتياطية",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          Text(
            text = "الصق نص النسخة الاحتياطية المشفر وأدخل كلمة المرور لمعاينتها ثم استعادتها بأمان.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedTextField(
            value = importPayload,
            onValueChange = {
              importPayload = it
              importErrorMessage = null
              importStatusMessage = null
              previewInfo = null
            },
            label = { Text("النص المشفر للنسخة الاحتياطية") },
            modifier = Modifier.fillMaxWidth().testTag("import_payload_input"),
            maxLines = 4
          )

          OutlinedTextField(
            value = importPassword,
            onValueChange = {
              importPassword = it
              importErrorMessage = null
              importStatusMessage = null
            },
            label = { Text("كلمة مرور النسخة الاحتياطية") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("import_password_input"),
            singleLine = true
          )

          if (importErrorMessage != null) {
            Text(
              text = importErrorMessage!!,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error
            )
          }

          if (importStatusMessage != null) {
            Text(
              text = importStatusMessage!!,
              style = MaterialTheme.typography.bodySmall,
              color = HeroGreen
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                if (importPayload.isBlank() || importPassword.isBlank()) {
                  importErrorMessage = "يرجى لصق النص وإدخال كلمة المرور"
                  return@OutlinedButton
                }
                viewModel.previewBackup(
                  encryptedJson = importPayload.trim(),
                  password = importPassword,
                  onPreview = { preview ->
                    previewInfo = preview
                    importErrorMessage = null
                  },
                  onError = { err ->
                    importErrorMessage = err
                    previewInfo = null
                  }
                )
              },
              modifier = Modifier.weight(1f).testTag("preview_backup_btn")
            ) {
              Text("معاينة المحتوى")
            }

            Button(
              onClick = {
                if (importPayload.isBlank() || importPassword.isBlank()) {
                  importErrorMessage = "يرجى لصق النص وإدخال كلمة المرور"
                  return@Button
                }
                viewModel.restoreBackup(
                  encryptedJson = importPayload.trim(),
                  password = importPassword,
                  onSuccess = {
                    importStatusMessage = "تمت استعادة كافة البيانات الأسرية بنجاح وتحديث المهام!"
                    importErrorMessage = null
                    previewInfo = null
                    importPayload = ""
                    importPassword = ""
                  },
                  onError = { err ->
                    importErrorMessage = err
                  }
                )
              },
              colors = ButtonDefaults.buttonColors(containerColor = HeroGreen),
              modifier = Modifier.weight(1.2f).testTag("confirm_restore_btn")
            ) {
              Text("استعادة بأمان")
            }
          }

          // Preview card if loaded
          if (previewInfo != null) {
            val p = previewInfo!!
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "محتويات النسخة الاحتياطية:",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(text = "• عدد ملفات الأطفال: ${p.childCount}", style = MaterialTheme.typography.bodySmall)
                Text(text = "• عدد المهام المسجلة: ${p.taskCount}", style = MaterialTheme.typography.bodySmall)
                Text(text = "• سجلات الإنجازات والتنفيذ: ${p.occurrenceCount}", style = MaterialTheme.typography.bodySmall)
                Text(text = "• المكافآت واللحظات الأسرية: ${p.rewardCount}", style = MaterialTheme.typography.bodySmall)
                if (p.createdAt.isNotBlank()) {
                  Text(text = "• تاريخ تصدير النسخة: ${p.createdAt}", style = MaterialTheme.typography.labelSmall)
                }
              }
            }
          }
        }
      }
    }

    // SECTION: DATA DELETION
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("delete_data_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "حذف كافة البيانات وإعادة الضبط",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.error
            )
          }

          Text(
            text = "يمكّنك هذا الخيار من مسح كافة ملفات الأطفال، والمهام، وسجلات الإنجازات نهائياً من الجهاز مع الحفاظ على رمز أمان الوالدين.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedButton(
            onClick = {
              deleteConfirmationWord = ""
              showDeleteAllDialog = true
            },
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
              contentColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth().testTag("open_delete_all_dialog_btn")
          ) {
            Text("حذف جميع البيانات الأسرية...")
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(30.dp))
    }
  }

  // Delete All Dialog
  if (showDeleteAllDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteAllDialog = false },
      title = { Text("تأكيد مسح كافة البيانات نهائياً") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "هذا الإجراء سيحذف كافة ملفات الأطفال والمهام والذكريات المسجلة ولا يمكن التراجع عنه إلا بوجود نسخة احتياطية.",
            style = MaterialTheme.typography.bodyMedium
          )
          Text(
            text = "للتأكيد، اكتب كلمة «حذف» في الحقل التالي:",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
          )
          OutlinedTextField(
            value = deleteConfirmationWord,
            onValueChange = { deleteConfirmationWord = it },
            placeholder = { Text("حذف") },
            modifier = Modifier.fillMaxWidth().testTag("delete_confirmation_word_input"),
            singleLine = true
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (deleteConfirmationWord.trim() == "حذف") {
              viewModel.deleteAllFamilyData {
                showDeleteAllDialog = false
              }
            }
          },
          enabled = deleteConfirmationWord.trim() == "حذف",
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_delete_all_btn")
        ) {
          Text("تأكيد الحذف النهائي")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteAllDialog = false }) {
          Text("إلغاء")
        }
      }
    )
  }
}
