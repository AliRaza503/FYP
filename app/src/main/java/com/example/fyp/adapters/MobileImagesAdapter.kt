package com.example.fyp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp.MainViewModel
import com.example.fyp.Photo
import com.example.fyp.databinding.MobileImageItemBinding


class MobileImagesAdapter(
    private val cardClicked: (MobileImageViewHolder, Photo) -> Unit,
    private val viewModel: MainViewModel,
    private val imageAdded: () -> Unit
) :
    ListAdapter<Photo, MobileImagesAdapter.MobileImageViewHolder>(Companion) {
    companion object : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(
            oldItem: Photo,
            newItem: Photo
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Photo,
            newItem: Photo
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MobileImageViewHolder {
        val binding =
            MobileImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MobileImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MobileImageViewHolder, position: Int) {
        val photo = currentList[position]
        holder.binding.apply {
            imageView.setImageURI(photo.contentUri).also {
                imageView.setOnLongClickListener {
                    cardClicked(holder, photo)
                    true
                }
                imageView.setOnClickListener {
                    if (viewModel.selectedImages.size > 0) {
                        cardClicked(holder, photo)
                    }
                }
            }

            imageName.text = photo.name.substringBeforeLast(".")

            imageName.apply {
                setOnLongClickListener {
                    cardClicked(holder, photo)
                    true
                }
                setOnClickListener {
                    if (viewModel.selectedImages.size > 0) {
                        cardClicked(holder, photo)
                    }
                }
            }

            if (viewModel.selectedImages.contains(photo)) {
                itemCardView.isChecked = true
                itemCardView.setCardBackgroundColor(
                    itemCardView.context.getColor(
                        com.example.fyp.R.color.selected_item_bg
                    )
                )
            } else {
                itemCardView.isChecked = false
                itemCardView.setCardBackgroundColor(
                    itemCardView.context.getColor(
                        com.example.fyp.R.color.opaque
                    )
                )
            }
        }

        imageAdded()
    }


    override fun getItemCount(): Int = currentList.size

    inner class MobileImageViewHolder(val binding: MobileImageItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}