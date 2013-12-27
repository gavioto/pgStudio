package com.openscg.pgstudio.client.utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.openscg.pgstudio.client.PgStudio;

public class ExtendedDialogBox extends DialogBox {

	private Node closeEventTarget = null;

	Label caption = new Label();

	public ExtendedDialogBox() {
		caption.setStyleName("Caption");
		caption.setHeight("20px");
		
		Element dialogTopCenter = getCellElement(0, 1);
		dialogTopCenter.setInnerHTML("");
		dialogTopCenter.appendChild(caption.getElement());

		Element dialogTopRight = getCellElement(0, 2);

		ImageResource close = PgStudio.Images.close();
		String uri = close.getSafeUri().asString();

		dialogTopRight
				.setInnerHTML("<div style=\"margin-left:-25px;margin-top: 7px;\">"
						+ "<img src=\""
						+ uri
						+ "\" height=\"20px\"/>"
						+ "</div>");

		closeEventTarget = dialogTopRight.getChild(0).getChild(0);
	}

	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent event) {
		NativeEvent nativeEvent = event.getNativeEvent();

		if (!event.isCanceled() && (event.getTypeInt() == Event.ONCLICK)
				&& isCloseEvent(nativeEvent)) {
			this.hide();
		}
		super.onPreviewNativeEvent(event);
	}

	private boolean isCloseEvent(NativeEvent event) {
		return event.getEventTarget().equals(closeEventTarget);
	}

	@Override
	public void setText(String text) {
		caption.setText(text);
	}

	public String getText() {
		return caption.getText();
	}
}
