package fr.luteal.core.designsystem.component.bodymap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.luteal.app.R
import fr.luteal.core.designsystem.component.PainScale
import fr.luteal.core.designsystem.component.painIntensityColor
import fr.luteal.core.designsystem.theme.LutealSpacing
import fr.luteal.core.model.BodySide
import fr.luteal.core.model.BodyZone
import fr.luteal.core.model.PainMark
import fr.luteal.core.model.PainType
import java.util.UUID

@Composable
fun BodyMap(
    heat: Map<BodyZone, Int>,
    modifier: Modifier = Modifier
) {
    var side by remember { mutableStateOf(BodySide.FRONT) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)) {
        SideToggle(side = side, onSide = { side = it })
        ZoneCanvas(heat = heat, side = side, onZoneTap = null)
        InternalZoneRow(heat = heat, onZone = {})
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BodyMap(
    marks: List<PainMark>,
    onMarksChange: (List<PainMark>) -> Unit,
    modifier: Modifier = Modifier
) {
    var side by remember { mutableStateOf(BodySide.FRONT) }
    var listMode by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<BodyZone?>(null) }
    val heat = remember(marks) { marks.toHeat() }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)) {
        if (listMode) {
            ZoneList(heat = heat, onZone = { editing = it })
        } else {
            SideToggle(side = side, onSide = { side = it })
            ZoneCanvas(heat = heat, side = side, onZoneTap = { editing = it })
            InternalZoneRow(heat = heat, onZone = { editing = it })
        }
        TextButton(onClick = { listMode = !listMode }) {
            Text(
                text = stringResource(
                    if (listMode) R.string.body_map_silhouette_mode else R.string.body_map_list_mode
                )
            )
        }
    }
    editing?.let { zone ->
        PainMarkSheet(
            zone = zone,
            existing = marks.firstOrNull { it.zone == zone.id },
            onDismiss = { editing = null },
            onSave = { mark ->
                onMarksChange(marks.filterNot { it.zone == zone.id } + mark)
                editing = null
            },
            onRemove = {
                onMarksChange(marks.filterNot { it.zone == zone.id })
                editing = null
            }
        )
    }
}

@Composable
private fun SideToggle(side: BodySide, onSide: (BodySide) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
        FilterChip(
            selected = side == BodySide.FRONT,
            onClick = { onSide(BodySide.FRONT) },
            label = { Text(stringResource(R.string.body_side_front)) }
        )
        FilterChip(
            selected = side == BodySide.BACK,
            onClick = { onSide(BodySide.BACK) },
            label = { Text(stringResource(R.string.body_side_back)) }
        )
    }
}

@Composable
private fun ZoneCanvas(
    heat: Map<BodyZone, Int>,
    side: BodySide,
    onZoneTap: ((BodyZone) -> Unit)?
) {
    val shapes = BodyMapGeometry.shapes(side)
    val paths = remember(side) { shapes.map { it.toPath() } }
    val ink = MaterialTheme.colorScheme.onSurface
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(BodyMapGeometry.VIEWBOX_WIDTH / BodyMapGeometry.VIEWBOX_HEIGHT)
            .then(
                if (onZoneTap == null) {
                    Modifier
                } else {
                    Modifier.pointerInput(side) {
                        detectTapGestures { offset ->
                            val zone = BodyMapGeometry.zoneAt(
                                offset,
                                side,
                                Size(size.width.toFloat(), size.height.toFloat())
                            )
                            if (zone != null) onZoneTap(zone)
                        }
                    }
                }
            )
    ) {
        val scaleX = size.width / BodyMapGeometry.VIEWBOX_WIDTH
        val scaleY = size.height / BodyMapGeometry.VIEWBOX_HEIGHT
        scale(scaleX, scaleY) {
            shapes.forEachIndexed { index, shape ->
                val path = paths[index]
                val intensity = heat[shape.zone]
                val fill = if (intensity == null) {
                    ink.copy(alpha = 0.28f)
                } else {
                    painIntensityColor(intensity).copy(alpha = 0.85f)
                }
                drawPath(path, fill)
                drawPath(path, ink, style = Stroke(width = 1.2f))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InternalZoneRow(
    heat: Map<BodyZone, Int>,
    onZone: (BodyZone) -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
        BodyZone.entries.filter { it.side == BodySide.INTERNAL }.forEach { zone ->
            val label = zoneLabel(zone, heat[zone])
            FilterChip(
                selected = heat.containsKey(zone),
                onClick = { onZone(zone) },
                label = { Text(label) },
                modifier = Modifier.semantics { contentDescription = label }
            )
        }
    }
}

@Composable
private fun ZoneList(
    heat: Map<BodyZone, Int>,
    onZone: (BodyZone) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)) {
        BodyRegion.entries.forEach { region ->
            Text(
                text = stringResource(region.labelRes),
                style = MaterialTheme.typography.titleSmall
            )
            BodyZone.entries.filter { it.region() == region }.forEach { zone ->
                val label = zoneLabel(zone, heat[zone])
                Text(
                    text = label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 48.dp)
                        .semantics { contentDescription = label }
                        .clickable { onZone(zone) }
                        .padding(vertical = LutealSpacing.xs)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PainMarkSheet(
    zone: BodyZone,
    existing: PainMark?,
    onDismiss: () -> Unit,
    onSave: (PainMark) -> Unit,
    onRemove: () -> Unit
) {
    var intensity by remember(zone) { mutableStateOf(existing?.intensity) }
    var types by remember(zone) {
        mutableStateOf(existing?.painTypes?.mapNotNull(PainType::fromId)?.toSet().orEmpty())
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LutealSpacing.md),
            verticalArrangement = Arrangement.spacedBy(LutealSpacing.sm)
        ) {
            Text(
                text = stringResource(zone.labelRes),
                style = MaterialTheme.typography.titleMedium
            )
            PainScale(value = intensity, onValueChange = { intensity = it })
            FlowRow(horizontalArrangement = Arrangement.spacedBy(LutealSpacing.xs)) {
                PainType.entries.forEach { type ->
                    FilterChip(
                        selected = type in types,
                        onClick = {
                            types = if (type in types) types - type else types + type
                        },
                        label = { Text(stringResource(type.labelRes)) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(LutealSpacing.sm)) {
                if (existing != null) {
                    TextButton(onClick = onRemove) {
                        Text(stringResource(R.string.body_map_remove))
                    }
                }
                TextButton(
                    onClick = {
                        val level = intensity ?: return@TextButton
                        onSave(
                            PainMark(
                                id = existing?.id ?: UUID.randomUUID().toString(),
                                zone = zone.id,
                                intensity = level,
                                painTypes = types.map { it.id }
                            )
                        )
                    },
                    enabled = intensity != null
                ) {
                    Text(stringResource(R.string.action_save_entry))
                }
            }
        }
    }
}

@Composable
private fun zoneLabel(zone: BodyZone, intensity: Int?): String {
    val name = stringResource(zone.labelRes)
    return if (intensity == null) name else "$name ($intensity)"
}

private fun List<PainMark>.toHeat(): Map<BodyZone, Int> =
    mapNotNull { mark -> BodyZone.fromId(mark.zone)?.let { zone -> zone to mark.intensity } }.toMap()
