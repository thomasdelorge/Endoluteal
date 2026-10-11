package fr.luteal.core.model.professional

enum class Specialty(val label: String) {
    GENERAL_PRACTITIONER("Médecin généraliste"),
    GYNECOLOGIST("Gynécologue"),
    ENDOMETRIOSIS_GYNECOLOGIST("Gynécologue spécialiste de l'endométriose"),
    RADIOLOGIST("Radiologue"),
    GASTROENTEROLOGIST("Gastro-entérologue"),
    UROLOGIST("Urologue"),
    PAIN_SPECIALIST("Médecin de la douleur"),
    SURGEON("Chirurgien"),
    ANESTHESIOLOGIST("Anesthésiste"),
    MIDWIFE("Sage-femme"),
    PHYSIOTHERAPIST("Kinésithérapeute"),
    OSTEOPATH("Ostéopathe"),
    PSYCHOLOGIST("Psychologue"),
    PSYCHIATRIST("Psychiatre"),
    SEXOLOGIST("Sexologue"),
    DIETITIAN("Diététicien"),
    OTHER("Autre");

    companion object {
        fun fromStored(value: String): Specialty = entries.find { it.name == value } ?: OTHER
    }
}
