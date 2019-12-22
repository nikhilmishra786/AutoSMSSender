package com.infomantri.autosms.send.adapter

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.infomantri.autosms.send.R

abstract class SwipeToDeleteCallback(context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24dp)
    private val favIcon = ContextCompat.getDrawable(context, R.drawable.ic_favorite_white_24dp)
    private val intrinsicDelWidth = deleteIcon?.intrinsicWidth
    private val intrinsicDelHeight = deleteIcon?.intrinsicHeight
    private val intrinsicFavWidth = favIcon?.intrinsicWidth
    private val intrinsicFavHeight = favIcon?.intrinsicHeight

    private val background = ColorDrawable()
    private val delBackgroundColor = Color.parseColor("#EF5350")
    private val favBackgroundColor = Color.parseColor("#FFA726")
    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    // Draw Delete View
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if(isCanceled){
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Draw the red delete Background
        background.color = delBackgroundColor
        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background.draw(c)

        // Calculate position of delete icon
        val iconTop = itemView.top + (itemHeight - intrinsicDelHeight!!) / 2
        val iconMargin = (itemHeight - intrinsicDelHeight) / 2
        val iconLeft = itemView.right - iconMargin - intrinsicDelWidth!!
        val iconRight = itemView.right - iconMargin
        val iconBottom = iconTop + intrinsicDelHeight

        // Draw the delete icon
        deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        deleteIcon?.draw(c)

        if (dX > 0) {   //Swipe to Right

            val favIconLeft = itemView.left + ((itemHeight - intrinsicFavHeight!!) / 2) + intrinsicFavWidth!!
            val favIconRight = itemView.left + iconMargin

            // Draw the red delete Background
            background.color = favBackgroundColor
            background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt() + 20, itemView.bottom)
            background.draw(c)

            favIcon?.setBounds(favIconLeft, iconTop, favIconRight, iconBottom)
            favIcon?.draw(c)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}