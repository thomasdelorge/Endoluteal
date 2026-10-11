package fr.luteal.core.model

import androidx.annotation.StringRes
import fr.luteal.app.R

enum class BodySide {
    FRONT,
    BACK,
    INTERNAL
}

enum class BodyZone(
    val id: String,
    val side: BodySide,
    @StringRes val labelRes: Int
) {
    HEAD("head", BodySide.FRONT, R.string.body_zone_head),
    NECK("neck", BodySide.FRONT, R.string.body_zone_neck),
    CHEST_LEFT("chest_left", BodySide.FRONT, R.string.body_zone_chest_left),
    CHEST_RIGHT("chest_right", BodySide.FRONT, R.string.body_zone_chest_right),
    EPIGASTRIUM("epigastrium", BodySide.FRONT, R.string.body_zone_epigastrium),
    HYPOCHONDRIUM_LEFT("hypochondrium_left", BodySide.FRONT, R.string.body_zone_hypochondrium_left),
    HYPOCHONDRIUM_RIGHT("hypochondrium_right", BodySide.FRONT, R.string.body_zone_hypochondrium_right),
    FLANK_LEFT("flank_left", BodySide.FRONT, R.string.body_zone_flank_left),
    FLANK_RIGHT("flank_right", BodySide.FRONT, R.string.body_zone_flank_right),
    PERIUMBILICAL("periumbilical", BodySide.FRONT, R.string.body_zone_periumbilical),
    ILIAC_FOSSA_LEFT("iliac_fossa_left", BodySide.FRONT, R.string.body_zone_iliac_fossa_left),
    ILIAC_FOSSA_RIGHT("iliac_fossa_right", BodySide.FRONT, R.string.body_zone_iliac_fossa_right),
    HYPOGASTRIUM("hypogastrium", BodySide.FRONT, R.string.body_zone_hypogastrium),
    INGUINAL_LEFT("inguinal_left", BodySide.FRONT, R.string.body_zone_inguinal_left),
    INGUINAL_RIGHT("inguinal_right", BodySide.FRONT, R.string.body_zone_inguinal_right),
    VULVA("vulva", BodySide.FRONT, R.string.body_zone_vulva),
    THIGH_FRONT_LEFT("thigh_front_left", BodySide.FRONT, R.string.body_zone_thigh_front_left),
    THIGH_FRONT_RIGHT("thigh_front_right", BodySide.FRONT, R.string.body_zone_thigh_front_right),
    KNEE_LEFT("knee_left", BodySide.FRONT, R.string.body_zone_knee_left),
    KNEE_RIGHT("knee_right", BodySide.FRONT, R.string.body_zone_knee_right),
    LEG_FRONT_LEFT("leg_front_left", BodySide.FRONT, R.string.body_zone_leg_front_left),
    LEG_FRONT_RIGHT("leg_front_right", BodySide.FRONT, R.string.body_zone_leg_front_right),

    NAPE("nape", BodySide.BACK, R.string.body_zone_nape),
    SHOULDER_LEFT("shoulder_left", BodySide.BACK, R.string.body_zone_shoulder_left),
    SHOULDER_RIGHT("shoulder_right", BodySide.BACK, R.string.body_zone_shoulder_right),
    UPPER_BACK("upper_back", BodySide.BACK, R.string.body_zone_upper_back),
    LUMBAR_LEFT("lumbar_left", BodySide.BACK, R.string.body_zone_lumbar_left),
    LUMBAR_RIGHT("lumbar_right", BodySide.BACK, R.string.body_zone_lumbar_right),
    SACRUM("sacrum", BodySide.BACK, R.string.body_zone_sacrum),
    COCCYX("coccyx", BodySide.BACK, R.string.body_zone_coccyx),
    BUTTOCK_LEFT("buttock_left", BodySide.BACK, R.string.body_zone_buttock_left),
    BUTTOCK_RIGHT("buttock_right", BodySide.BACK, R.string.body_zone_buttock_right),
    THIGH_BACK_LEFT("thigh_back_left", BodySide.BACK, R.string.body_zone_thigh_back_left),
    THIGH_BACK_RIGHT("thigh_back_right", BodySide.BACK, R.string.body_zone_thigh_back_right),
    CALF_LEFT("calf_left", BodySide.BACK, R.string.body_zone_calf_left),
    CALF_RIGHT("calf_right", BodySide.BACK, R.string.body_zone_calf_right),

    DEEP_VAGINA("deep_vagina", BodySide.INTERNAL, R.string.body_zone_deep_vagina),
    RECTUM("rectum", BodySide.INTERNAL, R.string.body_zone_rectum),
    BLADDER("bladder", BodySide.INTERNAL, R.string.body_zone_bladder),
    OVARY_LEFT("ovary_left", BodySide.INTERNAL, R.string.body_zone_ovary_left),
    OVARY_RIGHT("ovary_right", BodySide.INTERNAL, R.string.body_zone_ovary_right);

    companion object {
        fun fromId(id: String): BodyZone? = entries.firstOrNull { it.id == id }
    }
}
