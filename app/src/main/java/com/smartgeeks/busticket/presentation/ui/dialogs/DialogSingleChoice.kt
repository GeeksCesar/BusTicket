package com.smartgeeks.busticket.presentation.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.smartgeeks.busticket.databinding.DialogSingleChoiseBinding

class DialogSingleChoice(
    private val title: String,
    private var items: List<SingleItem>
) : DialogFragment() {

    private lateinit var binding: DialogSingleChoiseBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogSingleChoiseBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() = with(binding) {
        tvDialogTitle.text = title

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
        lvOptions.adapter = adapter

        lvOptions.setOnItemClickListener { _, _, position, _ ->
            onItemClickListener?.let { click ->
                click(items[position])
            }
            dismiss()
        }
    }

    private var onItemClickListener: ((SingleItem) -> Unit)? = null

    fun setOnItemClick(onItemClick: (SingleItem) -> Unit) {
        this.onItemClickListener = onItemClick
    }

    data class SingleItem(val id: Int, val item: String, val isSelected: Boolean) {
        override fun toString(): String {
            return item
        }
    }
}