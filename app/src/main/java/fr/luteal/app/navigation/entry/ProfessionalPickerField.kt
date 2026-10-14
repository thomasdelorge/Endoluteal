package fr.luteal.app.navigation.entry

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.luteal.app.R
import fr.luteal.core.designsystem.component.SettingsDropdownField
import fr.luteal.core.model.professional.Professional

@Composable
fun ProfessionalPickerField(
    professionals: List<Professional>,
    selectedId: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (professionals.isEmpty()) {
        Text(
            text = stringResource(R.string.appointment_no_professional),
            modifier = modifier.fillMaxWidth()
        )
        return
    }
    val selectedLabel = professionals.find { it.id == selectedId }
        ?.let { "${it.name} · ${it.displaySpecialty()}" }
        ?: ""
    SettingsDropdownField(
        label = stringResource(R.string.appointment_professional),
        selectedLabel = selectedLabel,
        options = professionals.map { professional ->
            professional.id to "${professional.name} · ${professional.displaySpecialty()}"
        },
        onSelect = onSelected,
        modifier = modifier
    )
}
