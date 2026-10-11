package fr.luteal.app.navigation.settings.meds

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.luteal.app.R
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.meds.AlertSeverity
import fr.luteal.core.model.meds.Composition
import fr.luteal.core.model.meds.DrugClass
import fr.luteal.core.model.meds.InteractionRule
import fr.luteal.core.model.meds.InteractionTargetType
import fr.luteal.core.model.meds.Medication
import fr.luteal.core.model.meds.Substance
import java.util.UUID

private sealed interface MedsPage {
    data object List : MedsPage
    data class MedicationForm(val id: String?) : MedsPage
    data object Substances : MedsPage
    data class SubstanceForm(val id: String?) : MedsPage
    data object Rules : MedsPage
    data class RuleForm(val id: String?) : MedsPage
}

@Composable
fun MedsSettingsScreen(
    onBack: () -> Unit,
    viewModel: MedicationCatalogViewModel = hiltViewModel()
) {
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val seedOffered by viewModel.seedOffered.collectAsStateWithLifecycle()
    val followupDelay by viewModel.painFollowupDelayMinutes.collectAsStateWithLifecycle()
    var current by androidx.compose.runtime.remember { mutableStateOf<MedsPage>(MedsPage.List) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(LutealSpacing.md),
        verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
    ) {
        TextButton(onClick = {
            if (current == MedsPage.List) onBack() else current = MedsPage.List
        }) {
            Text(stringResource(R.string.meds_back))
        }
        Text(
            text = stringResource(R.string.meds_disclaimer),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        when (val shown = current) {
            MedsPage.List -> MedicationList(
                catalog = catalog,
                seedOffered = seedOffered,
                followupDelay = followupDelay,
                onAcceptSeed = viewModel::acceptSeed,
                onDismissSeed = viewModel::dismissSeed,
                onFollowupDelay = viewModel::setPainFollowupDelayMinutes,
                onOpenMedication = { current = MedsPage.MedicationForm(it) },
                onAddMedication = { current = MedsPage.MedicationForm(null) },
                onOpenSubstances = { current = MedsPage.Substances },
                onOpenRules = { current = MedsPage.Rules }
            )
            is MedsPage.MedicationForm -> MedicationForm(
                medication = catalog.medications.find { it.id == shown.id },
                substances = catalog.substances,
                onSave = {
                    viewModel.saveMedication(it)
                    current = MedsPage.List
                }
            )
            MedsPage.Substances -> {
                catalog.substances.forEach { substance ->
                    TextButton(onClick = { current = MedsPage.SubstanceForm(substance.id) }) {
                        Text(substanceLabel(substance))
                    }
                }
                TextButton(onClick = { current = MedsPage.SubstanceForm(null) }) {
                    Text(stringResource(R.string.meds_add_composition))
                }
            }
            is MedsPage.SubstanceForm -> SubstanceForm(
                substance = catalog.substances.find { it.id == shown.id },
                onSave = {
                    viewModel.saveSubstance(it)
                    current = MedsPage.Substances
                }
            )
            MedsPage.Rules -> {
                catalog.rules.forEach { rule ->
                    TextButton(onClick = { current = MedsPage.RuleForm(rule.id) }) {
                        Text(rule.message)
                    }
                }
                TextButton(onClick = { current = MedsPage.RuleForm(null) }) {
                    Text(stringResource(R.string.meds_add_rule))
                }
            }
            is MedsPage.RuleForm -> RuleForm(
                rule = catalog.rules.find { it.id == shown.id },
                substances = catalog.substances,
                onSave = {
                    viewModel.saveRule(it)
                    current = MedsPage.Rules
                },
                onDelete = shown.id?.let { id ->
                    {
                        viewModel.deleteRule(id)
                        current = MedsPage.Rules
                    }
                }
            )
        }
    }
}

@Composable
private fun MedicationList(
    catalog: fr.luteal.core.model.meds.MedicationCatalog,
    seedOffered: Boolean,
    followupDelay: Int,
    onAcceptSeed: () -> Unit,
    onDismissSeed: () -> Unit,
    onFollowupDelay: (Int) -> Unit,
    onOpenMedication: (String) -> Unit,
    onAddMedication: () -> Unit,
    onOpenSubstances: () -> Unit,
    onOpenRules: () -> Unit
) {
    if (!seedOffered) {
        Text(stringResource(R.string.meds_seed_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.meds_seed_body))
        Row {
            TextButton(onClick = onAcceptSeed) { Text(stringResource(R.string.meds_seed_accept)) }
            TextButton(onClick = onDismissSeed) { Text(stringResource(R.string.meds_seed_dismiss)) }
        }
    }
    if (catalog.medications.isEmpty()) {
        Text(stringResource(R.string.meds_empty))
    }
    catalog.medications.forEach { medication ->
        val unvalidated = medication.compositions.any { composition ->
            catalog.substances.find { it.id == composition.substanceId }?.validated == false
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenMedication(medication.id) }
                .padding(vertical = LutealSpacing.xs)
        ) {
            Text(medication.name, style = MaterialTheme.typography.titleMedium)
            val detail = buildString {
                append(medication.form)
                if (medication.archived) append(" · ").append("archivé")
                if (unvalidated) append(" · ").append("à valider")
            }
            Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    TextButton(onClick = onAddMedication) { Text(stringResource(R.string.meds_add_medication)) }
    TextButton(onClick = onOpenSubstances) { Text(stringResource(R.string.meds_substances)) }
    TextButton(onClick = onOpenRules) { Text(stringResource(R.string.meds_rules)) }
    var delay by rememberSaveable { mutableStateOf(followupDelay.toString()) }
    OutlinedTextField(
        value = delay,
        onValueChange = { raw ->
            delay = raw.filter { it.isDigit() }
            delay.toIntOrNull()?.let(onFollowupDelay)
        },
        label = { Text(stringResource(R.string.meds_followup_delay)) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun MedicationForm(
    medication: Medication?,
    substances: List<Substance>,
    onSave: (Medication) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(medication?.name.orEmpty()) }
    var form by rememberSaveable { mutableStateOf(medication?.form.orEmpty()) }
    var contraindications by rememberSaveable { mutableStateOf(medication?.contraindications.orEmpty()) }
    var archived by rememberSaveable { mutableStateOf(medication?.archived ?: false) }
    var rows by androidx.compose.runtime.remember {
        mutableStateOf(
            medication?.compositions?.map { it.substanceId to it.amountMgPerUnit.toString() }
                ?: listOf("" to "")
        )
    }
    OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.meds_name)) }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(form, { form = it }, label = { Text(stringResource(R.string.meds_form)) }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(
        contraindications,
        { contraindications = it },
        label = { Text(stringResource(R.string.meds_contraindications)) },
        modifier = Modifier.fillMaxWidth()
    )
    rows.forEachIndexed { index, row ->
        Text(substances.find { it.id == row.first }?.name ?: stringResource(R.string.meds_substances))
        FlowChips(substances.map { it.id to it.name }, row.first) { picked ->
            rows = rows.toMutableList().also { it[index] = picked to row.second }
        }
        OutlinedTextField(
            value = row.second,
            onValueChange = { value ->
                rows = rows.toMutableList().also { it[index] = row.first to value }
            },
            label = { Text(stringResource(R.string.meds_amount_mg)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
    TextButton(onClick = { rows = rows + ("" to "") }) {
        Text(stringResource(R.string.meds_add_composition))
    }
    if (medication != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(if (archived) R.string.meds_restore else R.string.meds_archive))
            Switch(checked = archived, onCheckedChange = { archived = it })
        }
    }
    TextButton(onClick = {
        val compositions = rows.mapNotNull { (substanceId, amount) ->
            val mg = amount.replace(',', '.').toDoubleOrNull() ?: return@mapNotNull null
            if (substanceId.isBlank() || mg <= 0.0) null else Composition(substanceId, mg)
        }
        if (name.isBlank() || compositions.isEmpty()) return@TextButton
        onSave(
            Medication(
                id = medication?.id ?: UUID.randomUUID().toString(),
                name = name,
                form = form,
                contraindications = contraindications,
                archived = archived,
                compositions = compositions
            )
        )
    }) { Text(stringResource(R.string.meds_save)) }
}

@Composable
private fun SubstanceForm(substance: Substance?, onSave: (Substance) -> Unit) {
    var name by rememberSaveable { mutableStateOf(substance?.name.orEmpty()) }
    var drugClass by rememberSaveable { mutableStateOf(substance?.drugClass ?: DrugClass.OTHER) }
    var maxDaily by rememberSaveable { mutableStateOf(substance?.maxDailyMg?.toString().orEmpty()) }
    var maxIntake by rememberSaveable { mutableStateOf(substance?.maxPerIntakeMg?.toString().orEmpty()) }
    var interval by rememberSaveable { mutableStateOf(substance?.minIntervalMinutes?.toString().orEmpty()) }
    var validated by rememberSaveable { mutableStateOf(substance?.validated ?: false) }
    var notes by rememberSaveable { mutableStateOf(substance?.notes.orEmpty()) }
    OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.meds_name)) }, modifier = Modifier.fillMaxWidth())
    FlowChips(DrugClass.entries.map { it.name to it.name }, drugClass.name) { picked ->
        drugClass = DrugClass.valueOf(picked)
    }
    OutlinedTextField(maxDaily, { maxDaily = it }, label = { Text(stringResource(R.string.meds_max_daily)) }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(maxIntake, { maxIntake = it }, label = { Text(stringResource(R.string.meds_max_intake)) }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(interval, { interval = it }, label = { Text(stringResource(R.string.meds_interval)) }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.meds_notes)) }, modifier = Modifier.fillMaxWidth())
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.meds_validated))
        Switch(checked = validated, onCheckedChange = { validated = it })
    }
    TextButton(onClick = {
        if (name.isBlank()) return@TextButton
        onSave(
            Substance(
                id = substance?.id ?: UUID.randomUUID().toString(),
                name = name,
                drugClass = drugClass,
                maxDailyMg = maxDaily.replace(',', '.').toDoubleOrNull(),
                maxPerIntakeMg = maxIntake.replace(',', '.').toDoubleOrNull(),
                minIntervalMinutes = interval.toIntOrNull(),
                validated = validated,
                notes = notes
            )
        )
    }) { Text(stringResource(R.string.meds_save)) }
}

@Composable
private fun RuleForm(
    rule: InteractionRule?,
    substances: List<Substance>,
    onSave: (InteractionRule) -> Unit,
    onDelete: (() -> Unit)?
) {
    var typeA by rememberSaveable { mutableStateOf(rule?.targetAType ?: InteractionTargetType.CLASS) }
    var idA by rememberSaveable { mutableStateOf(rule?.targetAId.orEmpty()) }
    var typeB by rememberSaveable { mutableStateOf(rule?.targetBType ?: InteractionTargetType.CLASS) }
    var idB by rememberSaveable { mutableStateOf(rule?.targetBId.orEmpty()) }
    var severity by rememberSaveable { mutableStateOf(rule?.severity ?: AlertSeverity.WARNING) }
    var message by rememberSaveable { mutableStateOf(rule?.message.orEmpty()) }
    TargetPicker(typeA, idA, substances, { typeA = it }, { idA = it })
    TargetPicker(typeB, idB, substances, { typeB = it }, { idB = it })
    FlowChips(
        listOf(
            AlertSeverity.WARNING.name to AlertSeverity.WARNING.name,
            AlertSeverity.DANGER.name to AlertSeverity.DANGER.name
        ),
        severity.name
    ) { severity = AlertSeverity.valueOf(it) }
    OutlinedTextField(message, { message = it }, label = { Text(stringResource(R.string.meds_rule_message)) }, modifier = Modifier.fillMaxWidth())
    TextButton(onClick = {
        if (message.isBlank() || idA.isBlank() || idB.isBlank()) return@TextButton
        onSave(
            InteractionRule(
                id = rule?.id ?: UUID.randomUUID().toString(),
                targetAType = typeA,
                targetAId = idA,
                targetBType = typeB,
                targetBId = idB,
                severity = severity,
                message = message
            )
        )
    }) { Text(stringResource(R.string.meds_save)) }
    if (onDelete != null) {
        TextButton(onClick = onDelete) { Text(stringResource(R.string.meds_delete)) }
    }
}

@Composable
private fun TargetPicker(
    type: InteractionTargetType,
    id: String,
    substances: List<Substance>,
    onType: (InteractionTargetType) -> Unit,
    onId: (String) -> Unit
) {
    FlowChips(
        listOf(
            InteractionTargetType.SUBSTANCE.name to stringResource(R.string.meds_target_substance),
            InteractionTargetType.CLASS.name to stringResource(R.string.meds_target_class)
        ),
        type.name,
        onType = { onType(InteractionTargetType.valueOf(it)) }
    )
    val options = if (type == InteractionTargetType.SUBSTANCE) {
        substances.map { it.id to it.name }
    } else {
        DrugClass.entries.map { it.name to it.name }
    }
    FlowChips(options, id, onType = onId)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowChips(
    options: List<Pair<String, String>>,
    selected: String,
    onType: (String) -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
        options.forEach { (id, label) ->
            FilterChip(selected = id == selected, onClick = { onType(id) }, label = { Text(label) })
        }
    }
}

@Composable
private fun substanceLabel(substance: Substance): String {
    val badge = if (substance.validated) "" else " · ${stringResource(R.string.meds_to_validate)}"
    return substance.name + badge
}
