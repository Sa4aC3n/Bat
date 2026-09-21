package com.batal.elyoum.ui.screens.parent

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batal.elyoum.data.ContentReviewRecordEntity
import com.batal.elyoum.ui.HeroViewModel
import com.batal.elyoum.ui.components.ContentReviewDialog
import com.batal.elyoum.ui.theme.HeroGold
import com.batal.elyoum.ui.theme.HeroGoldDark
import com.batal.elyoum.ui.theme.HeroGreen
import com.batal.elyoum.ui.theme.HeroGreenContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContentReviewTabContent(
  viewModel: HeroViewModel,
  modifier: Modifier = Modifier
) {
  val records by viewModel.contentReviewRecords.collectAsState()
  var selectedCategory by remember { mutableStateOf("ALL") }
  var activeReviewRecord by remember { mutableStateOf<ContentReviewRecordEntity?>(null) }

  val filteredRecords = when (selectedCategory) {
    "HERO" -> records.filter { it.contentType == "HERO_PROFILE" }
    "DEED" -> records.filter { it.contentType == "DAILY_DEED" }
    else -> records
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(10.dp))
      // Overview Card
      Card(
        modifier = Modifier.fillMaxWidth().testTag("content_review_header_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HeroGold.copy(alpha = 0.1f))
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(Icons.Default.FactCheck, contentDescription = null, tint = HeroGoldDark, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "سجل التوثيق والمراجعة التحريرية",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = HeroGoldDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "جميع القصص والأعمال المقترحة موثقة بمصادرها وأهدافها التربوية ومعتمدة لضمان تقديم محتوى ملهم، آمن، وموثوق لأطفالنا.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Category Filter
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = selectedCategory == "ALL",
          onClick = { selectedCategory = "ALL" },
          label = { Text("كافة المحتوى (${records.size})") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = HeroGold.copy(alpha = 0.25f),
            selectedLabelColor = HeroGoldDark
          ),
          modifier = Modifier.testTag("filter_all_content")
        )
        FilterChip(
          selected = selectedCategory == "HERO",
          onClick = { selectedCategory = "HERO" },
          label = { Text("أبطال اليوم") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = HeroGold.copy(alpha = 0.25f),
            selectedLabelColor = HeroGoldDark
          ),
          modifier = Modifier.testTag("filter_hero_content")
        )
        FilterChip(
          selected = selectedCategory == "DEED",
          onClick = { selectedCategory = "DEED" },
          label = { Text("أعمال الخير") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = HeroGold.copy(alpha = 0.25f),
            selectedLabelColor = HeroGoldDark
          ),
          modifier = Modifier.testTag("filter_deed_content")
        )
      }
    }

    // List of Content Records
    items(filteredRecords, key = { it.contentId }) { record ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable { activeReviewRecord = record }
          .testTag("review_record_${record.contentId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (record.contentType == "HERO_PROFILE") Icons.Default.Verified else Icons.Default.MenuBook,
            contentDescription = null,
            tint = HeroGreen,
            modifier = Modifier.size(28.dp)
          )

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = record.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = HeroGreenContainer
              ) {
                Text(
                  text = "معتمد",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color(0xFF065F46),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "المصدر: ${record.sourceReference}",
              style = MaterialTheme.typography.bodySmall,
              color = HeroGoldDark
            )

            if (record.pedagogicalGoal.isNotBlank()) {
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "الهدف: ${record.pedagogicalGoal}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
              )
            }
          }

          Spacer(modifier = Modifier.width(6.dp))
          Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(30.dp))
    }
  }

  // Active Dialog
  if (activeReviewRecord != null) {
    ContentReviewDialog(
      record = activeReviewRecord!!,
      onDismiss = { activeReviewRecord = null }
    )
  }
}
