package com.lehmann.pewpew;

public interface IFieldRenderer {

	void doDraw();

	void drawLine(float x1, float y1, float x2, float y2, int red,
			int g, int b);

	void fillCircle(float cx, float cy, float radius, int red, int g,
			int b);

	void frameCircle(float cx, float cy, float radius, int red, int g,
			int b);

	int getHeight();

	int getWidth();

	void setManager(FieldViewManager manager);

}
