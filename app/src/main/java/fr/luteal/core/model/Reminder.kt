package fr.luteal.core.model

enum class ReminderType {
    MEDS,
    PAIN_FOLLOWUP,
    CARE_FOLLOWUP,
    RECAP,
    APPOINTMENT_REPORT
}

enum class ReminderStatus {
    SCHEDULED,
    FIRED,
    DONE,
    DISMISSED
}
