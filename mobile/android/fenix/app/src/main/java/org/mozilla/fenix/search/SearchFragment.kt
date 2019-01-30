/* This Source Code Form is subject to the terms of the Mozilla Public
   License, v. 2.0. If a copy of the MPL was not distributed with this
   file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_search.view.*
import org.mozilla.fenix.R
import org.mozilla.fenix.mvi.ActionBusFactory
import org.mozilla.fenix.search.awesomebar.AwesomeBarAction
import org.mozilla.fenix.search.awesomebar.AwesomeBarChange
import org.mozilla.fenix.search.awesomebar.AwesomeBarComponent
import org.mozilla.fenix.search.toolbar.*

class SearchFragment : Fragment() {
    private lateinit var toolbarComponent: ToolbarComponent
    private lateinit var awesomeBarComponent: AwesomeBarComponent

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        toolbarComponent = ToolbarComponent(view.toolbar_wrapper, ActionBusFactory.get(this))
        awesomeBarComponent = AwesomeBarComponent(view.search_layout, ActionBusFactory.get(this))
        return view
    }

    override fun onResume() {
        super.onResume()
        toolbarComponent.editMode()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutComponents(view.search_layout)

        lifecycle.addObserver((toolbarComponent.uiView as ToolbarUIView).toolbarIntegration)

        view.toolbar_wrapper.clipToOutline = false

        toolbarComponent
            .getModelChangeEvents<SearchChange>()
            .subscribe {
                when (it) {
                    is SearchChange.QueryChanged -> {
                        ActionBusFactory.get(this).emit(AwesomeBarChange::class.java, AwesomeBarChange.UpdateQuery(it.query))
                    }
                }
            }

        toolbarComponent
            .getUserInteractionEvents<SearchAction>()
            .subscribe {
                when (it) {
                    is SearchAction.UrlCommitted -> transitionToBrowser()
                }
            }

        awesomeBarComponent
            .getUserInteractionEvents<AwesomeBarAction>()
            .subscribe {
                when (it) {
                    is AwesomeBarAction.ItemSelected -> transitionToBrowser()
                }
            }
    }

    private fun transitionToBrowser() {
        Navigation.findNavController(view!!.search_layout)
            .navigate(R.id.action_searchFragment_to_browserFragment, null, null)
    }
}
