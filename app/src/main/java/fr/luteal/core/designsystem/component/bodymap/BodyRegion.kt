package fr.luteal.core.designsystem.component.bodymap

import androidx.annotation.StringRes
import fr.luteal.app.R
import fr.luteal.core.model.BodyZone

enum class BodyRegion(@StringRes val labelRes: Int) {
    HEAD(R.string.body_region_head),
    TORSO(R.string.body_region_torso),
    BACK(R.string.body_region_back),
    LIMBS(R.string.body_region_limbs),
    INTERNAL(R.string.body_region_internal)
}

fun BodyZone.region(): BodyRegion = when (this) {
    BodyZone.HEAD, BodyZone.NECK, BodyZone.NAPE -> BodyRegion.HEAD
    BodyZone.SHOULDER_FRONT_LEFT, BodyZone.SHOULDER_FRONT_RIGHT,
    BodyZone.CHEST_LEFT, BodyZone.CHEST_RIGHT,
    BodyZone.EPIGASTRIUM, BodyZone.HYPOCHONDRIUM_LEFT, BodyZone.HYPOCHONDRIUM_RIGHT,
    BodyZone.FLANK_LEFT, BodyZone.FLANK_RIGHT, BodyZone.PERIUMBILICAL,
    BodyZone.ILIAC_FOSSA_LEFT, BodyZone.ILIAC_FOSSA_RIGHT, BodyZone.HYPOGASTRIUM,
    BodyZone.INGUINAL_LEFT, BodyZone.INGUINAL_RIGHT, BodyZone.VULVA -> BodyRegion.TORSO
    BodyZone.SHOULDER_LEFT, BodyZone.SHOULDER_RIGHT, BodyZone.UPPER_BACK,
    BodyZone.LUMBAR_LEFT, BodyZone.LUMBAR_RIGHT, BodyZone.SACRUM, BodyZone.COCCYX,
    BodyZone.BUTTOCK_LEFT, BodyZone.BUTTOCK_RIGHT -> BodyRegion.BACK
    BodyZone.THIGH_FRONT_LEFT, BodyZone.THIGH_FRONT_RIGHT,
    BodyZone.KNEE_LEFT, BodyZone.KNEE_RIGHT,
    BodyZone.LEG_FRONT_LEFT, BodyZone.LEG_FRONT_RIGHT,
    BodyZone.THIGH_BACK_LEFT, BodyZone.THIGH_BACK_RIGHT,
    BodyZone.CALF_LEFT, BodyZone.CALF_RIGHT -> BodyRegion.LIMBS
    BodyZone.DEEP_VAGINA, BodyZone.RECTUM, BodyZone.BLADDER,
    BodyZone.OVARY_LEFT, BodyZone.OVARY_RIGHT -> BodyRegion.INTERNAL
}
