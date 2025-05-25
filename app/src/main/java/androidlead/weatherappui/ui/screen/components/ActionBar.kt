// Improved ActionBar.kt with dynamic location support
package androidlead.weatherappui.ui.screen.components

import androidlead.weatherappui.R
import androidlead.weatherappui.di.AppLocationManager
import androidlead.weatherappui.ui.theme.ColorGradient1
import androidlead.weatherappui.ui.theme.ColorGradient2
import androidlead.weatherappui.ui.theme.ColorGradient3
import androidlead.weatherappui.ui.theme.ColorImageShadow
import androidlead.weatherappui.ui.theme.ColorSurface
import androidlead.weatherappui.ui.theme.ColorTextPrimary
import androidlead.weatherappui.ui.theme.ColorTextSecondary
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ActionBar(
    modifier: Modifier = Modifier,
    location: String = "Loading...",
    isUpdating: Boolean = false,
    onLocationClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ControlButton(onClick = onMenuClick)
        LocationInfo(
            modifier = Modifier
                .padding(top = 10.dp)
                .clickable { onLocationClick() },
            location = location,
            isUpdating = isUpdating
        )
        ProfileButton(onClick = onProfileClick)
    }
}

@Composable
private fun ControlButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        color = ColorSurface,
        shape = CircleShape,
        modifier = modifier
            .size(48.dp)
            .customShadow(
                color = Color.Black,
                alpha = 0.15f,
                shadowRadius = 16.dp,
                borderRadius = 48.dp,
                offsetY = 4.dp
            )
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_control),
                contentDescription = "Menu",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ProfileButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .border(
                width = 1.5.dp,
                color = ColorSurface,
                shape = CircleShape
            )
            .customShadow(
                color = ColorImageShadow,
                alpha = 0.7f,
                shadowRadius = 12.dp,
                borderRadius = 48.dp,
                offsetY = 6.dp
            )
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_profile),
            contentDescription = "Profile",
            modifier = modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}

@Composable
private fun LocationInfo(
    modifier: Modifier = Modifier,
    location: String,
    isUpdating: Boolean = false
) {
    val currentContext = LocalContext.current // Get the Context here
    remember { AppLocationManager(currentContext) } // Pass it and remember

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_location_pin),
                contentDescription = null,
                modifier = Modifier.height(18.dp),
                contentScale = ContentScale.FillHeight
            )
            Text(
                text = location,
                style = MaterialTheme.typography.titleLarge,
                color = ColorTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        if (isUpdating) {
            ProgressBar(text = "Updating •")
        } else {
            ProgressBar(text = "Tap to retry")
        }
    }
}

@Composable
private fun ProgressBar(
    modifier: Modifier = Modifier,
    text: String = "Updating •"
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    0f to ColorGradient1,
                    0.25f to ColorGradient2,
                    1f to ColorGradient3
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(
                vertical = 2.dp,
                horizontal = 10.dp
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = ColorTextSecondary.copy(alpha = 0.7f)
        )
    }
}