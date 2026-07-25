package com.iotkin.smartoutlet.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iotkin.smartoutlet.R
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import com.iotkin.smartoutlet.ui.theme.OreoSpacing

@Composable
fun OreoCatLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    contentDescription: String? = "Oreo Smart Outlet logo"
) {
    Image(
        painter = painterResource(
            id = R.drawable.oreo_cat_logo
        ),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

@Preview(
    name = "Oreo Cat Logo Light",
    showBackground = true
)
@Composable
private fun OreoCatLogoLightPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = OreoShapeTokens.ExtraLarge
                )
                .padding(OreoSpacing.StackLarge),
            contentAlignment = Alignment.Center
        ) {
            OreoCatLogo(
                size = 180.dp
            )
        }
    }
}

@Preview(
    name = "Oreo Cat Logo Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun OreoCatLogoDarkPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = OreoShapeTokens.ExtraLarge
                )
                .padding(OreoSpacing.StackLarge),
            contentAlignment = Alignment.Center
        ) {
            OreoCatLogo(
                size = 180.dp
            )
        }
    }
}