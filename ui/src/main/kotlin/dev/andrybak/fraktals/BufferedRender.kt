package dev.andrybak.fraktals

import java.awt.Graphics2D
import java.awt.Transparency
import java.awt.image.BufferedImage

class BufferedRender(private val r: Render) {
    private var image: BufferedImage? = null
    private var needBufferRepaint: Boolean = true

    fun paint(g: Graphics2D) {
        if (needBufferRepaint || image == null) {
            repaint(g)
            needBufferRepaint = false
        }
        g.drawImage(image, 0, 0, null)
    }

    fun needRepaint() {
        needBufferRepaint = true
    }

    private fun repaint(g: Graphics2D) {
        val w = g.clipBounds.width
        val h = g.clipBounds.height
        image = g.deviceConfiguration.createCompatibleImage(w, h, Transparency.OPAQUE)
        println("$w x $h")
        r.paint(image!!.createGraphics())
    }
}