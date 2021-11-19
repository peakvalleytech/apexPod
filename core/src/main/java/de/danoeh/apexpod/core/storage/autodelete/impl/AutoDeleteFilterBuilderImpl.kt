package de.danoeh.apexpod.core.storage.autodelete.impl

import de.danoeh.apexpod.core.storage.autodelete.AutoDeleteRule
import de.danoeh.apexpod.core.storage.autodelete.AutoDeleteFilter

class AutoDeleteFilterBuilderImpl() : AutoDeleteFilter.Builder {
    private val rules : MutableList<AutoDeleteRule> = mutableListOf()
    override fun addAutoDeleteRule(rule: AutoDeleteRule): AutoDeleteFilter.Builder {
        rules.add(rule)
        return this
    }

    override fun build(): AutoDeleteFilter {
        return AutoDeleteFilterImpl(rules)
    }
}