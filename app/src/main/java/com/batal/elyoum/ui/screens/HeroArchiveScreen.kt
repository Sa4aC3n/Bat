package com.batal.elyoum.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.batal.elyoum.data.Hero
import com.batal.elyoum.data.HeroCategory
import com.batal.elyoum.ui.HeroViewModel
import com.batal.elyoum.ui.components.HeroAvatarBadge
import com.batal.elyoum.ui.components.HeroCategoryChip
import com.batal.elyoum.ui.theme.HeroGold
import com.batal.elyoum.ui.theme.HeroGoldDark
import com.batal.elyoum.ui.theme.HeroGoldLight
import com.batal.elyoum.ui.theme.HeroGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroArchiveScreen(
  viewModel: HeroViewModel,
  onHeroSelected: (Hero) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Archive, 1: Quiz

  Column(
    modifier = modifier.fillMaxSize()
  ) {
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = HeroGoldDark,
      modifier = Modifier.fillMaxWidth()
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Text(
            text = "موسوعة الأبطال",
            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
          )
        }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Text(
            text = "من يشبهك من الأبطال؟",
            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
          )
        }
      )
    }

    if (selectedTab == 0) {
      ArchiveListContent(
        viewModel = viewModel,
        onHeroSelected = onHeroSelected
      )
    } else {
      HeroQuizContent(
        viewModel = viewModel,
        onHeroSelected = onHeroSelected
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArchiveListContent(
  viewModel: HeroViewModel,
  onHeroSelected: (Hero) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf<HeroCategory?>(null) }
  val favorites by viewModel.favoriteHeroIds.collectAsState()

  val filteredHeroes = viewModel.allHeroes.filter { hero ->
    val matchesSearch = searchQuery.isBlank() ||
      hero.name.contains(searchQuery, ignoreCase = true) ||
      hero.title.contains(searchQuery, ignoreCase = true) ||
      hero.story.contains(searchQuery, ignoreCase = true)
    val matchesCategory = selectedCategory == null || hero.category == selectedCategory
    matchesSearch && matchesCategory
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(12.dp))

      // Search field
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("ابحث عن بطل بالاسم أو الإنجاز...") },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "بحث",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_hero_input"),
        shape = RoundedCornerShape(16.dp)
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Category filter chips
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val allSelected = selectedCategory == null
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (allSelected) HeroGold else MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.clickable { selectedCategory = null }
        ) {
          Text(
            text = "الكل (${viewModel.allHeroes.size})",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (allSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }

        HeroCategory.values().forEach { cat ->
          val isSelected = selectedCategory == cat
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) HeroGold else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clickable { selectedCategory = cat }
          ) {
            Text(
              text = cat.titleArabic,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
    }

    items(filteredHeroes, key = { it.id }) { hero ->
      val isFav = favorites.contains(hero.id)

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .clickable { onHeroSelected(hero) }
          .testTag("archive_hero_card_${hero.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          HeroAvatarBadge(
            iconName = hero.iconName,
            sizeDp = 60
          )

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = hero.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )

              if (isFav) {
                Icon(
                  imageVector = Icons.Default.Bookmark,
                  contentDescription = "محفوظ",
                  tint = HeroGold,
                  modifier = Modifier.size(20.dp)
                )
              }
            }

            Text(
              text = hero.title,
              style = MaterialTheme.typography.bodySmall,
              color = HeroGoldDark,
              maxLines = 1,
              modifier = Modifier.padding(vertical = 2.dp)
            )

            HeroCategoryChip(category = hero.category)
          }

          Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = "عرض",
            tint = HeroGoldDark,
            modifier = Modifier.padding(start = 8.dp)
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun HeroQuizContent(
  viewModel: HeroViewModel,
  onHeroSelected: (Hero) -> Unit,
  modifier: Modifier = Modifier
) {
  val currentIndex by viewModel.currentQuizIndex.collectAsState()
  val quizResult by viewModel.quizResult.collectAsState()
  val questions = viewModel.quizQuestions

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (quizResult != null) {
      val result = quizResult!!
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("quiz_result_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(HeroGold, HeroGoldLight))
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = HeroGoldDark,
            modifier = Modifier.size(42.dp)
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "نمط بطولتك الداخلي:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Text(
            text = result.archetypeTitle,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = HeroGoldDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
          )

          Text(
            text = result.description,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Matched Hero Card preview
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onHeroSelected(result.matchedHero) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              HeroAvatarBadge(
                iconName = result.matchedHero.iconName,
                sizeDp = 50
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "بطل يشبه روحك:",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = result.matchedHero.name,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              Text(
                text = "عرض 👈",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = HeroGoldDark
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedButton(
            onClick = { viewModel.resetQuiz() },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reset_quiz_button"),
            shape = RoundedCornerShape(14.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("إعادة الاختبار")
          }
        }
      }
    } else {
      // Question Display
      val q = questions[currentIndex]

      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "اختبار بطل اليوم",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = HeroGold.copy(alpha = 0.15f)
          ) {
            Text(
              text = "سؤال ${currentIndex + 1} من ${questions.size}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = HeroGoldDark,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Text(
            text = q.question,
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              lineHeight = 30.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        q.options.forEachIndexed { optIndex, option ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 5.dp)
              .clip(RoundedCornerShape(16.dp))
              .clickable { viewModel.answerQuiz(currentIndex, option.categoryMatch) }
              .testTag("quiz_option_${optIndex}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(HeroGold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${optIndex + 1}",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = HeroGoldDark
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = option.text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }
  }
}
