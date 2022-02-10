package moe.sdl.tracks.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal data class DownloadItem(
    val name: String,
    val downloaded: Long, // in bytes
    val total: Long, // in bytes
)

@Composable
internal fun DownloadList(
    data: List<DownloadItem>,
    modifier: Modifier = Modifier,
) = Box(
    modifier,
    contentAlignment = Alignment.TopStart,
) {
    LazyColumn(
        Modifier.matchParentSize(),
        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 5.dp),
    ) {
        items(data.size + 1) {
            Row {
                Text("文件名", modifier = Modifier.size(50.dp, 20.dp))
                Text("大小", modifier = Modifier.size(30.dp, 20.dp))
            }
            data.forEach {
                this@LazyColumn.DownloadListRow(it)
            }
        }
    }
}

@Composable
private fun LazyListScope.DownloadListRow(
    data: DownloadItem,
) = Row {
    Text(
        data.name,
        modifier = Modifier.size(50.dp, 20.dp)
    )
    Text(
        "${data.downloaded}/${data.total}",
        modifier = Modifier.size(30.dp, 20.dp)
    )
}
