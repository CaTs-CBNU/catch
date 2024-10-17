package com.cbnu.cat_ch.story.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.cbnu.cat_ch.databinding.DialogEditCharacterRoleBinding
import com.cbnu.cat_ch.story.data.CharacterRole
import com.cbnu.cat_ch.databinding.ItemCharacterRoleBinding
import com.cbnu.cat_ch.story.viewmodel.StoryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CharacterRoleAdapter(
    private val context: Context,
    private var characterRoles: List<CharacterRole>
) : RecyclerView.Adapter<CharacterRoleAdapter.CharacterRoleViewHolder>() {

    inner class CharacterRoleViewHolder(val binding: ItemCharacterRoleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterRoleViewHolder {
        val binding = ItemCharacterRoleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CharacterRoleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CharacterRoleViewHolder, position: Int) {
        val characterRole = characterRoles[position]
        holder.binding.tvCharacterName.text = characterRole.characterName
        holder.binding.tvCharacterRole.text = characterRole.characterRole

        holder.itemView.setOnClickListener { showEditDialog(position) }
    }

    override fun getItemCount(): Int = characterRoles.size

    private fun showEditDialog(position: Int) {
        val characterRole = characterRoles[position]

        // Inflate the dialog layout with view binding
        val dialogBinding = DialogEditCharacterRoleBinding.inflate(LayoutInflater.from(context))

        // Set current values
        dialogBinding.etEditCharacterName.setText(characterRole.characterName)
        dialogBinding.etEditCharacterRole.setText(characterRole.characterRole)

        // Build and show the dialog
        MaterialAlertDialogBuilder(context)
            .setTitle("등장인물 수정하기")
            .setView(dialogBinding.root)
            .setPositiveButton("수정") { dialog, _ ->
                val newName = dialogBinding.etEditCharacterName.text.toString()
                val newRole = dialogBinding.etEditCharacterRole.text.toString()
                updateCharacterRole(position, newName, newRole)
                dialog.dismiss()
            }
            .setNeutralButton("삭제") { dialog, _ ->
                deleteCharacterRole(position)
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    private fun deleteCharacterRole(position: Int) {
        val updatedList = characterRoles.toMutableList()
        updatedList.removeAt(position)
        characterRoles = updatedList
        notifyItemRemoved(position)

        // ViewModel에 업데이트된 리스트 반영
        (context as? FragmentActivity)?.let {
            val viewModel = ViewModelProvider(it)[StoryViewModel::class.java]
            viewModel.setCharacterRoles(updatedList)
        }
    }
    private fun updateCharacterRole(position: Int, newName: String, newRole: String) {
        val updatedList = characterRoles.toMutableList()
        updatedList[position] = CharacterRole(newName, newRole)
        characterRoles = updatedList
        notifyItemChanged(position)

        // ViewModel에 업데이트된 리스트 반영
        (context as? FragmentActivity)?.let {
            val viewModel = ViewModelProvider(it)[StoryViewModel::class.java]
            viewModel.setCharacterRoles(updatedList)
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<CharacterRole>) {
        characterRoles = newList
        notifyDataSetChanged()
    }
}
