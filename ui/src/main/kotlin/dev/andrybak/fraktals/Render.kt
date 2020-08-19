package dev.andrybak.fraktals

import java.awt.image.BufferedImage

interface Render {
    fun paint(img: BufferedImage)
}
