package dev.andrybak.fraktals

import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.max
import kotlin.math.min

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
    var a: Double = 0.0
    var b: Double = 0.0
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
    private var bounds: DoubleRectangle
) : JPanel() {

    private val boundsStack: Deque<DoubleRectangle> = ArrayDeque()
    private val escapeBounds = DoubleRectangle(-100.0, 100.0, -100.0, 100.0)
    private val bufferedRender = BufferedRender(object : Render {
        override fun paint(img: BufferedImage) {
            val r: Rectangle = getBounds()
            val width: Int = r.width
            val height: Int = r.height
            println("R.paint: $width x $height")
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val c: DoublePoint = screenToCoords(x, y, r, bounds)
                    val i = mandelbrot(c, maxIterations, escapeBounds)
                    if (i == maxIterations)
                        continue
                    val colorVal = i % 256
                    val rgb: Int = ((0xFF) shl 24) or
                            ((colorVal and 0xFF) shl 16) or
                            ((colorVal and 0xFF) shl 8) or
                            ((colorVal and 0xFF) shl 0)
//                g.color = Color.getHSBColor(i.toFloat() / maxIterations, 1.0f, i.toFloat() / maxIterations)
                    img.setRGB(x, y, rgb)
                }
            }
        }

    })

    fun zoom(p1: Point, p2: Point) {
        val (left, top) = screenToCoords(min(p1.x, p2.x), min(p1.y, p2.y), getBounds(), bounds)
        val (right, bottom) = screenToCoords(max(p1.x, p2.x), max(p1.y, p2.y), getBounds(), bounds)
        if (boundsStack.size < 1000)
            boundsStack.push(bounds)
        bounds = DoubleRectangle(left, right, bottom, top)
        println(bounds)
        repaintBufferedRender()
    }

    fun zoomOut() {
        if (boundsStack.isEmpty())
            return
        bounds = boundsStack.pop()
        println(bounds)
        repaintBufferedRender()
    }

    fun zoomOutAll() {
        if (boundsStack.isEmpty())
            return
        bounds = boundsStack.peekLast()
        boundsStack.clear()
        println(bounds)
        repaintBufferedRender()
    }

    private fun repaintBufferedRender() {
        bufferedRender.needRepaint()
        repaint()
    }

    override fun paintComponent(topGraphics: Graphics?) {
        val g: Graphics2D = topGraphics!!.create() as Graphics2D
        bufferedRender.paint(g)
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
    val mandelbrotPanel = MandelbrotPanel(
        Main.MAX_ITERATIONS,
        bounds
    )
    window.contentPane = mandelbrotPanel
    window.contentPane.addMouseListener(object : MouseAdapter() {
        var a: Point = Point(0, 0)
        var b: Point = Point(800, 600)

        override fun mousePressed(e: MouseEvent?) {
            if (e!!.button != MouseEvent.BUTTON1)
                return
            a = e.point
        }

        override fun mouseReleased(e: MouseEvent?) {
            if (e!!.button != MouseEvent.BUTTON1)
                return
            b = e.point
            mandelbrotPanel.zoom(a, b)
        }

        override fun mouseClicked(e: MouseEvent?) {
            if (e!!.button != MouseEvent.BUTTON3)
                return
            if (e.clickCount == 2) {
                mandelbrotPanel.zoomOutAll()
            } else {
                mandelbrotPanel.zoomOut()
            }
        }
    })

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
