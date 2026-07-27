package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentOnPrimary
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BgDark
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub

@Composable
fun HeroSpotlight(
    artistName: String = "Arijit Singh",
    subtitle: String = "Listen to all popular songs without downloading.",
    onPlayAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.img_hero_spotlight_1785109463743),
            contentDescription = "Spotlight Banner Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            BgDark.copy(alpha = 0.95f),
                            BgDark.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentPrimary)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SPOTLIGHT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentOnPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = artistName,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextMain
            )

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSub,
                maxLines = 1
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onPlayAllClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .testTag("play_all_hero_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = AccentOnPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LISTEN NOW",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AccentOnPrimary
                    )
                }
            }
        }
    }
}
