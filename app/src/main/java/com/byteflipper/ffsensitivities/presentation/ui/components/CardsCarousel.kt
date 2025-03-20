package com.byteflipper.ffsensitivities.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter

// Модель данных для карточки
data class CardData(
    val imageUrl: String,
    val title: String? = null, // для второго типа карточки
    val description: String? = null, // для второго типа карточки
    val nestedItems: List<CardData> = emptyList(), // вложенные карточки
    val type: CardType // тип карточки
)

// Типы карточек
enum class CardType {
    FIRST_TYPE, // первый тип: картинка + кнопка
    SECOND_TYPE // второй тип: картинка + заголовок + описание + кнопка
}

// Компонент карусели с карточками
@Composable
fun CardsCarousel(cardDataList: List<CardData>) {
    // Получаем высоту экрана
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Карусель, которая занимает 1/4 экрана по высоте
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight / 4) // 1/4 высоты экрана
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cardDataList) { cardData ->
                ParentCard(cardData)
            }
        }
    }
}

// Родительская карточка, внутри которой вложены другие карточки
@Composable
fun ParentCard(cardData: CardData) {
    Card(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp) // Можно подстроить ширину карточки
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Основное изображение карточки
            Image(
                painter = rememberImagePainter(cardData.imageUrl),
                contentDescription = "Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp), // Ограничиваем высоту изображения
                contentScale = ContentScale.Crop
            )

            // Если есть вложенные элементы, показываем горизонтальный список
            if (cardData.nestedItems.isNotEmpty()) {
                NestedCardsRow(nestedItems = cardData.nestedItems)
            }

            Button(onClick = { /* Действие для открытия */ }) {
                Text("Открыть")
            }
        }
    }
}

// Горизонтальный ряд вложенных карточек
@Composable
fun NestedCardsRow(nestedItems: List<CardData>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp), // Высота для вложенных карточек
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(nestedItems) { nestedItem ->
            NestedCard(nestedItem)
        }
    }
}

@Composable
fun NestedCard(cardData: CardData) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(100.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(cardData.imageUrl),
                contentDescription = "Nested Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentScale = ContentScale.Crop
            )
            cardData.title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}