package com.batal.elyoum.ui.components

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batal.elyoum.data.HeroRepository
import com.batal.elyoum.ui.HeroViewModel
import com.batal.elyoum.ui.theme.HeroGold
import com.batal.elyoum.ui.theme.HeroGoldDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ParentPinGateDialog(
  viewModel: HeroViewModel,
  onDismiss: () -> Unit,
  onSuccess: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var isConfigured by remember { mutableStateOf<Boolean?>(null) }
  var isDeviceAuthConfirmedForReset by remember { mutableStateOf(false) }
  var setupStep by remember { mutableIntStateOf(1) } // 1: Enter new PIN, 2: Confirm new PIN
  var initialPinAttempt by remember { mutableStateOf("") }
  var enteredPin by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var lockoutSeconds by remember { mutableIntStateOf(0) }
  var showResetDialog by remember { mutableStateOf(false) }

  // Check if configured
  LaunchedEffect(Unit) {
    isConfigured = viewModel.isPinConfigured()
  }

  // Handle countdown for lockout
  LaunchedEffect(lockoutSeconds) {
    if (lockoutSeconds > 0) {
      delay(1000)
      lockoutSeconds -= 1
    }
  }

  val isLockedOut = lockoutSeconds > 0

  val keyguardManager = remember {
    context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
  }
  val hasDeviceSecurity = remember {
    keyguardManager?.isDeviceSecure ?: false
  }

  val deviceAuthLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      isDeviceAuthConfirmedForReset = true
      showResetDialog = false
      setupStep = 1
      enteredPin = ""
      initialPinAttempt = ""
      errorMessage = null
    } else {
      errorMessage = "فشلت مصادقة أمان الجهاز. لم يتم تغيير الرمز."
    }
  }

  fun onKeyClick(digit: String) {
    if (isLockedOut) return
    if (enteredPin.length < 6) {
      enteredPin += digit
      errorMessage = null
      if (enteredPin.length == 6) {
        val pin = enteredPin
        val inSetupOrReset = (isConfigured == false) || isDeviceAuthConfirmedForReset
        if (inSetupOrReset) {
          // Setup or authenticated reset flow
          if (setupStep == 1) {
            initialPinAttempt = pin
            enteredPin = ""
            setupStep = 2
          } else {
            if (pin == initialPinAttempt) {
              coroutineScope.launch {
                val ok = if (isDeviceAuthConfirmedForReset) {
                  viewModel.resetPinWithDeviceAuth(pin, isDeviceAuthConfirmed = true)
                } else {
                  viewModel.setupInitialPin(pin)
                }
                if (ok) {
                  onSuccess()
                } else {
                  errorMessage = "حدث خطأ أثناء حفظ الرمز. حاول مجددًا."
                  setupStep = 1
                  enteredPin = ""
                }
              }
            } else {
              errorMessage = "الرمزان غير متطابقين. يرجى البدء من جديد."
              setupStep = 1
              initialPinAttempt = ""
              enteredPin = ""
            }
          }
        } else {
          // Verification flow
          coroutineScope.launch {
            when (val res = viewModel.verifyPin(pin)) {
              is HeroRepository.PinCheckResult.Success -> {
                onSuccess()
              }
              is HeroRepository.PinCheckResult.LockedOut -> {
                lockoutSeconds = res.secondsRemaining.toInt()
                enteredPin = ""
                errorMessage = "مغلق مؤقتًا لأسباب أمنية. انتظر $lockoutSeconds ثانية."
              }
              is HeroRepository.PinCheckResult.IncorrectPin -> {
                enteredPin = ""
                if (res.lockoutSecondsRemaining > 0) {
                  lockoutSeconds = res.lockoutSecondsRemaining.toInt()
                  errorMessage = "رمز غير صحيح! تم القفل مؤقتًا لمدة ${res.lockoutSecondsRemaining} ثانية."
                } else {
                  errorMessage = "رمز غير صحيح! (المحاولة ${res.failedAttempts})"
                }
              }
              is HeroRepository.PinCheckResult.NotConfigured -> {
                isConfigured = false
                setupStep = 1
                enteredPin = ""
              }
            }
          }
        }
      }
    }
  }

  fun onDeleteClick() {
    if (enteredPin.isNotEmpty() && !isLockedOut) {
      enteredPin = enteredPin.dropLast(1)
      errorMessage = null
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {},
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("pin_dialog_cancel")
      ) {
        Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = HeroGoldDark,
          modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = when {
            isDeviceAuthConfirmedForReset && setupStep == 1 -> "إعادة تعيين رمز الدخول (٦ أرقام)"
            isDeviceAuthConfirmedForReset && setupStep == 2 -> "تأكيد الرمز الجديد (٦ أرقام)"
            isConfigured == false && setupStep == 1 -> "تعيين رمز دخول للأهل (٦ أرقام)"
            isConfigured == false && setupStep == 2 -> "تأكيد رمز الدخول (٦ أرقام)"
            else -> "بوابة ولي الأمر"
          },
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = when {
            isDeviceAuthConfirmedForReset && setupStep == 1 -> "تم التحقق من أمان الجهاز بنجاح. أدخل الرمز الجديد المكون من ٦ أرقام."
            isDeviceAuthConfirmedForReset && setupStep == 2 -> "أعد كتابة الرمز الجديد للتأكيد."
            isConfigured == false && setupStep == 1 -> "قم بتعيين رمز سري من ٦ أرقام لحماية إعدادات الأطفال والمهام والاعتمادات."
            isConfigured == false && setupStep == 2 -> "أعد كتابة الرمز نفسه للتأكيد."
            else -> "أدخل رمز المرور المكون من ٦ أرقام للمتابعة إلى قسم الوالدين."
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 6 PIN Dots
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(6) { index ->
            val filled = index < enteredPin.length
            Box(
              modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                  if (filled) HeroGoldDark else MaterialTheme.colorScheme.outlineVariant
                )
                .border(
                  width = 1.dp,
                  color = if (filled) HeroGoldDark else MaterialTheme.colorScheme.outline,
                  shape = CircleShape
                )
                .testTag("pin_dot_$index")
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Error or Lockout message
        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("pin_error_text")
          )
          Spacer(modifier = Modifier.height(8.dp))
        }

        if (isLockedOut) {
          Text(
            text = "⏳ مغلق مؤقتًا: يرجى الانتظار $lockoutSeconds ثانية",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(8.dp))
        }

        // Keypad (3x4)
        Column(
          modifier = Modifier.fillMaxWidth(0.9f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val keypad = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("reset", "0", "delete")
          )

          for (row in keypad) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly
            ) {
              for (key in row) {
                when (key) {
                  "delete" -> {
                    IconButton(
                      onClick = { onDeleteClick() },
                      enabled = !isLockedOut && enteredPin.isNotEmpty(),
                      modifier = Modifier
                        .size(52.dp)
                        .testTag("pin_key_delete")
                    ) {
                      Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "مسح",
                        tint = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                  "reset" -> {
                    if (isConfigured == true) {
                      IconButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier
                          .size(52.dp)
                          .testTag("pin_key_reset")
                      ) {
                        Icon(
                          imageVector = Icons.Default.Fingerprint,
                          contentDescription = "استعادة الرمز",
                          tint = HeroGoldDark
                        )
                      }
                    } else {
                      Spacer(modifier = Modifier.size(52.dp))
                    }
                  }
                  else -> {
                    Box(
                      modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .clickable(enabled = !isLockedOut) { onKeyClick(key) }
                        .testTag("pin_key_$key"),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = key,
                        style = MaterialTheme.typography.titleLarge.copy(
                          fontWeight = FontWeight.Bold,
                          fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  )

  // Reset dialog via device lock authentication
  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = {
        Text("استعادة أو إعادة تعيين الرمز السري", style = MaterialTheme.typography.titleMedium)
      },
      text = {
        Column {
          if (hasDeviceSecurity) {
            Text(
              text = "لحماية خصوصية الأسرة وعدم السماح بتجاوز الرمز دون إذن، تتطلب استعادة الرمز تأكيد قفل شاشة الجهاز (البصمة، النمط، أو رمز المرور).",
              style = MaterialTheme.typography.bodyMedium
            )
          } else {
            Text(
              text = "لا يوجد قفل شاشة مفعل على هذا الجهاز. لحماية إعدادات الأسرة، يرجى تفعيل قفل الشاشة أو البصمة في إعدادات الهاتف لتتمكن من إعادة تعيين الرمز بأمان.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.error
            )
          }
        }
      },
      confirmButton = {
        if (hasDeviceSecurity) {
          Button(
            onClick = {
              val intent = keyguardManager?.createConfirmDeviceCredentialIntent(
                "مصادقة ولي الأمر",
                "يرجى تأكيد أمان الجهاز لإعادة تعيين رمز الدخول"
              )
              if (intent != null) {
                deviceAuthLauncher.launch(intent)
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = HeroGoldDark)
          ) {
            Text("متابعة المصادقة")
          }
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text("إلغاء")
        }
      }
    )
  }
}
