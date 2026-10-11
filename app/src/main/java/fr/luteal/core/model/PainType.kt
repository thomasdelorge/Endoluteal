package fr.luteal.core.model

import androidx.annotation.StringRes
import fr.luteal.app.R

enum class PainType(
    val id: String,
    @StringRes val labelRes: Int
) {
    SHOOTING("shooting", R.string.pain_type_shooting),
    LANCINATING("lancinating", R.string.pain_type_lancinating),
    PENETRATING("penetrating", R.string.pain_type_penetrating),
    ELECTRIC("electric", R.string.pain_type_electric),
    STABBING("stabbing", R.string.pain_type_stabbing),
    VISE("vise", R.string.pain_type_vise),
    CRAMP("cramp", R.string.pain_type_cramp),
    TORSION("torsion", R.string.pain_type_torsion),
    PINCHING("pinching", R.string.pain_type_pinching),
    PULLING("pulling", R.string.pain_type_pulling),
    BURNING("burning", R.string.pain_type_burning),
    PRICKING("pricking", R.string.pain_type_pricking),
    TINGLING("tingling", R.string.pain_type_tingling),
    NUMBNESS("numbness", R.string.pain_type_numbness),
    HEAVINESS("heaviness", R.string.pain_type_heaviness),
    PELVIC_HEAVINESS("pelvic_heaviness", R.string.pain_type_pelvic_heaviness),
    DULL("dull", R.string.pain_type_dull);

    companion object {
        fun fromId(id: String): PainType? = entries.firstOrNull { it.id == id }
    }
}
