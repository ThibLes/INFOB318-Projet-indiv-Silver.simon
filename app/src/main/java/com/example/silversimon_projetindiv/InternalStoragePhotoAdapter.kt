package com.example.silversimon_projetindiv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.silversimon_projetindiv.databinding.ItemPhotoBinding

class InternalStoragePhotoAdapter(
    // permet de cliquer sur une photo
    private val onPhotoClick: (InternalStoragePhoto) -> Unit
) : ListAdapter<InternalStoragePhoto, InternalStoragePhotoAdapter.PhotoViewHolder>(Companion) {

    // Classe interne qui permet de lier les données de la photo à la vue.
    inner class PhotoViewHolder(val binding: ItemPhotoBinding): RecyclerView.ViewHolder(binding.root)

    // Objet qui permet de comparer les photos.
    companion object : DiffUtil.ItemCallback<InternalStoragePhoto>() {
        /**
         * Vérifie si les deux photos sont identiques.
         */
        override fun areItemsTheSame(oldItem: InternalStoragePhoto, newItem: InternalStoragePhoto): Boolean {
            return oldItem.name == newItem.name
        }

        /**
         * Vérifie si les deux photos sont identiques et ont le même contenu (bitmap).
         */
        override fun areContentsTheSame(oldItem: InternalStoragePhoto, newItem: InternalStoragePhoto): Boolean {
            return oldItem.name == newItem.name && oldItem.bmp.sameAs(newItem.bmp)
        }
    }

    /**
     * Crée un [PhotoViewHolder] et le lie à la vue [ItemPhotoBinding].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(
            ItemPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    /**
     * Lie les données de la photo à la vue [ItemPhotoBinding].
     */
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = currentList[position]
        holder.binding.apply {
            // Affiche la photo et le nom du patient
            ivPhoto.setImageBitmap(photo.bmp)
            tvPhotoName.text = photo.namePatient

            // Ajuste le ratio de l'image pour qu'elle ne soit pas déformée
            val aspectRatio = photo.bmp.width.toFloat() / photo.bmp.height.toFloat()
            ConstraintSet().apply {
                clone(root)
                setDimensionRatio(ivPhoto.id, aspectRatio.toString())
                applyTo(root)
            }
            // Permet de cliquer sur une photo
            ivPhoto.setOnLongClickListener {
                onPhotoClick(photo)
                true
            }
        }
    }

}