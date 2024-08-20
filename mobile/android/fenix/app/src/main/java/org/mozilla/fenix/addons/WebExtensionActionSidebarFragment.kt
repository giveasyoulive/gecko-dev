package org.mozilla.fenix.addons


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import mozilla.components.browser.state.action.WebExtensionAction
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.engine.window.WindowRequest
import org.mozilla.fenix.R
import org.mozilla.fenix.databinding.DownloadDialogLayoutBinding
import org.mozilla.fenix.databinding.FragmentAddOnInternalSettingsBinding
import org.mozilla.fenix.ext.requireComponents

/**
 * A fragment to show the web extension action popup with [EngineView].
 */
@ExperimentalCoroutinesApi
class WebExtensionActionSidebarFragment : AddonPopupBaseFragment(), EngineSession.Observer {

    var callback: SidebarListener? = null

    interface SidebarListener {
        fun onOpenSidebar()
        fun onCloseSidebar()
    }

    private val args by navArgs<WebExtensionActionPopupFragmentArgs>()
    private val coreComponents by lazy { requireComponents.core }
    private val safeArguments get() = requireNotNull(arguments)
    private var sessionConsumed
        get() = safeArguments.getBoolean("isSessionConsumed", false)
        set(value) {
            safeArguments.putBoolean("isSessionConsumed", value)
        }

    private var _binding: FragmentAddOnInternalSettingsBinding? = null
    internal val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Grab the [EngineSession] from the store when the view is created if it is available.
        coreComponents.store.state.extensions[args.webExtensionId]?.popupSession?.let {
            initializeSession(it)

        }

        return inflater.inflate(R.layout.fragment_add_on_internal_settings, container, false)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAddOnInternalSettingsBinding.bind(view)

        val session = engineSession

        // If we have the session, render it otherwise consume it from the store.
        if (session != null) {
            binding.addonSettingsEngineView.render(session)
            consumePopupSession()
        }
    }


    fun setSidebarListener(callback: SidebarListener) {
        this.callback = callback
    }

    private fun consumePopupSession() {

        coreComponents.store.dispatch(
            WebExtensionAction.UpdatePopupSessionAction(args.webExtensionId, popupSession = null)
        )
        sessionConsumed = true

    }


    override fun provideDownloadContainer(): ViewGroup {
        return binding.startDownloadDialogContainer
    }

    override fun provideDownloadDialogLayoutBinding(): DownloadDialogLayoutBinding {
        return binding.viewDynamicDownloadDialog
    }
    override fun onWindowRequest(windowRequest: WindowRequest) {
        callback?.onCloseSidebar()
    }

    override fun onLocationChange(url: String, hasUserGesture: Boolean) {
        callback?.onOpenSidebar()
    }

}

