package fr.wdes.authentication.custom;

public class Response {
	protected String error;
	protected String errorMessage;
	protected String cause;

    public String getCause() {
        return cause;
    }

    public String getError() {
        return error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}