package com.batal.elyoum.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batal.elyoum.data.HeroCategory
import com.batal.elyoum.ui.theme.HeroCrimson
import com.batal.elyoum.ui.theme.HeroGold
import com.batal.elyoum.ui.theme.HeroGoldDark
import com.batal.elyoum.ui.theme.HeroGoldLight
import com.batal.elyoum.ui.theme.HeroGreen
import com.batal.elyoum.ui.theme.NavyDark
import com.batal.elyoum.ui.theme.NavySurface

fun getHeroIcon(name: String): ImageVector {
  return when (name) {
    "medical_services" -> Icons.Default.MedicalServices
    "school" -> Icons.Default.School
    "flight" -> Icons.Default.Flight
    "explore" -> Icons.Default.Explore
    "calculate" -> Icons.Default.Science
    "visibility" -> Icons.Default.Visibility
    "sailing" -> Icons.Default.Explore
    "local_fire_department" -> Icons.Default.LocalFireDepartment
    "volunteer_activism" -> Icons.Default.VolunteerActivism
    "favorite" -> Icons.Default.Favorite
    "military_tech" -> Icons.Default.MilitaryTech
    "psychology" -> Icons.Default.Psychology
    else -> Icons.Default.Star
  }
}

fun getBadgeColor(badgeName: String): Color {
  return when {
    badgeName.contains("وفاء") -> HeroGold
    badgeName.contains("شجاعة") -> HeroCrimson
    badgeName.contains("عطاء") -> HeroGreen
    badgeName.contains("صبر") -> Color(0xFF8B5CF6)
    else -> HeroGold
  }
}

@Composable
fun HeroAvatarBadge(
  iconName: String,
  modifier: Modifier = Modifier,
  sizeDp: Int = 84,
  badgeTint: Color = HeroGold
) {
  Box(
    modifier = modifier
      .size(sizeDp.dp)
      .clip(CircleShape)
      .background(
        Brush.radialGradient(
          colors = listOf(
            NavySurface,
            NavyDark
          )
        )
      )
      .border(
        width = 2.5.dp,
        brush = Brush.linearGradient(
          colors = listOf(HeroGoldLight, HeroGoldDark, HeroGold)
        ),
        shape = CircleShape
      ),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = getHeroIcon(iconName),
      contentDescription = null,
      tint = badgeTint,
      modifier = Modifier.size((sizeDp * 0.52).dp)
    )
  }
}

@Composable
fun HeroCategoryChip(
  category: HeroCategory,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor) = when (category) {
    HeroCategory.SCIENCE -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
    HeroCategory.HUMANITY -> Color(0xFFD1FAE5) to Color(0xFF065F46)
    HeroCategory.COURAGE -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
    HeroCategory.WISDOM -> Color(0xFFFEF3C7) to Color(0xFF92400E)
    HeroCategory.EVERYDAY -> Color(0xFFEDE9FE) to Color(0xFF5B21B6)
  }

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = bgColor,
    modifier = modifier
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
      Text(
        text = category.titleArabic,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun QuoteDisplayCard(
  quote: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(listOf(HeroGold.copy(alpha = 0.6f), Color.Transparent))
    )
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.Top
    ) {
      Icon(
        imageVector = Icons.Default.FormatQuote,
        contentDescription = null,
        tint = HeroGoldDark,
        modifier = Modifier
          .size(32.dp)
          .padding(end = 4.dp)
      )
      Text(
        text = quote,
        style = MaterialTheme.typography.bodyLarge.copy(
          lineHeight = 26.sp,
          fontWeight = FontWeight.Medium
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
fun CertificateCard(
  name: String,
  relation: String,
  reason: String,
  badgeName: String,
  modifier: Modifier = Modifier
) {
  val badgeColor = getBadgeColor(badgeName)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("certificate_card_${name}"),
    shape = RoundedCornerShape(20.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(listOf(HeroGold, HeroGoldLight, HeroGoldDark))
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              MaterialTheme.colorScheme.surface,
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
          )
        )
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.WorkspacePremium,
          contentDescription = null,
          tint = HeroGold,
          modifier = Modifier.size(28.dp)
        )
        Text(
          text = "وثيقة شكر وبطولة",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          ),
          color = HeroGoldDark
        )
        Icon(
          imageVector = Icons.Default.WorkspacePremium,
          contentDescription = null,
          tint = HeroGold,
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "بطل اليوم في حياتي",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Text(
        text = name,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(vertical = 4.dp)
      )

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.padding(bottom = 12.dp)
      ) {
        Text(
          text = relation,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
      }

      Text(
        text = "« $reason »",
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 8.dp)
      )

      Spacer(modifier = Modifier.height(14.dp))

      Surface(
        shape = RoundedCornerShape(24.dp),
        color = badgeColor.copy(alpha = 0.15f),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(badgeColor, badgeColor.copy(alpha = 0.5f)))
        )
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = badgeColor,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = badgeName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = badgeColor
          )
        }
      }
    }
  }
}
