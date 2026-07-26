package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiceTheme
import com.example.model.RollHistoryItem
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.DarkNavySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RollHistoryCard(
    item: RollHistoryItem,
    theme: DiceTheme,
    index: Int,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeString = timeFormat.format(Date(item.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavyCard
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Index & Time
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(DarkNavySurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$index",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = timeString,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    if (item.isDouble) {
                        Text(
                            text = "DOUBLE!",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentGold
                        )
                    } else if (item.isMaxRoll) {
                        Text(
                            text = "MAX ROLL!",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentCyan
                        )
                    }
                }
            }

            // Mini Dice Values & Total Pill
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.diceValues.forEach { valNum ->
                        DiceFace(
                            value = valNum,
                            theme = theme,
                            isRolling = false,
                            size = 32.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Total Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (item.isDouble) AccentGold.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "= ${item.total}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isDouble) AccentGold else TextPrimary
                    )
                }
            }
        }
    }
}
