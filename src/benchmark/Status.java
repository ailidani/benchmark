package benchmark;

public enum Status {

    OK ("OK", "The operation completed successfully."),
    ERROR ("ERROR", "The operation failed."),
    NOT_FOUND ("NOT_FOUND", "The requested record was not found."),
    NOT_IMPLEMENTED ("NOT_IMPLEMENTED", "The operation is not implemented for the current binding."),
    UNEXPECTED_STATE ("UNEXPECTED_STATE", "The operation reported success, but the result was not as expected."),
    BAD_REQUEST ("BAD_REQUEST", "The request was not valid."),
    FORBIDDEN ("FORBIDDEN", "The operation is forbidden."),
    SERVICE_UNAVAILABLE ("SERVICE_UNAVAILABLE", "Dependant service for the current binding is not available."),
    BATCHED_OK ("BATCHED_OK", "The operation has been batched by the binding to be executed later.");

    private final String name;
    private final String description;

    Status(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public boolean isOK() {
        return this == OK || this == BATCHED_OK;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
