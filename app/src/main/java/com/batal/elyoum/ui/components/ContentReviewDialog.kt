package com.batal.elyoum.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.batal.elyoum.data.ContentReviewRecordEntity
import com.batal.elyoum.data.ContentReviewStatus
import com.batal.elyoum.data.MaterialType
import com.batal.elyoum.ui.theme.HeroGold
import com.batal.elyoum.ui.theme.NavySurface

@Composable
fun ContentReviewDialog(
  record: ContentReviewRecordEntity,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 16.dp)
        .testTag("content_review_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(scrollState)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(HeroGold.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = "المصدر",
                tint = HeroGold,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "توثيق المحتوى والمصدر",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "سجل المراجعة التحريرية المعتمد",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_review_dialog")) {
            Icon(Icons.Default.Close, contentDescription = "إغلاق")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))

        // Title and Status Badge
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = record.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = HeroGold,
            modifier = Modifier.weight(1f)
          )

          val isVerified = record.reviewStatus == ContentReviewStatus.VERIFIED.code
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isVerified) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (isVerified) {
                Icon(
                  Icons.Default.Verified,
                  contentDescription = null,
                  tint = Color(0xFF2E7D32),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text(
                text = when (record.reviewStatus) {
                  ContentReviewStatus.VERIFIED.code -> "تمت المراجعة والتدقيق"
                  ContentReviewStatus.INCOMPLETE.code -> "بيانات مراجعة غير مكتملة"
                  else -> "بانتظار المراجعة"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Target Educational Goal
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "🎯 الهدف التربوي والقيمي:",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = record.targetGoal,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Source Reference Details
        Text(
          text = "المصدر والتوثيق التاريخي",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        ReviewDetailRow(label = "عنوان المرجع:", value = record.sourceReferenceTitle)
        ReviewDetailRow(label = "المؤلف أو الجهة:", value = record.sourceAuthorOrEntity)
        if (!record.sourceCitationLocation.isNullOrBlank()) {
          ReviewDetailRow(label = "موضع الإحالة:", value = record.sourceCitationLocation)
        }
        ReviewDetailRow(
          label = "نوع المادة:",
          value = when (record.materialType) {
            MaterialType.SUMMARY.code -> "تلخيص موثق بلغة مبسطة ومحببة للطفل"
            MaterialType.ORIGINAL_TEXT.code -> "نص أصلي منقول بأمانة"
            else -> "صياغة تربوية أصلية مستندة لمصادر تاريخية"
          }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Academic Reviewer details
        Text(
          text = "بيانات التدقيق والمراجعة",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (!record.reviewerName.isNullOrBlank()) {
          ReviewDetailRow(label = "المراجع:", value = "${record.reviewerName} (${record.reviewerSpecialty ?: ""})")
        }
        if (!record.reviewDate.isNullOrBlank()) {
          ReviewDetailRow(label = "تاريخ المراجعة:", value = record.reviewDate)
        }
        if (!record.approvalScope.isNullOrBlank()) {
          ReviewDetailRow(label = "نطاق الموافقة:", value = record.approvalScope)
        }
        if (!record.internalProofReference.isNullOrBlank()) {
          ReviewDetailRow(label = "رمز التوثيق الداخلي:", value = record.internalProofReference)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (!record.sourceUrl.isNullOrBlank()) {
            OutlinedButton(
              onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(record.sourceUrl))
                context.startActivity(intent)
              },
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("فتح المرجع", fontSize = 13.sp)
            }
          }

          Button(
            onClick = onDismiss,
            modifier = Modifier.weight(1f)
          ) {
            Text("إغلاق")
          }
        }
      }
    }
  }
}

@Composable
private fun ReviewDetailRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(0.4f)
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(0.6f),
      textAlign = TextAlign.Start
    )
  }
}
