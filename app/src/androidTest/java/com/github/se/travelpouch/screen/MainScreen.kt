package com.github.se.travelpouch.screen

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import com.github.se.travelpouch.resources.C
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class MainScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<MainScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag(C.Tag.main_screen_container) }) {

  val simpleText: KNode = child { hasTestTag(C.Tag.greeting) }
}
