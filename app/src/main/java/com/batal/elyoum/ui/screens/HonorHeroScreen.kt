package com.batal.elyoum.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.batal.elyoum.data.HonoredHeroEntity
import com.batal.elyoum.ui.HeroViewModel
import com.batal.elyoum.ui.components.CertificateCard
import com.batal.elyoum.ui.theme.HeroCrimson
import com.batal.elyoum.ui.theme.HeroGold
import com.batal.elyoum.ui.theme.HeroGoldDark
import com.batal.elyoum.ui.theme.HeroGoldLight
import com.batal.elyoum.ui.theme.HeroGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HonorHeroScreen(
  viewModel: HeroViewModel,
  modifier: Modifier = Modifier
) {
  val honoredHeroes by viewModel.honoredHeroes.collectAsState()
  val context = LocalContext.current

  var isFormOpen by remember { mutableStateOf(false) }
  var heroName by remember { mutableStateOf("") }
  var selectedRelation by remember { mutableStateOf("والدتي الحبيبة") }
  var heroReason by remember { mutableStateOf("") }
  var selectedBadge by remember { mutableStateOf("وسام الوفاء والعطاء") }

  val commonRelations = listOf(
    "والدتي الحبيبة", "والدي الكريم", "معلمي الفاضل", "رفيق دربي", "طبيبي المخلص", "ابني الغالي", "أخي السند"
  )

  val badges = listOf(
    "وسام الوفاء والعطاء",
    "وسام الشجاعة والصمود",
    "وسام الصبر العظيم",
    "وسام الحكمة والنور"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(12.dp))
      // Header
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "كرّم بطل حياتك",
          style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center
        )
        Text(
          text = "من هو الشخص الذي ألهمك وترك بصمة بطولة في حياتك اليوم؟",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
      }
    }

    // Toggle Add New Hero Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("honor_hero_toggle_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isFormOpen) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = HeroGoldDark,
                modifier = Modifier.size(28.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "منح وسام بطل اليوم لشخص عزيز",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            IconButton(
              onClick = { isFormOpen = !isFormOpen },
              modifier = Modifier.testTag("toggle_honor_form_button")
            ) {
              Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "فتح النموذج",
                tint = HeroGoldDark
              )
            }
          }

          AnimatedVisibility(visible = isFormOpen) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
            ) {
              OutlinedTextField(
                value = heroName,
                onValueChange = { heroName = it },
                label = { Text("اسم بطلك") },
                placeholder = { Text("مثال: أمي مريم، أستاذ أحمد...") },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("honor_hero_name_input"),
                shape = RoundedCornerShape(14.dp)
              )

              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = "صلة القرابة أو العلاقة:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
              )

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                commonRelations.forEach { relation ->
                  val isSelected = selectedRelation == relation
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) HeroGold else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { selectedRelation = relation }
                  ) {
                    Text(
                      text = relation,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              OutlinedTextField(
                value = heroReason,
                onValueChange = { heroReason = it },
                label = { Text("لماذا تعتبره بطل اليوم؟") },
                placeholder = { Text("اذكر صفة نبيلة أو موقفاً عطوفاً أو تضحية قدّمها...") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("honor_hero_reason_input"),
                shape = RoundedCornerShape(14.dp)
              )

              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = "اختر الوسام المستحق:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
              )

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                badges.forEach { badge ->
                  val isSelected = selectedBadge == badge
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(HeroGold, HeroGoldDark))) else null,
                    modifier = Modifier.clickable { selectedBadge = badge }
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = if (isSelected) HeroGoldDark else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                onClick = {
                  if (heroName.isNotBlank() && heroReason.isNotBlank()) {
                    viewModel.honorHero(
                      name = heroName,
                      relation = selectedRelation,
                      reason = heroReason,
                      badgeName = selectedBadge
                    ) {
                      heroName = ""
                      heroReason = ""
                      isFormOpen = false
                    }
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("submit_honor_button"),
                enabled = heroName.isNotBlank() && heroReason.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = HeroGoldDark,
                  contentColor = Color.White
                )
              ) {
                Icon(
                  imageVector = Icons.Default.WorkspacePremium,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "إصدار وثيقة التكريم والبطولة",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
              }
            }
          }
        }
      }
    }

    // Section Title: لوحة الشرف
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "لوحة شرف أبطال حياتي (${honoredHeroes.size})",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    if (honoredHeroes.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = null,
              tint = HeroGoldDark,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "لم تُضِف بطلاً بعد!",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "انقر على زر الإضافة أعلاه لإصدار أول شهادة شكر وتقدير لوالديك، معلمك، أو أي شخص يعيش دور البطولة في حياتك.",
              style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 6.dp)
            )
          }
        }
      }
    } else {
      items(honoredHeroes, key = { it.id }) { hero ->
        Column(modifier = Modifier.fillMaxWidth()) {
          CertificateCard(
            name = hero.name,
            relation = hero.relation,
            reason = hero.reason,
            badgeName = hero.badgeName
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Share certificate
            IconButton(
              onClick = {
                val shareText = "🏆 شهادة بطل اليوم!\n\nيسرني أن أمنح لقب بطل اليوم إلى:\n${hero.name} (${hero.relation})\n\n« ${hero.reason} »\n\nالوسام: ${hero.badgeName} 🎖️\n- من تطبيق بطل اليوم"
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(shareIntent, "مشاركة وثيقة التكريم"))
              }
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "مشاركة الوثيقة",
                tint = HeroGoldDark
              )
            }

            // Delete certificate
            IconButton(
              onClick = { viewModel.deleteHonoredHero(hero.id) }
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "حذف",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
