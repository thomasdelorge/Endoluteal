package fr.luteal.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.luteal.core.designsystem.theme.LutealSpacing

@Composable
fun EntryScaffold(
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    title: String? = null,
    titleContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    FullScreenEntry {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(LutealSpacing.md),
            verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
        ) {
            ScreenBack(onClick = onBack)
            when {
                titleContent != null -> titleContent()
                title != null -> {
                    Text(text = title, style = MaterialTheme.typography.titleLarge)
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
            ) {
                content()
            }
            FormSaveButton(onClick = onConfirm, enabled = confirmEnabled)
        }
    }
}
