import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration2(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val column = position / spanCount
        val row = position % spanCount

        if (includeEdge) {
            if (column == 0) {
                outRect.left = spacing
            } else {
                outRect.left = spacing / 2
            }
            outRect.right = spacing / 2
            if (row == 0) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        } else {
            if (column > 0) {
                outRect.left = spacing / 2
            }
            outRect.right = spacing / 2
            outRect.bottom = spacing
        }
    }
}
