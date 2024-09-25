package com.byteflipper.ffsensitivities.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ui.components.CardData
import com.byteflipper.ffsensitivities.ui.components.CardType
import com.byteflipper.ffsensitivities.ui.components.CardsCarousel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        data class CarouselItem(
            val id: Int,
            @DrawableRes val imageResId: Int,
            @StringRes val contentDescriptionResId: Int
        )

        val items =
            listOf(
                CarouselItem(0, R.drawable.ic_launcher_foreground, R.string.app_name),
                CarouselItem(1, R.drawable.ic_launcher_foreground, R.string.app_name),
                CarouselItem(2, R.drawable.ic_launcher_foreground, R.string.app_name),
                CarouselItem(3, R.drawable.ic_launcher_foreground, R.string.app_name),
                CarouselItem(4, R.drawable.ic_launcher_foreground, R.string.app_name),
            )

        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState { items.count() },
            modifier = Modifier.width(412.dp).height(221.dp),
            preferredItemWidth = 186.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) { i ->
            val item = items[i]
            Image(
                modifier = Modifier.height(205.dp).maskClip(MaterialTheme.shapes.extraLarge),
                painter = painterResource(id = item.imageResId),
                contentDescription = stringResource(item.contentDescriptionResId),
                contentScale = ContentScale.Crop
            )
        }

        val exampleData = listOf(
            CardData(imageUrl = "https://example.com/image1.jpg", type = CardType.FIRST_TYPE),
            CardData(imageUrl = "https://example.com/image2.jpg", title = "Заголовок", description = "Описание", type = CardType.SECOND_TYPE)
        )

        CardsCarousel(cardDataList = exampleData)

        ElevatedCard (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = ShapeDefaults.Medium,
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Item it",
                textAlign = TextAlign.Center,
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(100) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = ShapeDefaults.Medium
                ) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "Item $it",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}