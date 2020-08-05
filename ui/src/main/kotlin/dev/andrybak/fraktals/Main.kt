package dev.andrybak.fraktals

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities

class Main {
    companion object {
        const val WIDTH: Int = 800
        const val HEIGHT: Int = 600
        const val MAX_ITERATIONS: Int = 255
    }
}

data class DoublePoint(val x: Double, val y: Double)
data class DoubleRectangle(
    val left: Double,
    val right: Double,
    val bottom: Double,
    val top: Double
) {
    fun width(): Double = right - left
    fun height(): Double = top - bottom

    fun contains(x: Double, y: Double): Boolean = (left < x && x < right) && (bottom < y && y < top)
    fun contains(p: DoublePoint): Boolean = contains(p.x, p.y)
}

fun mandelbrot(c: DoublePoint, max: Int, bounds: DoubleRectangle): Int {
    var a: Double = c.x
    var b: Double = c.y
    for (i in 0..max) {
        val tmpA = (a * a) - (b * b) + c.x
        val tmpB = 2 * a * b + c.y
        if (!bounds.contains(tmpA, tmpB))
            return i
        a = tmpA
        b = tmpB
    }
    return max
}

class MandelbrotPanel(
    private val maxIterations: Int,
    private val bounds: DoubleRectangle
) : JPanel() {

    override fun paintComponent(topGraphics: Graphics?) {
        val g: Graphics2D = topGraphics!!.create() as Graphics2D
        val r: Rectangle = getBounds()
        val width: Int = r.width
        val height: Int = r.height
        g.color = Color.BLACK
        for (x in 0..width) {
            for (y in 0..height) {
                val c: DoublePoint = screenToCoords(x, y, r, bounds)
                val i = mandelbrot(c, maxIterations, bounds)
                if (i == maxIterations)
                    continue
                g.color = Color(i % 256, i % 256, i % 256)
//                g.color = Color.getHSBColor(i.toFloat() / maxIterations, 1.0f, i.toFloat() / maxIterations)
                g.drawLine(x, y, x, y)
            }
        }
        g.dispose()
    }

}

fun screenToCoords(x: Int, y: Int, screenBounds: Rectangle, bounds: DoubleRectangle): DoublePoint {
    return DoublePoint(
        bounds.left + (x - screenBounds.x) * bounds.width() / screenBounds.getWidth(),
        bounds.bottom + (screenBounds.getHeight() - y) * bounds.height() / screenBounds.getHeight()
    )
}

fun main() {
    val window = JFrame("Fraktals")
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    val bounds = DoubleRectangle(-2.0, 2.0, -1.5, 1.5)
    window.contentPane = MandelbrotPanel(
        Main.MAX_ITERATIONS,
        bounds
    )

    window.setSize(Main.WIDTH, Main.HEIGHT)
    window.isVisible = true
    SwingUtilities.invokeLater {
        val windowBounds = window.bounds
        println(windowBounds)
        println(screenToCoords(0, 0, windowBounds, bounds))
        println(screenToCoords(400, 300, windowBounds, bounds))
        println(screenToCoords(windowBounds.width, windowBounds.height, windowBounds, bounds))
    }
}
