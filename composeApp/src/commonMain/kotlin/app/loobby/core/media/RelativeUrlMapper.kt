package app.loobby.core.media

import coil3.map.Mapper
import coil3.request.Options

class RelativeUrlMapper(private val baseUrl: String) : Mapper<String, String> {
    override fun map(data: String, options: Options): String? {
        return if (data.startsWith("/")) {
            baseUrl.trimEnd('/') + data
        } else {
            null // não altera, segue normal
        }
    }
}