package com.example.individualproject.ui.fragment

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.individualproject.R
import com.example.individualproject.adapter.ExpenseLogAdapter
import com.example.individualproject.databinding.FragmentExpenseLogBinding
import com.example.individualproject.repository.ExpenseLogRepositoryImpl
import com.example.individualproject.viewmodel.ExpenseLogViewModel
import android.graphics.Paint
import com.example.individualproject.ui.activity.AddLogActivity


class ExpenseLogFragment : Fragment() {
    private var _binding: FragmentExpenseLogBinding? = null
    private val binding get() = _binding!!  // Prevent memory leaks

    private lateinit var ExpenseViewModel: ExpenseLogViewModel
    private lateinit var adapter: ExpenseLogAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseLogBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ExpenseLogAdapter(requireContext(), ArrayList())
        val repo = ExpenseLogRepositoryImpl()
        ExpenseViewModel = ExpenseLogViewModel(repo)

        // Set up RecyclerView
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())


        // Observe Expense Logs
        ExpenseViewModel.getExpenseAllLog()
        ExpenseViewModel.allExpense.observe(viewLifecycleOwner) { logs ->
            logs?.let { adapter.updateData(it)
                adapter.notifyDataSetChanged() }
        }


        // Observe Loading State
        ExpenseViewModel.loadingState.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }


        // Swipe-to-Delete with Red Background & Icon
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            private val background = ColorDrawable(Color.RED)
            private val paint = Paint().apply { color = Color.WHITE }
            private var deleteIcon: Drawable? = null

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return
                }
                val logId = adapter.getExpenseLogId(position)

                ExpenseViewModel.deleteLog(logId) { success, message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    if (success) {
                        ExpenseViewModel.getExpenseAllLog()
                    } else {
                        adapter.notifyItemChanged(position) // Restore item if deletion failed
                    }
                }
            }


            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val backgroundCornerOffset = 20

                if (dX > 0) { // Swiping right
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt() + backgroundCornerOffset, itemView.bottom)
                } else if (dX < 0) { // Swiping left
                    background.setBounds(itemView.right + dX.toInt() - backgroundCornerOffset, itemView.top, itemView.right, itemView.bottom)
                } else {
                    background.setBounds(0, 0, 0, 0)
                }

                background.draw(c)


                // Draw delete icon
                deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_delete_forever_24)
                deleteIcon?.let {
                    val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                    val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                    val iconBottom = iconTop + it.intrinsicHeight

                    if (dX > 0) { // Swiping right
                        val iconLeft = itemView.left + iconMargin
                        val iconRight = iconLeft + it.intrinsicWidth
                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    } else if (dX < 0) { // Swiping left
                        val iconRight = itemView.right - iconMargin
                        val iconLeft = iconRight - it.intrinsicWidth
                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    }
                    it.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })

        // Attach swipe-to-delete to RecyclerView
        itemTouchHelper.attachToRecyclerView(binding.recycler)

        // Floating Action Button to Add New Log
        binding.floatingActionButton2.setOnClickListener {
            val intent = Intent(activity, AddLogActivity::class.java)
            startActivity(intent)
        }

        // Adjust UI for insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Prevent memory leaks
    }
    override fun onResume() {
        super.onResume()
        ExpenseViewModel.getExpenseAllLog() // Refresh the data when the fragment resumes
    }
}