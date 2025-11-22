package com.vdx.flagship

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vdx.flagship.databinding.ItemFlagBinding
import com.vdx.flagship.model.FeatureFlag
import androidx.core.graphics.toColorInt

class FlagsAdapter : ListAdapter<FeatureFlag, FlagsAdapter.FlagViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlagViewHolder {
        val binding = ItemFlagBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FlagViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FlagViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))

    }

    class FlagViewHolder(private val binding: ItemFlagBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(flag: FeatureFlag) {
            binding.tvKey.text = flag.key
            binding.tvType.text = flag.type.name

            // Status Indicator
            if (flag.enabled) {
                binding.tvStatus.text = "ENABLED"
                binding.tvStatus.setTextColor("#2E7D32".toColorInt()) // Green
                binding.cardView.strokeColor = "#2E7D32".toColorInt()
            } else {
                binding.tvStatus.text = "DISABLED"
                binding.tvStatus.setTextColor("#C62828".toColorInt()) // Red
                binding.cardView.strokeColor = "#C62828".toColorInt()
            }

            // Value handling
            binding.tvValue.text = when {
                flag.value == null -> "(null)"
                flag.value!!.isEmpty() -> "(empty string)"
                else -> flag.value
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FeatureFlag>() {
        override fun areItemsTheSame(oldItem: FeatureFlag, newItem: FeatureFlag) =
            oldItem.key == newItem.key

        override fun areContentsTheSame(oldItem: FeatureFlag, newItem: FeatureFlag) =
            oldItem == newItem
    }
}
