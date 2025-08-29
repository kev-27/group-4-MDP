package com.example.mdp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CanvasGrid extends View {
    private static final int noOfCols = 20;
    private static final int noOfRows = 20;
    private static final int cellWidth = 35;
    private static final int cellHeight = 35;

    private static final Paint greenPaint = new Paint();
    private static final Paint blackPaint = new Paint();
    private static final Paint whitePaint = new Paint();
    private static final Paint canvasBackground = new Paint();

    public CanvasGrid(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        String backgroundColor = "#94dff2";
        String gridColor = "#366396";

        blackPaint.setColor(Color.BLACK);
        whitePaint.setColor(Color.WHITE);
        greenPaint.setColor(Color.parseColor(gridColor));
        canvasBackground.setColor(Color.parseColor(backgroundColor));
    }

    @Override
    public void onDraw(Canvas canvas) {
        // background
        for (int i = 0; i < noOfCols; i++) {
            for (int j = 0; j < noOfRows; j++) {
                canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight,
                        canvasBackground);
            }
        }

        // vertical lines
        for (int i = 1; i < noOfCols; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, noOfRows * cellHeight, greenPaint);
        }

        // horizontal lines
        for (int i = 1; i < noOfCols; i++) {
            canvas.drawLine(0, i * cellHeight, noOfCols * cellWidth, i * cellHeight, greenPaint);
        }

        // vertical grid axis
        for (int i = noOfRows - 1; i >= 0; i--) {
            canvas.drawText(String.valueOf(i), 0, cellHeight * (noOfRows - i - 1) + 15, greenPaint);
        }

        // horizontal grid axis
        for (int i = 1; i < noOfCols; i++) {
            canvas.drawText(String.valueOf(i), cellWidth * i + 5, cellHeight * 10 + 15, greenPaint);
        }
    }
}
