package fr.luteal.app.navigation.timeline

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import fr.luteal.app.R
import fr.luteal.core.data.report.TimelinePdfBuilder
import fr.luteal.core.model.report.TimelinePdfContent
import java.io.File

fun shareTimelinePdf(context: Context, content: TimelinePdfContent) {
    val dir = File(context.cacheDir, "timeline").apply { mkdirs() }
    val file = File(dir, "frise.pdf")
    file.outputStream().use { stream -> TimelinePdfBuilder.write(content, stream) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, context.getString(R.string.timeline_share)))
}
