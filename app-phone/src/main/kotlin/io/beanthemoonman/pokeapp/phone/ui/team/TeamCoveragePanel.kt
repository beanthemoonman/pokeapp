package io.beanthemoonman.pokeapp.phone.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.beanthemoonman.pokeapp.domain.model.TeamCoverage
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.phone.R
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadgeSize
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexTheme
import io.beanthemoonman.pokeapp.ui.common.theme.color
import io.beanthemoonman.pokeapp.ui.common.theme.onColor

/**
 * Stateless type-coverage panel. All values are read off the passed-in [coverage] — nothing is
 * computed or hardcoded here. When [hasMembers] is false the team is empty, so only the prompt is
 * shown. Otherwise it renders the defensive matrix (every roster type, highlighting those a member
 * is weak to) and the offensive STAB gaps.
 */
@Composable
fun TeamCoveragePanel(
    coverage: TeamCoverage,
    hasMembers: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!hasMembers) {
        Text(
            text = stringResource(R.string.team_coverage_empty),
            color = PokedexColors.TextDim,
            fontSize = 13.sp,
            modifier = modifier.fillMaxWidth().padding(vertical = 24.dp),
        )
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        DefensiveSection(coverage)
        OffensiveSection(coverage)
    }
}

@Composable
private fun DefensiveSection(coverage: TeamCoverage) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SectionLabel(stringResource(R.string.team_coverage_defensive))
        Text(
            text = stringResource(R.string.team_coverage_weak_points, coverage.weakPointCount),
            color = WeakPointColor,
            fontSize = 11.sp,
        )
    }

    coverage.roster.chunked(COLUMNS).forEach { rowTypes ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = CELL_GAP),
            horizontalArrangement = Arrangement.spacedBy(CELL_GAP),
        ) {
            rowTypes.forEach { type ->
                TypeCell(type = type, weakCount = coverage.defensiveWeaknesses[type] ?: 0, modifier = Modifier.weight(1f))
            }
            // Pad the final row so cells keep their column width.
            repeat(COLUMNS - rowTypes.size) { Box(modifier = Modifier.weight(1f)) }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(stringResource(R.string.team_coverage_legend_shared), ringed = true)
        LegendItem(stringResource(R.string.team_coverage_legend_covered), ringed = false)
    }
}

@Composable
private fun TypeCell(type: Type, weakCount: Int, modifier: Modifier) {
    val accent = type.color()
    val weak = weakCount > 0
    val shape = RoundedCornerShape(8.dp)
    Box(modifier = modifier.aspectRatio(1f)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape)
                .background(if (weak) accent else accent.copy(alpha = 0.14f))
                .then(if (weak) Modifier.border(2.dp, WeakRingColor, shape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = type.name.take(3),
                color = if (weak) type.onColor() else accent.copy(alpha = 0.9f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        if (weakCount > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(15.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(WeakRingColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "$weakCount", color = Color(0xFF1A0F0D), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OffensiveSection(coverage: TeamCoverage) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PokedexColors.Line),
        )
        SectionLabel(
            text = stringResource(R.string.team_coverage_offensive_gaps),
            modifier = Modifier.padding(top = 14.dp),
        )
        if (coverage.offensiveGaps.isEmpty()) {
            Text(
                text = stringResource(R.string.team_coverage_offensive_none),
                color = PokedexColors.TextFaint,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 9.dp),
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                coverage.offensiveGaps.forEach { type ->
                    TypeBadge(type = type, size = TypeBadgeSize.SM, soft = true)
                }
                Text(
                    text = stringResource(R.string.team_coverage_offensive_hint),
                    color = PokedexColors.TextFaint,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = PokedexColors.TextFaint,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
    )
}

@Composable
private fun LegendItem(label: String, ringed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        val swatch = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp))
        Box(
            modifier = if (ringed) swatch.border(2.dp, WeakRingColor, RoundedCornerShape(3.dp))
            else swatch.background(Color.White.copy(alpha = 0.16f)),
        )
        Text(text = label, color = PokedexColors.TextDim, fontSize = 10.5.sp)
    }
}

private const val COLUMNS = 6
private val CELL_GAP = 6.dp
private val WeakRingColor = Color(0xFFFF6B5C)
private val WeakPointColor = Color(0xFFE08A4A)

@Preview(widthDp = 360, backgroundColor = 0xFF0C0D11, showBackground = true)
@Composable
private fun TeamCoveragePanelPreview() {
    val roster = Type.entries.take(12)
    PokedexTheme {
        Box(modifier = Modifier.background(Color(0xFF0C0D11)).padding(16.dp)) {
            TeamCoveragePanel(
                coverage = TeamCoverage(
                    roster = roster,
                    defensiveWeaknesses = roster.associateWith { 0 } + mapOf(Type.ROCK to 2, Type.ELECTRIC to 1),
                    offensiveGaps = listOf(Type.WATER, Type.FIRE, Type.DRAGON),
                ),
                hasMembers = true,
            )
        }
    }
}
