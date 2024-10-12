package com.byteflipper.ffsensitivities.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ui.components.SliderView

//@Preview(showBackground = true)
@Composable
fun SensitivitiesScreen(
    modifier: Modifier = Modifier,
    model: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 0.dp, 16.dp, 0.dp)
    ) {

        var slider1Value by remember { mutableFloatStateOf(50f) }
        var slider2Value by remember { mutableFloatStateOf(75f) }
        var slider3Value by remember { mutableFloatStateOf(25f) }

        SliderView(
            label = R.string.review,
            initialValue = slider1Value,
            onValueChange = { slider1Value = it }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 1.dp
        )

        SliderView(
            label = R.string.collimator,
            initialValue = slider2Value,
            onValueChange = { slider2Value = it }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 1.dp
        )

        SliderView(
            label = R.string.x2_scope,
            initialValue = slider3Value,
            onValueChange = { slider3Value = it }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 1.dp
        )

        SliderView(
            label = R.string.x4_scope,
            initialValue = slider3Value,
            onValueChange = { slider3Value = it }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 1.dp
        )

        Row {
            Text(
                text = stringResource(id = R.string.fire_button) + ": ",
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 1.dp
        )

        Row {
            Text(
                text = stringResource(id = R.string.fire_button) + ": ",
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 1.dp
        )

        ElevatedButton(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.copy),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}