package com.toolplat.mosaic.ui.contoroller;

/**
 * FXML User Interface enum
 * <p>
 * Created by Owen on 6/20/16.
 */
public enum FXMLPage {

    NEW_CONNECTION("fxml/newConnection.fxml"),
    ;

    private String fxml;

    FXMLPage(String fxml) {
        this.fxml = fxml;
    }

    public String getFxml() {
        return this.fxml;
    }


}
