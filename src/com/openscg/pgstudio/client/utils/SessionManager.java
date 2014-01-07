/*
 * PostgreSQL Studio
 * 
 * Copyright (c) 2013 - 2014, Open Source Consulting Group, Inc.
 * Copyright (c) 2012 - 2013, StormDB, Inc.
 * 
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without a written agreement is
 * hereby granted, provided that the above copyright notice and this paragraph and
 * the following two paragraphs appear in all copies.
 * 
 * IN NO EVENT SHALL OPEN SOURCE CONSULTING GROUP BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST
 * PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * OPEN SOURCE CONSULTING GROUP HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * OPEN SOURCE CONSULTING GROUP SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND
 * OPEN SOURCE CONSULTING GROUP HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 */
package com.openscg.pgstudio.client.utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Event;

public class SessionManager {

	Timer countdown;
    HandlerRegistration handlerReg;
    int timeOut = 10*60000; //ten minutes max user inactivity(timeOut in millisec)
    PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);
    String logout_msg = null;

    public SessionManager() {
        countdown = new Timer() {
            @Override
            public void run() {
                // Logout
                handlerReg.removeHandler();
                countdown.cancel();
                logout("SESSION_TIMEOUT");
            }
        };
        countdown.schedule(timeOut);

        NativePreviewHandler handler = new NativePreviewHandler()	{
        	//Restart countdown on user activity like click and keypress
        	@Override
        	public void onPreviewNativeEvent(NativePreviewEvent event)	{
        		if(event.getNativeEvent().getType().equals("click") || event.getNativeEvent().getType().equals("keypress"))
        			countdown.schedule(timeOut);
        	}
        };
        handlerReg = Event.addNativePreviewHandler(handler);
        }

	public void logout(final String source)	{

		studioService.doLogout(PgStudio.getToken(), source, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				handleException(caught);
			}

			@Override
			public void onSuccess(Void result) {
				String log_msg = null;
		    	if(source.equals("SESSION_TIMEOUT"))	{
		    		log_msg = "You have been logged out due to inactivity. Please relogin or exit.";
		    		invalidateSession();
					showAlert(log_msg, true);
		    	}
		    	if(source.equals("USER_INITIATED"))	{
		    		invalidateSession();
		    	}

		    	if(source.equals("WINDOW_CLOSE"))
		    		log_msg = source;

			}
		});
	}

	private void invalidateSession()	{
		studioService.invalidateSession(new AsyncCallback<Void>() {
		@Override
		public void onFailure(Throwable caught) {
			handleException(caught);
		}
		@Override
		public void onSuccess(Void result) {
		}
	});
	}

    private final DialogBox showAlert(String msg, final boolean reloadWindow)	{

    final DialogBox alert = new DialogBox();
    VerticalPanel pane = new VerticalPanel();
    Label text = new Label(msg);

    Button ok = new Button("OK");
    ok.addClickHandler(new ClickHandler(){
		@Override
		public void onClick(ClickEvent event) {
			alert.hide();
			if(reloadWindow)
				Window.Location.reload();
		}
    });

    pane.add(text);
    pane.add(ok);
    alert.add(pane);
    alert.setGlassEnabled(true);
    alert.show();
    alert.center();

    return alert;
    }

    private void handleException(Throwable caught)	{

		try {
			throw caught;
			} catch (IncompatibleRemoteServiceException e) {
				showAlert(e.getMessage(), false);
			} catch (InvocationException e) {
				showAlert("Unable to contact the PostgreSQL server. There may be a problem with your network connection. " +
						"Please exit pgStudio by closing your browser window or tab. You may start a fresh session after ensuring " +
						"that your network connection is active. Thank you for using pgStudio!", false);
			} catch (Throwable e) {
				showAlert(e.getMessage(), false);
			}
    }
}