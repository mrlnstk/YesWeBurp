package yesweburp.ui

import yesweburp.Events.programsLoaded
import yesweburp.burpApi
import yesweburp.ui.options.OptionsTab
import yesweburp.ui.programs.ProgramTab
import javax.swing.JTabbedPane

class YesWeBurpTab : JTabbedPane(TOP) {

    private var programTab: ProgramTab? = null

    init {
        add("Options", OptionsTab())

        programsLoaded.listen { programs ->
            programTab?.let(::remove)
            programTab = ProgramTab(programs).also {
                add("Programs", it)
                burpApi.userInterface().applyThemeToComponent(it)
            }
        }

        burpApi.userInterface().applyThemeToComponent(this)
    }
}
