package com.example.silversimon_projetindiv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.silversimon_projetindiv.databinding.ItemPhotoBinding

/**
 * Adapter for the RecyclerView that displays the photos stored in the internal memory of the application.
 *
 * This adapter is used to display the photos in the RecyclerView of the [InternalStorageActivity].
 * It uses a ListAdapter to display the photos and handle the updates.
 *
 * @author Thibaut Lesage
 */
class InternalStoragePhotoAdapter(
    // Is called when a photo is clicked and takes an InternalStoragePhoto as a parameter which is the photo clicked.
    private val onPhotoClick: (InternalStoragePhoto) -> Unit
) : ListAdapter<InternalStoragePhoto, InternalStoragePhotoAdapter.PhotoViewHolder>(Companion) {

    // Class that represents the view of a photo.
    inner class PhotoViewHolder(val binding: ItemPhotoBinding): RecyclerView.ViewHolder(binding.root)

    // Companion object that allows to compare two photos by two criteria: the name and the bitmap.
    companion object : DiffUtil.ItemCallback<InternalStoragePhoto>() {
        /**
         * Compare only the names of the two photos.
         *
         * @author Thibaut Lesage
         * @version 1.0
         */
        override fun areItemsTheSame(oldItem: InternalStoragePhoto, newItem: InternalStoragePhoto): Boolean {
            return oldItem.name == newItem.name
        }

        /**
         * Compare the names and the bitmaps of the two photos.
         *
         * @author Thibaut Lesage
         * @version 1.0
         */
        override fun areContentsTheSame(oldItem: InternalStoragePhoto, newItem: InternalStoragePhoto): Boolean {
            return oldItem.name == newItem.name && oldItem.bmp.sameAs(newItem.bmp)
        }
    }

    /**
     * Creates a new [PhotoViewHolder] by inflating the layout [ItemPhotoBinding].
     *
     * @author Thibaut Lesage
     * @version 1.1
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(
            // Inflate the layout item_photo.xml. Inflate means to render the layout.
            ItemPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                // false
                false
            )
        )
    }


    /**
        * Binds the photo to the view holder.
        *
        * This function is called when the RecyclerView needs to display a photo.
        * It binds the photo to the view holder and sets the image and the name of the patient.
        * It also adjusts the aspect ratio of the image so that it is not distorted.
        * Finally, it allows you to click on a photo.
        *
        * @param holder The view holder that displays the photo.
        * @param position The position of the photo in the list.
        *
        * @author Thibaut Lesage
        * @version 1.3
        */

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = currentList[position]
        holder.binding.apply {
            // Display the image and the name of the patient
            ivPhoto.setImageBitmap(photo.bmp)
            tvPhotoName.text = photo.namePatient

            // Adjusts the aspect ratio of the image so that it is not distorted
            val aspectRatio = photo.bmp.width.toFloat() / photo.bmp.height.toFloat()
            // Create a new ConstraintSet and apply it to the root layout. A ConstraintSet is a set of constraints
            // and includes the ability to apply them to a ConstraintLayout.
            ConstraintSet().apply {
                clone(root)
                setDimensionRatio(ivPhoto.id, aspectRatio.toString())
                applyTo(root)
            }
            // Allows you to click on a photo
            ivPhoto.setOnLongClickListener {
                onPhotoClick(photo)
                true
            }
        }
    }

}