package com.tradecore.exchange.visualizer;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;

/**
 * Real-time stock price visualization using Swing.
 * Displays live price movements with grid, axes, and interactive hover tooltips.
 */
public class GraphVisualizer extends JFrame {

    public static GraphVisualizer instance;
    private static final LinkedList<BigDecimal> stockPrices = new LinkedList<>();
    private static final LinkedList<Integer> times = new LinkedList<>();
    private static GraphPanel graphPanel;

    public static final int MAX_POINTS = 20000;
    private static final BigDecimal MIN_VISIBLE_RANGE = new BigDecimal("1e-5");

    /**
     * Initialize the visualizer window with default size and settings.
     */
    private GraphVisualizer() {
        setTitle("Stock Exchange - Real-time Price Movement");
        setSize(1400, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        graphPanel = new GraphPanel();
        add(graphPanel);

        setVisible(true);
    }

    /**
     * Start the visualizer (thread-safe singleton).
     */
    public static void start() {
        if (instance == null) {
            SwingUtilities.invokeLater(() -> instance = new GraphVisualizer());
        }
    }

    /**
     * Update the graph with a new stock price.
     * @param newPrice the latest stock price
     */
    public static void updateStockPrice(BigDecimal newPrice) {
        if (newPrice == null) return;
        synchronized (stockPrices) {
            if (stockPrices.size() >= MAX_POINTS) {
                stockPrices.poll();
                times.poll();
            }
            stockPrices.add(newPrice.setScale(8, RoundingMode.HALF_UP));
            times.add(times.isEmpty() ? 0 : times.getLast() + 1);
        }
        if (graphPanel != null) {
            SwingUtilities.invokeLater(graphPanel::repaint);
        }
    }

    /**
     * Main rendering panel with price chart, grid, axes, and tooltips.
     */
    private static class GraphPanel extends JPanel implements MouseInputListener {
        private static final int MARGIN = 80;
        private static final int TOP_MARGIN = 60;

        private int mouseX = -1;
        private int mouseY = -1;
        private boolean showTooltip = false;

        public GraphPanel() {
            setBackground(new Color(240, 240, 245));
            setDoubleBuffered(true);
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Enable rendering quality
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            // Get data copy (thread-safe)
            LinkedList<BigDecimal> pricesCopy;
            int dataSize;
            synchronized (stockPrices) {
                pricesCopy = new LinkedList<>(stockPrices);
                dataSize = stockPrices.size();
            }

            // Draw components in order
            drawBackground(g2d);
            drawAxes(g2d);
            drawGridLines(g2d);
            drawYAxisLabels(g2d, pricesCopy);

            if (!pricesCopy.isEmpty()) {
                plotStockData(g2d, pricesCopy);
                drawStats(g2d, pricesCopy, dataSize);
                if (showTooltip) {
                    drawTooltip(g2d, pricesCopy);
                }
            } else {
                drawWaitingMessage(g2d);
            }
        }

        /**
         * Draw gradient background for chart area.
         */
        private void drawBackground(Graphics2D g2d) {
            int width = getWidth();
            int height = getHeight();
            int chartBottom = height - MARGIN;

            GradientPaint gradient = new GradientPaint(
                    0, TOP_MARGIN, new Color(250, 250, 255),
                    0, chartBottom, new Color(245, 245, 250)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(MARGIN, TOP_MARGIN, width - 2 * MARGIN, chartBottom - TOP_MARGIN);
        }

        /**
         * Draw X and Y axes with labels.
         */
        private void drawAxes(Graphics2D g2d) {
            int width = getWidth();
            int height = getHeight();
            int chartBottom = height - MARGIN;
            int chartRight = width - MARGIN;

            g2d.setColor(new Color(60, 60, 60));
            g2d.setStroke(new BasicStroke(2));

            // Axes
            g2d.drawLine(MARGIN, chartBottom, chartRight, chartBottom);
            g2d.drawLine(MARGIN, TOP_MARGIN, MARGIN, chartBottom);

            // Labels
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("Time →", chartRight - 60, chartBottom + 35);

            // Rotated Y-axis label
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.rotate(-Math.PI / 2);
            g2dCopy.drawString("Price (₹)", -(chartBottom + TOP_MARGIN) / 2 - 30, 25);
            g2dCopy.dispose();
        }

        /**
         * Draw dashed grid lines for reference.
         */
        private void drawGridLines(Graphics2D g2d) {
            int width = getWidth();
            int height = getHeight();
            int chartBottom = height - MARGIN;
            int chartRight = width - MARGIN;
            int chartHeight = chartBottom - TOP_MARGIN;

            g2d.setColor(new Color(220, 220, 220));
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{5}, 0));

            // Horizontal grid
            for (int i = 1; i <= 5; i++) {
                int y = TOP_MARGIN + (chartHeight * i / 6);
                g2d.drawLine(MARGIN, y, chartRight, y);
            }

            // Vertical grid
            for (int i = 1; i <= 8; i++) {
                int x = MARGIN + ((chartRight - MARGIN) * i / 9);
                g2d.drawLine(x, TOP_MARGIN, x, chartBottom);
            }
        }

        /**
         * Draw price values on Y-axis for easy reference.
         */
        private void drawYAxisLabels(Graphics2D g2d, LinkedList<BigDecimal> prices) {
            if (prices.isEmpty()) return;

            int height = getHeight();
            int chartBottom = height - MARGIN;
            int chartHeight = chartBottom - TOP_MARGIN;

            // Calculate min/max with sensitivity adjustment
            BigDecimal min = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal max = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
            BigDecimal range = max.subtract(min);

            if (range.compareTo(MIN_VISIBLE_RANGE) < 0) {
                BigDecimal midpoint = min.add(max).divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
                min = midpoint.subtract(MIN_VISIBLE_RANGE.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
                max = midpoint.add(MIN_VISIBLE_RANGE.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
                range = max.subtract(min);
            }

            g2d.setColor(new Color(100, 100, 100));
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            FontMetrics fm = g2d.getFontMetrics();

            // Draw 6 price labels
            for (int i = 0; i <= 5; i++) {
                BigDecimal price = min.add(range.multiply(BigDecimal.valueOf(i))
                        .divide(BigDecimal.valueOf(5), RoundingMode.HALF_UP));
                int y = chartBottom - (chartHeight * i / 5);
                String label = price.setScale(2, RoundingMode.HALF_UP).toPlainString();
                int labelWidth = fm.stringWidth(label);
                g2d.drawString(label, MARGIN - labelWidth - 10, y + 4);
            }
        }

        /**
         * Draw the main price line with color coding for trend direction.
         */
        private void plotStockData(Graphics2D g2d, LinkedList<BigDecimal> prices) {
            if (prices.isEmpty()) return;

            int width = getWidth();
            int height = getHeight();
            int chartBottom = height - MARGIN;
            int chartRight = width - MARGIN;
            int chartWidth = chartRight - MARGIN;
            int chartHeight = chartBottom - TOP_MARGIN;

            // Calculate min/max
            BigDecimal min = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal max = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
            BigDecimal range = max.subtract(min);

            if (range.compareTo(MIN_VISIBLE_RANGE) < 0) {
                BigDecimal midpoint = min.add(max).divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
                min = midpoint.subtract(MIN_VISIBLE_RANGE.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
                max = midpoint.add(MIN_VISIBLE_RANGE.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
                range = max.subtract(min);
            }

            int visiblePoints = Math.min(prices.size(), chartWidth / 4);
            int startIndex = Math.max(0, prices.size() - visiblePoints);

            if (visiblePoints <= 1) return;

            // Draw filled area under the line
            g2d.setColor(new Color(100, 150, 255, 50));
            Polygon fillPoly = new Polygon();
            fillPoly.addPoint(MARGIN, chartBottom);

            for (int i = startIndex; i < prices.size(); i++) {
                int x = MARGIN + (chartWidth * (i - startIndex) / (visiblePoints - 1));
                int y = scaleY(prices.get(i), min, range, TOP_MARGIN, chartHeight);
                fillPoly.addPoint(x, y);
            }
            fillPoly.addPoint(MARGIN + chartWidth, chartBottom);
            g2d.fillPolygon(fillPoly);

            // Draw the price line
            g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int prevX = MARGIN;
            int prevY = scaleY(prices.get(startIndex), min, range, TOP_MARGIN, chartHeight);

            for (int i = startIndex + 1; i < prices.size(); i++) {
                int x = MARGIN + (chartWidth * (i - startIndex) / (visiblePoints - 1));
                int y = scaleY(prices.get(i), min, range, TOP_MARGIN, chartHeight);

                // Color coding: Green = up, Red = down, Blue = flat
                if (y < prevY) {
                    g2d.setColor(new Color(50, 200, 50));   // Green
                } else if (y > prevY) {
                    g2d.setColor(new Color(255, 50, 50));   // Red
                } else {
                    g2d.setColor(new Color(100, 150, 255)); // Blue
                }

                g2d.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }

            // Draw current price indicator (orange dot)
            g2d.setColor(new Color(255, 100, 0));
            g2d.fillOval(prevX - 4, prevY - 4, 8, 8);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(prevX - 2, prevY - 2, 4, 4);
        }

        /**
         * Draw interactive tooltip on mouse hover showing exact price and time.
         */
        private void drawTooltip(Graphics2D g2d, LinkedList<BigDecimal> prices) {
            int width = getWidth();
            int height = getHeight();
            int chartBottom = height - MARGIN;
            int chartRight = width - MARGIN;
            int chartWidth = chartRight - MARGIN;
            int chartHeight = chartBottom - TOP_MARGIN;

            // Only show tooltip if mouse is over chart
            if (mouseX < MARGIN || mouseX > chartRight || mouseY < TOP_MARGIN || mouseY > chartBottom) {
                return;
            }

            BigDecimal min = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal max = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
            BigDecimal range = max.subtract(min);

            if (range.compareTo(MIN_VISIBLE_RANGE) < 0) {
                BigDecimal midpoint = min.add(max).divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
                min = midpoint.subtract(MIN_VISIBLE_RANGE.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
                max = midpoint.add(MIN_VISIBLE_RANGE.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
                range = max.subtract(min);
            }

            int visiblePoints = Math.min(prices.size(), chartWidth / 4);
            int startIndex = Math.max(0, prices.size() - visiblePoints);

            // Find nearest data point to cursor
            double normalizedX = (double) (mouseX - MARGIN) / chartWidth;
            int pointIndex = startIndex + (int) (normalizedX * (visiblePoints - 1));
            pointIndex = Math.max(startIndex, Math.min(prices.size() - 1, pointIndex));

            BigDecimal hoverPrice = prices.get(pointIndex);

            // Draw vertical guide line
            g2d.setColor(new Color(100, 100, 100, 100));
            g2d.drawLine(mouseX, TOP_MARGIN, mouseX, chartBottom);

            // Draw tooltip box
            String priceStr = hoverPrice.setScale(6, RoundingMode.HALF_UP).toPlainString();
            String tooltipText = "₹" + priceStr + " | Time: " + pointIndex;

            g2d.setFont(new Font("Monospace", Font.BOLD, 11));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(tooltipText);
            int textHeight = fm.getAscent();

            int tooltipX = mouseX + 10;
            int tooltipY = mouseY - 10;

            // Keep tooltip in bounds
            if (tooltipX + textWidth + 10 > width) {
                tooltipX = mouseX - textWidth - 10;
            }
            if (tooltipY - textHeight < TOP_MARGIN) {
                tooltipY = mouseY + 20;
            }

            // Draw tooltip background
            g2d.setColor(new Color(255, 255, 200, 240));
            g2d.fillRect(tooltipX - 5, tooltipY - textHeight - 5, textWidth + 10, textHeight + 10);

            g2d.setColor(Color.BLACK);
            g2d.drawRect(tooltipX - 5, tooltipY - textHeight - 5, textWidth + 10, textHeight + 10);

            // Draw tooltip text
            g2d.drawString(tooltipText, tooltipX, tooltipY);
        }

        /**
         * Draw waiting message when no data is available.
         */
        private void drawWaitingMessage(Graphics2D g2d) {
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Arial", Font.ITALIC, 16));
            String msg = "Waiting for price data...";
            FontMetrics fm = g2d.getFontMetrics();
            int msgWidth = fm.stringWidth(msg);
            g2d.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2);
        }

        /**
         * Draw statistics panel showing current, min, max, and spread.
         */
        private void drawStats(Graphics2D g2d, LinkedList<BigDecimal> prices, int dataSize) {
            BigDecimal min = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal max = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal current = prices.getLast();
            BigDecimal range = max.subtract(min);

            int panelX = getWidth() - MARGIN - 280;
            int panelY = 10;

            // Draw stats panel background
            g2d.setColor(new Color(255, 255, 255, 230));
            g2d.fillRoundRect(panelX, panelY, 270, 140, 10, 10);
            g2d.setColor(new Color(100, 100, 100));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(panelX, panelY, 270, 140, 10, 10);

            // Draw title
            g2d.setFont(new Font("Monospace", Font.BOLD, 12));
            g2d.setColor(new Color(50, 50, 50));
            int textX = panelX + 15;
            int textY = panelY + 25;
            int lineHeight = 22;
            g2d.drawString("MARKET STATS", textX, textY);

            // Draw stats
            g2d.setFont(new Font("Monospace", Font.PLAIN, 11));

            g2d.setColor(new Color(0, 120, 0));
            g2d.drawString("Current: ₹" + current.setScale(6, RoundingMode.HALF_UP).toPlainString(),
                    textX, textY + lineHeight);

            g2d.setColor(new Color(200, 0, 0));
            g2d.drawString("Max:     ₹" + max.setScale(6, RoundingMode.HALF_UP).toPlainString(),
                    textX, textY + lineHeight * 2);

            g2d.setColor(new Color(0, 100, 200));
            g2d.drawString("Min:     ₹" + min.setScale(6, RoundingMode.HALF_UP).toPlainString(),
                    textX, textY + lineHeight * 3);

            g2d.setColor(new Color(100, 100, 100));
            g2d.drawString("Spread:  ₹" + range.setScale(6, RoundingMode.HALF_UP).toPlainString(),
                    textX, textY + lineHeight * 4);
            g2d.drawString("Points:  " + dataSize, textX, textY + lineHeight * 5);
        }

        /**
         * Convert price value to Y-axis pixel position.
         */
        private int scaleY(BigDecimal price, BigDecimal min, BigDecimal range,
                           int topMargin, int chartHeight) {
            if (range.compareTo(BigDecimal.ZERO) == 0) {
                return topMargin + chartHeight / 2;
            }
            BigDecimal normalized = price.subtract(min)
                    .divide(range, 10, RoundingMode.HALF_UP);
            int y = topMargin + chartHeight - (int) (normalized.doubleValue() * chartHeight);
            return Math.max(topMargin, Math.min(topMargin + chartHeight, y));
        }

        // === Mouse Event Handlers ===

        @Override
        public void mouseMoved(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
            showTooltip = true;
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            showTooltip = true;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            showTooltip = false;
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {}

        @Override
        public void mouseClicked(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}
    }
}
