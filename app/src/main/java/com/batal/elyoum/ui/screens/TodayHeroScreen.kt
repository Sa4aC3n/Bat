package com.batal.elyoum.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batal.elyoum.ui.HeroViewModel
import com.batal.elyoum.ui.components.ContentReviewDialog
import com.batal.elyoum.ui.components.HeroAvatarBadge
import com.batal.elyoum.ui.components.HeroCategoryChip
import com.batal.elyoum.ui.components.QuoteDisplayCard
import com.batal.elyoum.ui.theme.HeroCrimson
import com.batal.elyoum.ui.theme.HeroGold
import com.batal.elyoum.ui.theme.HeroGoldDark
import com.batal.elyoum.ui.theme.HeroGoldLight
import com.batal.elyoum.ui.theme.NavyDark
import com.batal.elyoum.ui.theme.NavySurface

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodayHeroScreen(
  viewModel: HeroViewModel,
  modifier: Modifier = Modifier
) {
  val selectedHero by viewModel.selectedHero.collectAsState()
  val todayHero = viewModel.todayHero
  val favorites by viewModel.favoriteHeroIds.collectAsState()
  val isFavorite = favorites.contains(selectedHero.id)
  val isViewingTodayHero = selectedHero.id == todayHero.id

  val context = LocalContext.current
  val scrollState = rememberScrollState()

  var showReviewDialog by remember { mutableStateOf(false) }
  val currentReviewRecord by viewModel.getContentReview("hero_${selectedHero.id}").collectAsState(initial = null)

  if (showReviewDialog && currentReviewRecord != null) {
    ContentReviewDialog(
      record = currentReviewRecord!!,
      onDismiss = { showReviewDialog = false }
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Bar Badge: بطل اليوم الرسمي
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isViewingTodayHero) HeroGold.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(HeroGold, HeroGoldDark))
        )
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (isViewingTodayHero) Icons.Default.Today else Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = HeroGoldDark,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isViewingTodayHero) "بطل اليوم المختار" else "استكشاف أبطال الإنسانية",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        // Content Review / Documentation action
        if (currentReviewRecord != null) {
          IconButton(
            onClick = { showReviewDialog = true },
            modifier = Modifier.testTag("content_review_button")
          ) {
            Icon(
              imageVector = Icons.Default.MenuBook,
              contentDescription = "المصدر والتوثيق",
              tint = HeroGold
            )
          }
        }

        // Favorite action
        IconButton(
          onClick = { viewModel.toggleFavorite(selectedHero.id, isFavorite) },
          modifier = Modifier.testTag("toggle_favorite_button")
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            contentDescription = "حفظ البطل",
            tint = if (isFavorite) HeroGold else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Share action
        IconButton(
          onClick = {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
              type = "text/plain"
              putExtra(
                Intent.EXTRA_TEXT,
                "🌟 بطل اليوم: ${selectedHero.name}\n${selectedHero.title}\n\n« ${selectedHero.quote} »\n\n- من تطبيق بطل اليوم"
              )
            }
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة إلهام بطل اليوم"))
          },
          modifier = Modifier.testTag("share_hero_button")
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "مشاركة",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Hero Main Showcase Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("hero_main_card"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      border = CardDefaults.outlinedCardBorder().copy(
        brush = Brush.linearGradient(listOf(HeroGold.copy(alpha = 0.5f), Color.Transparent))
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        HeroAvatarBadge(
          iconName = selectedHero.iconName,
          sizeDp = 96
        )

        Spacer(modifier = Modifier.height(14.dp))

        HeroCategoryChip(category = selectedHero.category)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = selectedHero.name,
          style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center
        )

        Text(
          text = selectedHero.title,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
          color = HeroGoldDark,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
        )

        Text(
          text = "📍 ${selectedHero.era}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quote card
        QuoteDisplayCard(quote = selectedHero.quote)

        Spacer(modifier = Modifier.height(16.dp))

        // Story Section
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(16.dp)
        ) {
          Text(
            text = "قصة البطولة والأثر الخالد",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
          )
          Text(
            text = selectedHero.story,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Virtues / صفات البطولة
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.Start
        ) {
          Text(
            text = "صفات البطولة التي تميز بها:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
          )

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            selectedHero.virtues.forEach { virtue ->
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = CardDefaults.outlinedCardBorder()
              ) {
                Text(
                  text = "✨ $virtue",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Heroic Lesson / كيف نكون مثله اليوم
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = HeroGold.copy(alpha = 0.12f)
          ),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(HeroGold, HeroGoldLight))
          )
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = Icons.Default.Lightbulb,
              contentDescription = null,
              tint = HeroGoldDark,
              modifier = Modifier
                .size(24.dp)
                .padding(end = 4.dp)
            )
            Column {
              Text(
                text = "كيف تصنع بطولتك اليوم مثل ${selectedHero.name.split(" ").firstOrNull() ?: ""}؟",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = HeroGoldDark,
                modifier = Modifier.padding(bottom = 4.dp)
              )
              Text(
                text = selectedHero.dailyHeroicLesson,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Random shuffle or return to today hero
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Button(
        onClick = { viewModel.selectRandomHero() },
        modifier = Modifier
          .weight(1f)
          .testTag("shuffle_hero_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Casino,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "إلهام بطل آخر",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
      }

      if (!isViewingTodayHero) {
        OutlinedButton(
          onClick = { viewModel.resetToTodayHero() },
          modifier = Modifier
            .weight(1f)
            .testTag("return_today_hero_button"),
          shape = RoundedCornerShape(16.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Today,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "بطل اليوم",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
