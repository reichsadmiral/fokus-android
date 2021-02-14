package com.isaiahvonrundstedt.fokus.features.archived.subject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemArchivedSubjectBinding
import com.isaiahvonrundstedt.fokus.features.archived.ArchivedAdapter
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage

class ArchivedSubjectAdapter(private val listener: ArchivedItemClickListener)
    : ArchivedAdapter<SubjectPackage, ArchivedSubjectAdapter.ArchivedSubjectViewHolder>(SubjectPackage.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchivedSubjectViewHolder {
        val binding = LayoutItemArchivedSubjectBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ArchivedSubjectViewHolder(binding.root, listener)
    }

    override fun onBindViewHolder(holder: ArchivedSubjectViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class ArchivedSubjectViewHolder(itemView: View, private val listener: ArchivedItemClickListener)
        : BaseViewHolder(itemView) {
        private val binding = LayoutItemArchivedSubjectBinding.bind(itemView)

        override fun <T> onBind(t: T) {
            if (t is SubjectPackage) {
                with(t.subject) {
                    binding.root.transitionName = subjectID
                    binding.tagView.setImageDrawable(tintDrawable(binding.tagView.drawable))
                    binding.titleView.text = code
                    binding.summaryView.text = description
                }

                binding.root.setOnClickListener {
                    listener.onArchivedItemClicked(t)
                }
            }
        }
    }
}