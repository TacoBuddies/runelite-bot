package net.tacobuddies.bot.ui

import javax.swing.DefaultListSelectionModel

class ToggleableListSelectionModel : DefaultListSelectionModel() {
    override fun addSelectionInterval(index0: Int, index1: Int) {
        if(index0 == index1) {
            if(isSelectedIndex(index0)) {
                removeSelectionInterval(index0, index0)
                return
            }
        }
        super.addSelectionInterval(index0, index1)
    }

    override fun setSelectionInterval(index0: Int, index1: Int) {
        if(index0 == index1) {
            if(isSelectedIndex(index0)) {
                removeSelectionInterval(index0, index0)
                return
            }
        }
        super.setSelectionInterval(index0, index1)
    }
}