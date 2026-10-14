package fr.luteal.app.navigation.timeline

import android.content.Context
import android.net.Uri
import fr.luteal.core.data.report.TimelinePdfBuilder
import fr.luteal.core.model.report.TimelinePdfContent

fun writeTimelinePdf(context: Context, uri: Uri, content: TimelinePdfContent) {
    context.contentResolver.openOutputStream(uri)?.use { stream ->
        TimelinePdfBuilder.write(content, stream)
    }
}
